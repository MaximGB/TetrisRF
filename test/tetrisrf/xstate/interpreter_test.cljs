(ns tetrisrf.xstate.interpreter-test
  (:require [cljs.test :refer [deftest is testing async use-fixtures]]
            [cljs.core.async :as casync]
            [re-frame.core :as rf]
            [tetrisrf.xstate.core :as xs :refer [machine
                                                 interpreter!
                                                 interpreter->machine
                                                 interpreter-start!
                                                 interpreter-stop!
                                                 interpreter-send!]]))

(def rf-checkpoint (volatile! nil))

(use-fixtures
  :each
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


(deftest simple-machine-interpreter
  (testing "Simple machine interpreter"
    (async done
           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 machine-spec {:id :simple-machine
                               :initial :ready
                               :states {:ready {:on {:toggle :running}
                                                :entry :at-ready}
                                        :running {:on {:toggle :ready}
                                                  :entry :at-running}}}
                 machine-opts {:actions {:at-running #(casync/put! c :at-running)
                                         :at-ready #(casync/put! c :at-ready)}}
                 interpreter (interpreter! machine-spec machine-opts)]
             (casync/go
               (interpreter-start! interpreter)
               (is (= :at-ready (casync/<! c)) "Machine initialized at `ready` state")
               (interpreter-send! interpreter :toggle)
               (is (= :at-running (casync/<! c)) "Machine toggled to `running` state")
               (interpreter-send! interpreter :toggle)
               (is (= :at-ready (casync/<! c)) "Machine toggled to `ready` state")
               (done))))))


(deftest multiple-actions-test
  (testing "Multiple actions execution and their order of execution")
  (async done
         (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
               interpreter (interpreter! {:id :simple-machine
                                          :initial :ready
                                          :states {:ready {:on {:toggle {:target :running
                                                                         :actions [:one :two :three]}}}
                                                   :running {}}}
                                         {:actions {:one #(casync/put! c :one)
                                                    :two #(casync/put! c :two)
                                                    :three #(casync/put! c :three)}})]
           (casync/go
             (interpreter-start! interpreter)
             (interpreter-send! interpreter :toggle)
             (is (= (casync/<! c) :one) "Action :one executed")
             (is (= (casync/<! c) :two) "Action :two executed")
             (is (= (casync/<! c) :three) "Action :three executed")
             (done)))))
