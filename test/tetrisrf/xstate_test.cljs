(ns tetrisrf.xstate-test
  (:require [cljs.pprint :refer [pprint]]
            [cljs.test :refer-macros [deftest is testing]]
            [tetrisrf.xstate
             :as
             xs
             :refer
             [machine
              machine->config
              machine->events-at-state
              machine->options
              machine->xs-initial-state
              machine->xs-machine
              interpreter
              interpreter-start!
              interpreter-stop!
              interpreter-send!]
             :refer-macros
             [db-action
              fx-action
              ctx-action
              db-guard
              fx-guard
              ctx-guard]]
            [xstate :as jsxs]
            [re-frame.core :as rf]))

(def test-machine-config-1 {:id :test-1
                            :initial :ready
                            :states {:ready {:on {:go :one
                                                  :check :ready}}
                                     :one {:on {:go-next :two}
                                           :states {:two {:on {:go-self :two
                                                               :stop :..ready}}}}}})

(def test-machine-options-1 {:actions {:do #()}})


(def test-machine-config-2 {:id :test-2
                            :initial :ready
                            :states {:ready {:entry [:in-ready-1 :in-ready-2]
                                             :exit :out-ready-1
                                             :on {:go {:target :running
                                                       :cond [:can-run-now]
                                                       :actions [:on-go-1, :on-go-2]}}
                                             :states  {:steady {:on {:go {:target :running
                                                                          :actions :on-go-1}
                                                                     :wait :steady}}}}
                                     :running {:entry [:in-running-1]
                                               :exit :out-running-1
                                               :on {::stop {:target :ready
                                                           :cond [:can-stop-now]
                                                           :actions [:on-stop-1 :on-stop-2]}}}}})


(def test-machine-options-2 {:actions {:in-ready-1 (db-action ['interceptor-1] [] nil)
                                       :in-ready-2 nil
                                       :out-ready-1 (fx-action ['interceptor-2] [] nil)
                                       :on-go-1 nil
                                       :on-go-2 nil
                                       :in-running-1 nil
                                       :out-running-1 nil
                                       :on-stop-1 nil
                                       :on-stop-2 nil}
                             :guards {:can-run-now (db-guard ['interceptor-3] [] nil)
                                      :can-stop-now (db-guard ['interceptor-4] [] nil)}})


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


(deftest x-initial-state-retrieval

  (testing "Machine should be able to return initial state instance"
    (let [m (machine test-machine-config-1)]
      (is (instance? jsxs/State (machine->xs-initial-state m))))))


(deftest events-at-state-extraction

  (testing "It should be possible to extract events at state from machine config"
    (let [m (machine test-machine-config-2)
          eas (machine->events-at-state m)]
      (is (= eas [":ready@:go"
                  ":ready@:steady@:go"
                  ":ready@:steady@:wait"
                  ":running@:tetrisrf.xstate-test/stop"])))))
