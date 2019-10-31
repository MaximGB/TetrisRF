(ns tetrisrf.xstate.impl.interpreter
  (:require [tetrisrf.xstate.protocols :as protocols]
            [tetrisrf.xstate.utils :as utils]
            [tetrisrf.xstate.impl.machine :as machine]
            [re-frame.core :as rf]
            [xstate :as xs]))


(defn- execute-transition-actions
  "Executes given `actions` in re-frame context `re-ctx`."
  [re-ctx actions]
  (areduce actions idx ret re-ctx
           (let [action (aget actions idx)
                 exec (or (aget action "exec") (aget action "xs-exec") identity)
                 action-result (exec ret)]
             (if (map? action-result)
               action-result
               ret))))


(defn- actions->interceptors
  "Collects vector of unique action interceptors (#js [action]) -> [].

   If several actions require same interceptor the interceptor will be included only once."
  [actions]
  (last (areduce actions idx result [#{} []]
                 (-> (aget actions idx)
                     ((fn [action] []))
                     ((fn [action-interceptors]
                        (let [[result-interceptors-set result-interceptors-vec] result
                              action-interceptors-filtered (filterv (fn [interceptor]
                                                                      (not (result-interceptors-set interceptor)))
                                                                    action-interceptors)]
                          [(into result-interceptors-set action-interceptors-filtered)
                           (into result-interceptors-vec action-interceptors-filtered)])))))))


; Re-frame event handler serving as the bridge between re-frame and XState
(rf/reg-event-ctx
 ::xs-transition-event
 (fn [re-ctx]
   (let [*interpreter (utils/re-ctx->*interpreter re-ctx)]
     (protocols/-interpreter-transition! *interpreter re-ctx))))



;; Re-frame interceptor executing state transition actions
(def exec-interceptor
  (rf/->interceptor
   :id ::xs-actions-exec-interceptor
   :before (fn [re-ctx]
             (let [*interpreter (utils/re-ctx->*interpreter re-ctx)
                   xs-state (protocols/interpreter->state *interpreter)
                   actions (.-actions xs-state)]
               (execute-transition-actions re-ctx actions)))))


(declare interpreter-send!)


(defn- interpreter-
  [machine-or-spec machine-options defer-events?]
  (let [vid (str ::interpreter-id)
        *interpreter (volatile! {:machine (if (instance? machine/Machine machine-or-spec)
                                            machine-or-spec
                                            (machine/machine machine-or-spec machine-options))
                                 :state nil
                                 :started? false
                                 :defer-events? defer-events?})]
    (reify
      IDeref

      (-deref [this] @*interpreter)

      protocols/InterpreterProto

      (interpreter->machine [this]
        (:machine @*interpreter))

      (interpreter->state [this]
        (:state @*interpreter))

      (interpreter->started? [this]
        (:started? @*interpreter))

      (interpreter->defer-events? [this]
        (:defer-events? @*interpreter))

      (interpreter-start! [this]
        (let [started? (protocols/interpreter->started? this)]
          (if-not started?
            ;; Starting
            (do
              (vswap! *interpreter
                      assoc
                      :started? true)
              ;; Dispatching self-initialization event to transit to machine initial state
              (interpreter-send! this ::xs-init)))
          ;; Always return self
          this))

      (interpreter-stop! [this]
        (let [started? (protocols/interpreter->started? this)]
          (if started?
            (let [interpreter @*interpreter]
              ;; Updating self
              (vswap! *interpreter
                      assoc
                      :started? false)))
          this))

      (interpreter-send-! [this event]
        (rf/dispatch [::xs-transition-event this event])
        ;; Always return self
        this)

      protocols/-InterpreterProto

      (-interpreter-transition! [this re-ctx]
        (let [machine (protocols/interpreter->machine this)
              xs-machine (protocols/machine->xs-machine machine)
              xs-event-type (utils/re-ctx->xs-event-type re-ctx)
              xs-current-state (protocols/interpreter->state this)
              xs-new-state (if xs-current-state
                             (.transition xs-machine
                                          (.from xs/State xs-current-state re-ctx)
                                          ;; Only type is needed for XState to make transition
                                          ;; Event payload handlers will take from `re-ctx` afterwards
                                          (clj->js xs-event-type))
                             (.-initialState (.withContext xs-machine re-ctx)))
              actions (.-actions xs-new-state)
              interceptors (actions->interceptors actions)]
          (vswap! *interpreter
                  assoc
                  :state xs-new-state)
          (rf/enqueue re-ctx (conj interceptors exec-interceptor)))))))


(defn interpreter
  "Creates XState based interpreter which uses re-frame facilities to send/receive and handle events"

  ([machine-or-spec & [machine-options & kvargs :as vargs]]
   (if (keyword? machine-options)
     (let [[& {:keys [defer-events?] :or {defer-events? false}}] vargs]
       (interpreter- machine-or-spec {} defer-events?))
     (let [[& {:keys [defer-events?] :or {defer-events? false}}] kvargs]
       (interpreter- machine-or-spec machine-options defer-events?)))))


(defn interpreter-send!
  "Sends an event to XState machine via re-frame facilities and initiates re-frame event processing using XState machine actions."
  [interpreter event & payload]
  (protocols/interpreter-send-! interpreter
                                 (into [event] payload)))
