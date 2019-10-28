(ns tetrisrf.xstate-test
  (:require [cljs.pprint :refer [pprint]]
            [cljs.core.async :as casync]
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
              ctx-action
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
                 interpreter (interpreter machine-spec machine-opts)]
             (casync/go
               (interpreter-start! interpreter)
               (is (= :at-ready (casync/<! c)) "Machine initialized at `ready` state")
               (interpreter-send! interpreter :toggle)
               (is (= :at-running (casync/<! c)) "Machine toggled to `running` state")
               (interpreter-send! interpreter :toggle)
               (is (= :at-ready (casync/<! c)) "Machine toggled to `ready` state")
               (done))))))


(deftest db-action-test
  (testing "DB action co-effect/effect"
    (async done
           (rf/reg-event-db
            ::db-action-test-setup
            (fn [db]
              (assoc db ::db-action-test-key 1)))
           (rf/dispatch-sync [::db-action-test-setup])
           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter {:id :simple-machine
                                           :initial :ready
                                           :states {:ready {:entry :in-ready
                                                            :on {:toggle :running}}
                                                    :running {:entry :in-running}}}
                                          {:actions {:in-ready (db-action
                                                                (fn [db]
                                                                  (casync/put! c (::db-action-test-key db))
                                                                  (assoc db ::db-action-test-key 2)))
                                                     :in-running (db-action
                                                                  (fn [db]
                                                                    (casync/put! c (::db-action-test-key db))
                                                                    (assoc db ::db-action-test-key 1)))}})]
             (casync/go
               (interpreter-start! interpreter)
               (is (= (casync/<! c) 1) "Got correct test key value from db-action handler")
               (interpreter-send! interpreter :toggle)
               (is (= (casync/<! c) 2) "Got updated test-key value from db-action handler")
               (done))))))


(deftest fx-action-test
  (testing "Fx action co-effect/effect"
    (async done

           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter {:id :simple-machine
                                           :initial :ready
                                           :states {:ready {:entry :in-ready}}}
                                          {:actions {:in-ready (fx-action
                                                                (fn [cofx]
                                                                  (is (:event cofx) "Fx-handler recieved `cofx` map")
                                                                  {::async.put :done}))}})]
             (rf/reg-fx
              ::async.put
              (fn [val]
                (casync/put! c val)))

             (casync/go
               (interpreter-start! interpreter)
               (is (= (casync/<! c) :done) "Got correct effect from fx-action handler")
               (done))))))


(deftest ctx-action-test
  (testing "Ctx action co-effect/effect"
    (async done

           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter {:id :simple-machine
                                           :initial :ready
                                           :states {:ready {:entry :in-ready}}}
                                          {:actions {:in-ready (ctx-action
                                                                (fn [re-ctx]
                                                                  (let [event (rf/get-coeffect re-ctx :event)]
                                                                    (is event "Ctx-handler recieved `context`.")
                                                                    (rf/assoc-effect re-ctx ::async.put :done))))}})]
             (rf/reg-fx
              ::async.put
              (fn [val]
                (casync/put! c val)))

             (casync/go
               (interpreter-start! interpreter)
               (is (= (casync/<! c) :done) "Got correct effect from ctx-action handler")
               (done))))))
