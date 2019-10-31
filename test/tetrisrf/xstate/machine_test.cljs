(ns tetrisrf.xstate.machine-test
  (:require [cljs.test :refer [deftest is testing async use-fixtures]]
            [tetrisrf.xstate.core :refer [machine
                                          machine->config
                                          machine->options
                                          machine->xs-machine]]
            [xstate :as jsxs]))


(def test-machine-config-1 {:id :test-1
                            :initial :ready
                            :states {:ready {:on {:go :one
                                                  :check :ready}}
                                     :one {:on {:go-next :two}
                                           :states {:two {:on {:go-self :two
                                                               :stop :..ready}}}}}})


(def test-machine-options-1 {:actions {:do #()}})


(deftest machine-creation

  (testing "Default machine options should be empty map"
    (let [m (machine test-machine-config-1)]
      (is (= (machine->options m) {}) "Default machine options are correct")))

  (testing "Machine config and options should be stored unchanged"
    (let [m (machine test-machine-config-1 test-machine-options-1)]
      (is (= (machine->config m) test-machine-config-1) "Machine config got unchanged")
      (is (= (machine->options m) test-machine-options-1) "Machine options got unchanged"))))


(deftest xstate-machine-creation

  (testing "Machine record should hold instance of XState machine"
    (let [m (machine {})]
      (is (instance? jsxs/StateNode (machine->xs-machine m))))))