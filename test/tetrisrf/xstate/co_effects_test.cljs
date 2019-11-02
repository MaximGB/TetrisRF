(ns tetrisrf.xstate.co-effects-test
  (:require [cljs.core.async :as casync]
            [cljs.test :refer [deftest is testing async use-fixtures]]
            [re-frame.core :as rf]
            [tetrisrf.xstate.core :as xs
                                  :refer [machine
                                          interpreter!
                                          interpreter-start!
                                          interpreter-send!
                                          interpreter->id
                                          fx-action
                                          cofx-interpreter-id]]))


(def rf-checkpoint (volatile! nil))

(use-fixtures
  :each
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


(deftest interpreter-id-cofx-test
  (testing "Interpreter id co-effect injection"
    (async done
           (let [c (casync/timeout 100)
                 interpreter (interpreter! (machine {:id :simple-machine
                                                     :initial :ready
                                                     :states {:ready {:entry :in-ready}}}

                                                    {:actions {:in-ready (fx-action
                                                                          [cofx-interpreter-id]
                                                                          (fn [cofx]
                                                                            (casync/put! c (cofx-interpreter-id cofx))))}}))]
             (casync/go
               (interpreter-start! interpreter)
               (is (= (casync/<! c) (interpreter->id interpreter)) "Correct interpreter id injected")
               (done))))))
