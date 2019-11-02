(ns tetrisrf.xstate.co-effects
  (:require [re-frame.core :as rf]
            [tetrisrf.xstate.protocols :as protocols]))


;; Inject interpreter id into co-effects map
(rf/reg-cofx
 ::interpreter-id
 (fn [cofx & [as]]
   (let [[_ interpreter] (:event cofx)]
     (assert (satisfies? protocols/InterpreterProto interpreter)
             "Can't inject interpreter id, the event being handled is non XState interpreter event!")
     (assoc cofx
            (or as ::interpreter-id)
            (protocols/interpreter->id interpreter)))))
