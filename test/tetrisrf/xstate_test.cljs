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
              interpreter-send!
              interpreter->xs-state]]
            [xstate :as jsxs]))

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
                            :states {:ready {:entry [:in-ready :common-in]
                                             :exit :out-ready
                                             :on {:go {:target :running
                                                       :cond [:can-run-now]
                                                       :actions [:to-running, :common-action]}}
                                             :states  {:steady {:on {:go {:target :running
                                                                          :actions :to-running}
                                                                     :wait :steady}}}}
                                     :running {:entry [:in-running :common-in]
                                               :exit :out-running
                                               :on {::stop {:target :ready
                                                           :cond [:can-stop-now]
                                                           :actions [:to-ready :common-action]}}}}})


(def test-machine-options-2 {:actions {:in-ready {:exec #()
                                                  :interceptors []}}
                             :guards {:can-run-now {:exec #()
                                                    :interceptors []}}})


(deftest machine-creation

  (testing "Default machine options should be empty map"
    (let [m (machine test-machine-config-1)]
      (is (= (machine->options m) {}) "Default machine options are correct")))

  (testing "Machine config and options should be stored unchanged"
    (let [m (machine test-machine-config-1 test-machine-options-1)]
      (is (= (machine->config m) test-machine-config-1) "Machine config got unchanged")
      (is (= (machine->options m) test-machine-options-1) "Machine options got unchanged"))))


;; TODO: remove this, seems I don't need it
#_(deftest events-extraction

  (testing "It should be possible to extract all events defined for the all machine states"
    (let [m (machine test-machine-config-1)]
      (is (= #{:check :go :go-next :go-self :stop}
             (machine->events m))
          "Events are extracted correctly"))))


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



(-> (interpreter {:id :ttt
                  :initial :start
                  :states {:start {:entry :started}}}

                 {:actions {:started #(.log js/console "Started!!!")}})
    (:machine)
    (machine->xs-initial-state)
    (#(.log js/console %)))

(db-action [arr] body)
(fx-action [arr] body)
(ctx-action [arr] body)

(db-guard [db event])
(fx-guard [fx event])
(ctx-guard [ctx event])

(def test-fn (with-meta (fn []) {::action-type ::db}))

(def test-fn (fn []))

(.with-context machine ctx)

(meta test-fn)
