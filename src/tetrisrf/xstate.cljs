(ns tetrisrf.xstate
  (:require [xstate :as xs]
            [com.rpl.specter :as s :include-macros true]
            [re-frame.core :as rf]
            [clojure.string :as cstr]
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


;; TODO: remove this, seems I don't need it
;; (def EVENTS (s/recursive-path []
;;                               p
;;                               [:states s/MAP-VALS (s/multi-path [:on s/MAP-KEYS]
;;                                                                 p)]))


;; (defn machine->events
;;   "Returns set of all events machine can handle"
;;   [machine]
;;   (->> machine
;;        (machine->config)
;;        (s/traverse EVENTS)
;;        (into #{})))


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


(defprotocol InterpreterProto
  "XState based interpreter protocol which uses re-frame facilities to send/recieve and handle events"
  (interpreter->xs-state [this]
    "Returns current interpreting machine state path.")
  (interpreter->started? [this]
    "Checks if interpreter has been started.")
  (interpreter-start! [this]
    "Starts machine interpretation. Registers re-frame event handlers to recieve events of the machine.")
  (interpreter-stop! [this]
    "Stops machine interpretation. Un-registers re-frame event handlers registered at (start) call.")
  (interpreter-send! [this event params]
    "Sends and event to the machine via re-frame facilities"))


;; Event sent to re-frame to make initial transition for a machine
(def initial-transition-event ::initial-transition-event)

(rf/reg-event-ctx
 initial-transition-event
 (fn [ctx]
   (let [[_ interpreter] (get-in ctx [:coeffects :event])
         xs-state (interpreter->xs-state interpreter)
         actions (.-actions xs-state)
         fx-map (areduce actions idx ret {}
                  (let [action (aget actions idx)
                        exec (.-exec action)
                        action-result (exec ctx)]
                    (if (map? action-result)
                      (merge ret action-result)
                      ret)))]
     (assoc ctx :effects fx-map))))


(defrecord Interpreter [machine state started? defer-events machine-events]

  InterpreterProto

  (interpreter->xs-state [this]
    (machine->xs-machine (:machine this)))

  (interpreter->started? [this]
    (:started? this))

  (interpreter-start! [this]
    (let [started? (:started? this)]
      (if started?
        this
        (let [m (:machine this)
              initial-state (machine->xs-initial-state m)
              events (or (:events this) (machine->events-at-state m))
              result (assoc this
                            :machine (assoc m :xs-machine initial-state)
                            :machine-events events
                            :started? true)]
          (doseq [e events]
            (rf/reg-event-fx e #({})))
          (rf/dispatch [initial-transition-event result])
          result))))

  (interpreter-stop! [this]
    (let [started? (:started? this)]
      (if (not started?)
        this
        (let [events (:machine-events this)]
          (doseq [e events]
            (rf/clear-event e))
          (assoc this
                 :started? false)))))

  (interpreter-send! [this event params]
    #_(let [state (interpreter->state this)]
      state)))


;; Reify over a volatile thus implementing the protocol

(defn- interpreter-
  [machine-or-spec machine-options defer-events]
  (map->Interpreter {:machine (if (instance? Machine machine-or-spec)
                                machine-or-spec
                                (machine machine-or-spec machine-options))
                     :started? false
                     :state nil
                     :machine-events nil
                     :defer-events defer-events}))


(defn interpreter
  "Creates XState based interpreter which uses re-frame facilities to send/receive and handle events"

  ([machine-or-spec & [machine-options & kvargs :as vargs]]
   (if (keyword? machine-options)
     (let [[& {:keys [defer-events] :or {defer-events false}}] vargs]
       (interpreter- machine-or-spec {} defer-events))
     (let [[& {:keys [defer-events] :or {defer-events false}}] kvargs]
       (interpreter- machine-or-spec machine-options defer-events)))))




;; start return an atom which can recieve events

;; (def m (xs/Machine (clj->js {:id :test
;;                              :initial :ready
;;                              :type :parallel
;;                              :states {:ready {:entry [:do1]}
;;                                       :idle {:entry [:do2]}}})
;;                    (clj->js {:actions {:do1 #(.log js/console "ready entry")
;;                                        :do2 #(.log js/console "idle entry")}})))

;; (def initial-state (.-initialState m))

;; (def initial-actions (.-actions initial-state))

;; (areduce initial-actions
;;          idx
;;          ret
;;          {}
;;          (let [action (aget initial-actions idx)
;;                exec (.-exec action)
;;                r (exec)]
;;            (if (map? r)
;;              (merge ret m)
;;              ret)))

;; (defn machine
;;   "Creates a XState machine with given definition and optional options"
;;   ([definition]
;;    (xs/Machine (clj->js definition)))
;;   ([definition options]
;;    (xs/Machine (clj->js definition) (clj->js options))))


;; (defn service

;;   ([machine-or-definition]
;;    (let [machine (if (instance? xs/Machine machine-or-definition)
;;                    machine-or-definition
;;                    (machine machine-or-definition))
;;          service-atom (atom machine)]
;;      ;;

;;      ))

;;   ([definition options]
;;    (service (machine definition options))))


;; (def a1 {:a (js/Date. 2019)})

;; (def a2 {:a (js/Date. 2019)})

;; (= a1 a2)

;; a1
;; a2

;; (defn- inject-machine-spec-value-accumulators [machine-spec acc])

;; (defn- inject-machine-options-value-accumulators [machine-spec acc])

;; (defn xinterpret
;;   "Defines an XState machine interpreting service which automaticaly registers corresponding re-frame event handlers to recieve events via re-frame machinery."
;;   [machine-spec machine-options]
;;   (let [xservice (service machine-spec machine-options {:execute false})]))




;; (def machine-spec {:id :tetrisrf
;;                    :initial :ready
;;                    :states {:ready {:on {:toggle :ready}}}})


;; (defn create-xservice [on-state-change]
;;   (-> machine-spec
;;       (clj->js)
;;       (xs/Machine.)
;;       (xs/interpret.)
;;       (.onTransition on-state-change)
;;       (.start)))


;; (defn create-service [machine options]
;;   (-> (xs/interpret machine options)
;;       (.start)))


;; (def test-machine (xs/Machine (clj->js {:id :test
;;                                         :initial :ready
;;                                         :states {:ready {:on {:toggle {:target :ready
;;                                                                        :actions [:do]}}
;;                                                          :entry [:entry]
;;                                                          :exit [:exit]}}})
;;                               (clj->js {:actions {:do #(do
;;                                                          (.log js/console "do!")
;;                                                          {:do 1})
;;                                                   :entry #(do
;;                                                             (.log js/console "entry!")
;;                                                             {:entry 2})
;;                                                   :exit #(do
;;                                                            (.log js/console "exit!")
;;                                                            {:exit 3})}})))

;; (def test-service (create-service test-machine (clj->js {:execute false})))

;; (.execute test-service (.send test-service (name :toggle)))

;; (str (namespace ::test) "/" (name ::test))


;; (defService myCompService {:id ::myCompService
;;                            :initial ::ready
;;                            :states {::ready {:on {::run {:target ::run
;;                                                          :actions [:do-something]}}}
;;                                     ::run {:entry [:do-other-thing]
;;                                            :exit [:do-next-thing]
;;                                            :on {::stop ::ready}}}})



;; (send myCompService ::run 1 2 3)

;; (defn send [service ])
