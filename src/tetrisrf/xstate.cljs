(ns tetrisrf.xstate
  (:refer-clojure
   :exclude [clj->js])
  (:require [xstate :as xs]
            [com.rpl.specter :as s :include-macros true]
            [re-frame.core :as rf]
            [clojure.string :as cstr]
            [clojure.pprint :refer [pprint]]))


(defn- clj->js
  "Custom clj->js function, uses `str` to translate keywords to JS, thus `:kv` produces \":kv\" instead of \"kv\"."
  [clj]
  (clojure.core/clj->js clj :keyword-fn str))


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


(defn machine->xs-initial-state
  "Returns XState machine initial state object instance"
  [machine]
  (-> machine
      (machine->xs-machine)
      (.-initialState)))


(def EVENTS-AT-STATE (s/recursive-path []
                                       p
                                       [(s/multi-path
                                         ;; Collecting events
                                         [:on s/MAP-KEYS]
                                         ;; Recursivelly collecting states
                                         [:states s/ALL (s/collect-one s/FIRST) s/LAST p])]))


(defn machine->events-at-state
  "Analyzes machine configuration and produces a set of events a machine can consume.

   Each event is converted to a string and prepended with the full state path it can be consumed at.
   For example if there're two states defined `:one` and `:two` and both can handle `:do` event
   then resulting set of events will have two entries:
   - :one@:do
   - :two@:do"
  [machine]
  (->> machine
       (machine->config)
       (s/select EVENTS-AT-STATE)
       (s/transform s/ALL (partial cstr/join "@"))))


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
  (interpreter-send! ^InterpreterProto [this event]
    "Sends an event to the machine via re-frame facilities."))


(defprotocol -InterpreterProto
  "Module private interpreter protocol, users should not implement or call it's methods."

  (-interpreter-transition! [this state re-ctx]
    "Does the state chart transition.")

  (-interpreter-register-init-transition-event-handler ^InterpreterProto [this]
    #_TODO:_rework_the_docs
    "Register this particular interpreter initial transition event handler.

     Every startchart starts from initial state, upon entring the state actions might be executed,
     each action might have different set of interceptors defined, thus to provide those interceptors
     in re-frame's `reg-event-ctx` event handler the event handler must be registered this those interceptors
     collected and listed. So each interpreter uses unique event and registers unique initial transition event
     handler with intereptors list collected from the actions and listed in call to `reg-event-fx`.")

  (-interpreter-clear-init-transition-event-handler ^InterpreterProto [this]
    #_TODO:_rework_the_docs
    "Clears this particular interpreter unique initial transition event handler.

     See `-register-init-transition-event-handler` for more information."))


(defn uni-handler
  "Universal re-frame event handler which translates re-frame event and payload into XState event and makes corresponding XState machine transition."
  [vinterpreter ctx]
  #_TODO
  ctx)


;; Event sent to re-frame to make initial transition for an interpreter
(def initial-transition-event ::initial-xs-transition-event)

;; Event send to re-frame to make other transitions for an interpreter
(def transition-event ::xs-transition-event)


(rf/reg-event-ctx
 initial-transition-event
 (fn [ctx]
   (let [[_ vinterpreter] (get-in ctx [:coeffects :event])
         machine (interpreter->machine vinterpreter)
         xs-machine (machine->xs-machine machine)
         xs-machine-w-context (.withContext xs-machine ctx)
         xs-initial-state (.-initialState xs-machine-w-context)
         actions (.-actions xs-initial-state)]
     ;; Updating interpreter state

     ;; Executing actions
     (areduce actions idx ret ctx
              (let [action (aget actions idx)
                    exec (.-exec action)]
                (exec ret))))))


(defn- interpreter-
  [machine-or-spec machine-options defer-events?]
  (let [vid (str ::interpreter-id)
        vinterpreter (volatile! {:machine (if (instance? Machine machine-or-spec)
                                            machine-or-spec
                                            (machine machine-or-spec machine-options))
                                 :state nil
                                 :started? false
                                 :machine-events nil
                                 :defer-events? defer-events?})]
    (reify
      IDeref

      (-deref [this] @vinterpreter)

      InterpreterProto

      (interpreter->machine [this]
        (:machine @vinterpreter))

      (interpreter->state [this]
        (if-let [state (:state @vinterpreter)]
          (.-id state)))

      (interpreter->started? [this]
        (:started? @vinterpreter))

      (interpreter->defer-events? [this]
        (:defer-events? @vinterpreter))

      (interpreter-start! [this]
        (let [started? (interpreter->started? this)]
          (if-not started?
            ;; Starting
            (let [interpreter @vinterpreter
                  machine (:machine interpreter)
                  events (or (:events interpreter)
                             (machine->events-at-state machine))]
              ;; Registering re-frame handlers for every possible state@event combination
              (doseq [e events]
                (rf/reg-event-ctx e
                                  #_TODO:_Collect_interceptors
                                  (fn [ctx]
                                    (uni-handler this ctx))))
              ;; Updating self
              (vswap! assoc
                      :started? true
                      :machine-events events)
              ;; Dispatching self-initialization event to transit to machine initial state
              (rf/dispatch [initial-transition-event this])))
          ;; Always return self
          this))

      (interpreter-stop! [this]
        (let [started? (interpreter->started? this)]
          (if started?
            (let [interpreter @vinterpreter
                  events (:machine-events interpreter)]
              ;; Clearing re-frame handlers registered at interpreter-start!
              (doseq [e events]
                (rf/clear-event e))
              ;; Updating self
              (vswap! assoc
                      :started? false)))
          ;; Always return self
          this))

      (interpreter-send! [this event]
        #_TODO)

      -InterpreterProto

      (-interpreter-transition! [this state re-ctx]
        #_TODO)

      (-interpreter-register-init-transition-event-handler [this]
        #_TODO)

      (-interpreter-clear-init-transition-event-handler [this]
        #_TODO))))



(defn interpreter
  "Creates XState based interpreter which uses re-frame facilities to send/receive and handle events"

  ([machine-or-spec & [machine-options & kvargs :as vargs]]
   (if (keyword? machine-options)
     (let [[& {:keys [defer-events?] :or {defer-events? false}}] vargs]
       (interpreter- machine-or-spec {} defer-events?))
     (let [[& {:keys [defer-events?] :or {defer-events? false}}] kvargs]
       (interpreter- machine-or-spec machine-options defer-events?)))))


(defn wrap-db-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is a function similar to re-frame's reg-event-db handler (db event-vector) -> db
  The function also contains required interceptors in it's metadata."

  ([handler]
   (wrap-db-action [] handler))

  ([interceptors handler]
   (with-meta
     (fn [ctx]
       (let [db (get-in ctx [:coeffects :db])
             event (get-in ctx [:coeffects :event])
             new-db (or (handler db event) db)]
         (-> ctx
             ;; Assoc into both since there might be other action handlers which
             ;; read from [:coeffects :db]
             (assoc-in [:effects :db] new-db)
             (assoc-in [:coeffects :db] new-db))))
     {::xs-handler interceptors})))


(defn wrap-fx-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is function similar to re-frame's reg-event-fx handler (cofx-map event-vector) -> fx-map.
  The function also contains required interceptors in it's metadata."

  ([handler]
   (wrap-fx-action [] handler))

  ([interceptors handler]
   (with-meta
     (fn [ctx]
       (let [cofx-map (:coeffects ctx)
             event (:event cofx-map)
             new-effects (handler cofx-map event)
             effects (merge (:effects ctx) new-effects)]
         (-> ctx
             (assoc :effects effects)
             ;; Special :db handling since there might be other action handlers
             ;; reading :db from :coeffects
             (#(let [new-db (get-in % [:effects :db])]
                 (if new-db
                   (assoc-in % [:coeffects :db] new-db)))))))
     {::xs-handler interceptors})))


(defn wrap-ctx-action
  "Unlike `wrap-db-action` and `wrap-fx-action` with function doesn't wraps adopting code over given `handler`. There's no need.

  It just adds list of required interceptors to the handler metadata."

  ([handler]
   (wrap-ctx-action [] handler))

  ([interceptors handler]
   (with-meta handler {::xs-handler interceptors})))


(defn wrap-db-guard
  "TODO: docs"

  ([handler]
   (wrap-db-guard [] handler))

  ([interceptors handler]
   (with-meta
     handler
     {::xs-handler interceptors})))


(defn wrap-fx-guard
  "TODO: docs"

  ([handler]
   (wrap-fx-guard [] handler))

  ([interceptors handler]
   (with-meta
     handler
     {::xs-handler interceptors})))


(defn wrap-ctx-guard
  "TODO: docs"

  ([handler]
   (wrap-ctx-guard [] handler))

  ([interceptors handler]
   (with-meta
     handler
     {::xs-handler interceptors})))

;; Event type XState works with
;; {:type nil
;;  :payload nil}
