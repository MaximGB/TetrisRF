(ns tetrisrf.xstate-test
  (:require [cljs.pprint :refer [pprint]]
            [cljs.core.async :as asy]
            [cljs.test :refer [deftest is testing async use-fixtures]]
            [tetrisrf.xstate
             :as
             xs
             :refer
             [machine
              machine->config
              machine->options
              machine->xs-machine
              interpreter
              interpreter->machine
              interpreter-start!
              interpreter-stop!
              interpreter-send!
              db-action
              fx-action
              db-guard
              ttt]]
            [xstate :as jsxs]
            [re-frame.core :as rf]))


(def rf-checkpoint (volatile! nil))

(use-fixtures
  :each
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


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


(def test-machine-options-2 {:actions {:in-ready-1 (db-action ['interceptor-1] (fn []))
                                       :in-ready-2 nil
                                       :out-ready-1 (fx-action ['interceptor-2] (fn []))
                                       :on-go-1 nil
                                       :on-go-2 nil
                                       :in-running-1 nil
                                       :out-running-1 nil
                                       :on-stop-1 nil
                                       :on-stop-2 nil}
                             :guards {:can-run-now (db-guard (fn []))
                                      :can-stop-now (db-guard (fn []))}})


#_(deftest machine-creation

  (testing "Default machine options should be empty map"
    (let [m (machine test-machine-config-1)]
      (is (= (machine->options m) {}) "Default machine options are correct")))

  (testing "Machine config and options should be stored unchanged"
    (let [m (machine test-machine-config-1 test-machine-options-1)]
      (is (= (machine->config m) test-machine-config-1) "Machine config got unchanged")
      (is (= (machine->options m) test-machine-options-1) "Machine options got unchanged"))))


#_(deftest xstate-machine-creation

  (testing "Machine record should hold instance of XState machine"
    (let [m (machine {})]
      (is (instance? jsxs/StateNode (machine->xs-machine m))))))


#_(deftest simple-machine-interpreter
  (testing "Simple machine interpreter"
    (async done
           (let [c (asy/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 machine-spec {:id :simple-machine
                               :initial :ready
                               :states {:ready {:on {:toggle :running}
                                                :entry :at-ready}
                                        :running {:on {:toggle :ready}
                                                  :entry :at-running}}}
                 machine-opts {:actions {:at-running #(asy/put! c :at-running)
                                         :at-ready #(asy/put! c :at-ready)}}
                 interpreter (interpreter machine-spec machine-opts)]
             (asy/go
               (interpreter-start! interpreter)
               (is (= :at-ready (asy/<! c)) "Machine initialized at `ready` state")
               (interpreter-send! interpreter :toggle)
               (is (= :at-running (asy/<! c)) "Machine toggled to `running` state")
               (interpreter-send! interpreter :toggle)
               (is (= :at-ready (asy/<! c)) "Machine toggled to `ready` state")
               (done))))))


(deftest db-action-test
  (testing "DB action co-effect/effect"
    (async done
           (rf/reg-event-db
            ::db-action-test-setup
            (fn [db]
              (assoc db ::db-action-test-key 1)))
           (rf/dispatch-sync [::db-action-test-setup])
           (let [c (asy/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter {:id :simple-machine
                                           :initial :ready
                                           :states {:ready {:entry :in-ready
                                                            :on {:toggle :running}}
                                                    :running {:entry :in-running}}}
                                          {:actions {:in-ready (db-action
                                                                (fn [db]
                                                                  (asy/put! c (::db-action-test-key db))
                                                                  (assoc db ::db-action-test-key 2)))
                                                     :in-running (db-action
                                                                  (fn [db]
                                                                    (asy/put! c (::db-action-test-key db))
                                                                    (assoc db ::db-action-test-key 1)))}})]
             (asy/go
               (interpreter-start! interpreter)
               (is (= (asy/<! c) 1) "Got correct test key value from db-action handler")
               (interpreter-send! interpreter :toggle)
               (is (= (asy/<! c) 2) "Got updated test-key value from db-action handler")
               (done))))))
