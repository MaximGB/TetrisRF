(ns tetrisrf.xstate
  (:require [xstate :as xs]
            [re-frame.core :as rf]
            #_TODO:remove_commented
            #_[com.rpl.specter :as s :include-macros true]
            #_[clojure.string :as cstr]
            [clojure.pprint :refer [pprint]]))


(defprotocol MachineProtocol
  "XState based machine protocol.

   The protocol is used to obtain machine config/options unaltered by clj->js/js->clj transformations."
  (machine->config [this]
    "Returns machine config as a Clojure map")
  (machine->options [this]
    "Returns machine options as a Clojure map")
  (machine->xs-machine [this]
    "Returns XState machine instance"))


(defrecord Machine [config options xs-machine]
  MachineProtocol

  (machine->config [this]
    (:config this))

  (machine->options [this]
    (:options this))

  (machine->xs-machine [this]
    (:xs-machine this)))


(defn machine
  "Creates a XState based machine record with given definition and optional options"

  ([config]
   (machine config {}))

  ([config options]
   (map->Machine {:config config
                  :options options
                  :xs-machine (xs/Machine (clj->js config)
                                          (clj->js options))})))

; --------------------------------------------------------------------------------------------------

(defprotocol InterpreterProto
  "XState based interpreter protocol which uses re-frame facilities to send/recieve and handle events"
  (interpreter->machine ^Machine [this]
    "Returns currently interpreting machine.")
  (interpreter->state ^string  [this]
    "Returns currently active state id.")
  (interpreter->started? ^boolean  [this]
    "Checks if interpreter has been started.")
  (interpreter->defer-events? ^boolean [this]
    "Checks if the interpreter is configured with defer-events? option.")
  (interpreter-start! ^InterpreterProto [this]
    "Starts machine interpretation. Registers re-frame event handlers to recieve events of the machine.")
  (interpreter-stop! ^InterpreterProto [this]
    "Stops machine interpretation. Un-registers re-frame event handlers registered at (start) call.")
  (interpreter-send-! ^InterpreterProto [this event]
    "Sends an event to the machine via re-frame facilities.
     `event` is [event & payload]"))


(defprotocol -InterpreterProto
  "Module private interpreter protocol, users should not implement or call it's methods."

  (-interpreter-transition! [this xs-event re-ctx]
    "Does the state chart transition.

     Returns re-frame context"))


; Re-frame event handler serving as the bridge between re-frame and XState
(rf/reg-event-ctx
 ::xs-transition-event
 (fn [re-ctx]
   (let [[_ vinterpreter xs-event] (rf/get-coeffect re-ctx :event)]
     (-interpreter-transition! vinterpreter xs-event re-ctx))))

(get #js {:a-b 1} :a-b)

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
                     (aget "xs-interceptors")
                     ((fn [action-interceptors]
                        (let [[result-interceptors-set result-interceptors-vec] result
                              action-interceptors-filtered (filterv (fn [interceptor]
                                                                      (not (result-interceptors-set interceptor)))
                                                                    action-interceptors)]
                          [(into result-interceptors-set action-interceptors-filtered)
                           (into result-interceptors-vec action-interceptors-filtered)])))))))


;; Re-frame interceptor executing state transition actions
(def exec-interceptor
  (rf/->interceptor
   :id ::xs-actions-exec-interceptor
   :before (fn [re-ctx]
             (let [[_ vinterpreter] (get-in re-ctx [:coeffects :event])
                   xs-state (interpreter->state vinterpreter)
                   actions (.-actions xs-state)]
               (execute-transition-actions re-ctx actions)))))


(declare interpreter-send!)


(defn- interpreter-
  [machine-or-spec machine-options defer-events?]
  (let [vid (str ::interpreter-id)
        vinterpreter (volatile! {:machine (if (instance? Machine machine-or-spec)
                                            machine-or-spec
                                            (machine machine-or-spec machine-options))
                                 :state nil
                                 :started? false
                                 :defer-events? defer-events?})]
    (reify
      IDeref

      (-deref [this] @vinterpreter)

      InterpreterProto

      (interpreter->machine [this]
        (:machine @vinterpreter))

      (interpreter->state [this]
        (:state @vinterpreter))

      (interpreter->started? [this]
        (:started? @vinterpreter))

      (interpreter->defer-events? [this]
        (:defer-events? @vinterpreter))

      (interpreter-start! [this]
        (let [started? (interpreter->started? this)]
          (if-not started?
            ;; Starting
            (do
              (vswap! vinterpreter
                      assoc
                      :started? true)
              ;; Dispatching self-initialization event to transit to machine initial state
              (interpreter-send! this ::xs-transition-event)))
          ;; Always return self
          this))

      (interpreter-stop! [this]
        (let [started? (interpreter->started? this)]
          (if started?
            (let [interpreter @vinterpreter]
              ;; Updating self
              (vswap! vinterpreter
                      assoc
                      :started? false)))
          this))

      (interpreter-send-! [this event]
        (rf/dispatch [::xs-transition-event this event])
        ;; Always return self
        this)

      -InterpreterProto

      (-interpreter-transition! [this xs-event re-ctx]
        (let [machine (interpreter->machine this)
              xs-machine (machine->xs-machine machine)
              xs-machine-w-context (.withContext xs-machine re-ctx)
              xs-current-state (interpreter->state this)
              xs-new-state (if xs-current-state
                             (.transition xs-machine-w-context xs-current-state (clj->js xs-event))
                             (.-initialState xs-machine-w-context))
              actions (.-actions xs-new-state)
              interceptors (actions->interceptors actions)]
          (vswap! vinterpreter
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
  (interpreter-send-! interpreter
                      ;; This is how XState expects event to be formated
                      {:type event
                       :payload payload}))

; --------------------------------------------------------------------------------------------------

(defn db-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is a function similar to re-frame's reg-event-db handler (db event-vector) -> db
  The function also contains required interceptors in it's metadata."

  ([handler]
   (db-action [] handler))

  ([interceptors handler]
   (let [db-action (fn [re-ctx]
                      (let [db (rf/get-coeffect re-ctx :db)
                            event (rf/get-coeffect re-ctx :event)
                            new-db (or (handler db event) db)]
                        (-> re-ctx
                            ;; Assoc into both since there might be other action handlers which
                            ;; read from `db` coeffect
                            (rf/assoc-effect :db new-db)
                            (rf/assoc-coeffect :db new-db))))]
     {:xs-interceptors interceptors
      :xs-exec db-action
      ;; unlike `xs-exec`, `exec` will be overriden to undefined by XState, but anyway I provide it just for consistency
      :exec db-action})))


(defn fx-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is function similar to re-frame's reg-event-fx handler (cofx-map event-vector) -> fx-map.
  The function also contains required interceptors in it's metadata."

  ([handler]
   (fx-action [] handler))

  ([interceptors handler]
   (let [fx-action (fn [re-ctx]
                     (let [cofx (rf/get-coeffect re-ctx)
                           event (rf/get-coeffect re-ctx :event)
                           new-effects (handler cofx event)]
                       (-> re-ctx
                           ;; TODO: extract into util/merge-fx
                           ((fn [re-ctx]
                              (reduce (fn [re-ctx [effect-key effect-val]]
                                        (rf/assoc-effect re-ctx effect-key effect-val))
                                      re-ctx
                                      new-effects)))
                           ;; Special :db handling since there might be other action handlers
                           ;; reading :db from :coeffects
                           ((fn [re-ctx]
                              (rf/assoc-coeffect re-ctx
                                                 :db
                                                 (rf/get-effect re-ctx :db)))))))]
     {:xs-interceptors interceptors
      :xs-exec fx-action
      ;; unlike `xs-exec`, `exec` will be overriden to undefined by XState, but anyway I provide it just for consistency
      :exec fx-action})))


(defn ctx-action
  "Unlike `db-action` and `fx-action` this function doesn't wrap adopting code over given `handler`. There's no need.

  It just adds list of required interceptors to the handler metadata."

  ([handler]
   (ctx-action [] handler))

  ([interceptors ctx-action]
   {:xs-interceptors interceptors
    :xs-exec ctx-action
    ;; unlike `xs-exec`, `exec` will be overriden to undefined by XState, but anyway I provide it just for consistency
    :exec ctx-action}))


(defn db-guard
  "TODO: docs"

  ([handler]
   #_TODO
   handler))


(defn fx-guard
  "TODO: docs"

  ([handler]
   #_TODO
   handler))


(defn ctx-guard
  "TODO: docs"

  ([handler]
   #_TODO
   handler))
