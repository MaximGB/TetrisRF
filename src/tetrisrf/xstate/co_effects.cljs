(ns tetrisrf.xstate.co-effects
  (:require [re-frame.core :as rf]
            [tetrisrf.xstate.protocols :as protocols]
            [tetrisrf.xstate.utils :as utils]
            [tetrisrf.xstate.impl.interpreter :as interpreter]))


;; Injects interpreter instance into co-effects map
(rf/reg-cofx
 ::instance
 (fn [cofx]
   (let [interpreter (utils/cofx->interpreter cofx)]
     (assert (satisfies? protocols/InterpreterProto interpreter)
             "Can't inject interpreter instance, the event being handled is non XState interpreter event!")
     (assoc cofx
            ::instance
            interpreter))))


;; Spawns a new interpreter or a sequence of interpreters
(rf/reg-cofx
 ::spawn
 (fn [cofx machines]
   (assoc cofx
          ::spawn (cond
                    ;; No machine given spawning self machine
                    (nil? machines) (-> cofx
                                        (utils/cofx->interpreter)
                                        (protocols/interpreter->machine)
                                        (interpreter/interpreter!))
                    ;; One machine given, spawning one interpreter
                    (satisfies? protocols/MachineProto machines) (interpreter/interpreter! machines)
                    ;; Several machines given, spawning several interprters
                    (seqable? machines) (map interpreter/interpreter! machines)
                    ;; Else
                    :else (throw (js/Error. "Unknown ::spawn effect request given, don't know how to spawn!"))))))
