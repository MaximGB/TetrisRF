(ns tetrisrf.xstate.effects
  (:require [re-frame.core :as rf]
            [tetrisrf.xstate.protocols :as protocols]
            [tetrisrf.xstate.impl.registry :as registry]
            [tetrisrf.xstate.utils :as utils]))


(defn- normalize-interpreter
  [id]
  (if (satisfies? protocols/InterpreterProto id)
    id
    (registry/id->interpreter id)))


(defn- normalized-interpreter-start!
  [id payload]
  (protocols/-interpreter-start! (normalize-interpreter id)
                                (cond
                                  (nil? payload) []
                                  (utils/arglist? payload) payload
                                  :else [payload])))

(defn- normalized-interpreter-stop!
  [id]
  (protocols/interpreter-stop! (normalize-interpreter id)))


;; Registers interpreter instance in the registry
(rf/reg-fx
 ::register
 (fn [id-and-interpreter]
   ;; Map is used to register multiple interpreters
   (if (map? id-and-interpreter)
     (doseq [[id interpreter] (seq id-and-interpreter)]
       (registry/register-interpreter! id interpreter))
     (let [[id interpreter] id-and-interpreter]
       (registry/register-interpreter! id interpreter)))))


;; Unregisters intrepreter instance in the registry
(rf/reg-fx
 ::unregister
 (fn [ids]
   (if (seqable? ids)
     ;; Multipe ids unregistration
     (doseq [id ids]
       (registry/unregister-interpreter! id))
     ;; Single id unrgistration
     (registry/unregister-interpreter! ids))))


;; Starts interpreter
(rf/reg-fx
 ::start
 (fn [id-and-payload]
   (cond
     ;; Map is used to start several interpreters
     ;; keys are ids
     ;; values are payload
     (map? id-and-payload)
     (doseq [[id payload] (seq id-and-payload)]
       (normalized-interpreter-start! id payload))

     ;; Non-map seqables are used to pass single id + payload
     (seqable? id-and-payload)
     (let [[id & payload] id-and-payload]
       (normalized-interpreter-start! id payload))

     ;; single id
     :else
     (normalized-interpreter-start! id-and-payload nil))))


;; Stops interpreter
(rf/reg-fx
 ::stop
 (fn [ids]
   (cond
     ;; Map might be used to stop several interpreters
     ;; keys are ids
     ;; values are ignored
     (map? ids)
     (doseq [[id _] (seq ids)]
       (normalized-interpreter-stop! id))

     ;; Non-map seqable might be used to stop several interpeters
     (seqable? ids)
     (doseq [id ids]
       (normalized-interpreter-stop! id))

     ;; Single interpreter stop
     :else
     (normalized-interpreter-stop! ids))))


;; Sends an event to an interpreter
(rf/reg-fx
 ::send
 (fn [[id & event-and-payload]]
   ;; sequence of ids + payload
   ;; single id + payload
   (protocols/-interpreter-send! (normalize-interpreter id)
                                 event-and-payload)))
