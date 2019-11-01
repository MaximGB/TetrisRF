(ns tetrisrf.xstate.actions-test
  (:require [cljs.core.async :as casync]
            [cljs.test :refer [deftest is testing async use-fixtures]]
            [tetrisrf.xstate.core :as xs
                                  :refer [interpreter!
                                          interpreter-start!
                                          interpreter-send!
                                          db-action
                                          fx-action
                                          ctx-action]]
            [re-frame.core :as rf]))


(def rf-checkpoint (volatile! nil))

(use-fixtures
  :each
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


(deftest db-action-test
  (testing "DB action co-effect/effect"
    (async done

           (rf/reg-event-db
            ::db-action-test-setup
            (fn [db]
              (assoc db ::db-action-test-key 1)))

           (rf/dispatch-sync [::db-action-test-setup])

           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:entry {:type :in-ready
                                                                     :zzz 10}
                                                             :on {:toggle :running}}
                                                     :running {:entry :in-running}}}

                                           {:actions {:in-ready (db-action
                                                                 (fn [db [event & payload]]
                                                                   (is (= (event :tetrisrf.xstate/xs-init)) "Initialization event is correct")
                                                                   (is (= (count payload) 0) "Initialization event has no payload")
                                                                   (casync/put! c (::db-action-test-key db))
                                                                   (assoc db ::db-action-test-key 2)))
                                                      :in-running (db-action
                                                                   (fn [db [event arg]]
                                                                     (is (= (event :toggle)) "Transtion event is correct")
                                                                     (is (= arg :arg) "Transition event argument is correct")
                                                                     (casync/put! c (::db-action-test-key db))
                                                                     (assoc db ::db-action-test-key 1)))}})]
             (casync/go
               (interpreter-start! interpreter)
               (is (= (casync/<! c) 1) "Got correct test key value from db-action handler")
               (interpreter-send! interpreter :toggle :arg)
               (is (= (casync/<! c) 2) "Got updated test-key value from db-action handler")
               (done))))))


(deftest fx-action-test
  (testing "Fx action co-effect/effect"
    (async done

           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:entry :in-ready}}}

                                           {:actions {:in-ready (fx-action
                                                                 (fn [cofx [event & payload]]
                                                                   (is (= (event :tetrisrf.xstate/xs-init)) "Initialization event is correct")
                                                                   (is (= (count payload) 0) "Initialization event has no payload")
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
                 interpreter (interpreter! {:id :simple-machine
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


(deftest db-action-interceptors-test
  (testing "DB action interceptors injection"
    (async done
           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:on {:toggle {:target :running
                                                                           :actions :to-running}}}
                                                     :running {:entry [:in-running :check-coeffects]}}}

                                           {:actions {:to-running (db-action
                                                                   [::test-coeffect-1 ::test-coeffect-2]
                                                                   identity)
                                                      :in-running (db-action
                                                                   [(rf/inject-cofx ::test-coeffect-3 3)]
                                                                   identity)
                                                      :check-coeffects (fn [re-ctx]
                                                                         (casync/put! c [(rf/get-coeffect re-ctx ::test-coeffect-1)
                                                                                         (rf/get-coeffect re-ctx ::test-coeffect-2)
                                                                                         (rf/get-coeffect re-ctx ::test-coeffect-3)]))}})]

             (rf/reg-cofx
              ::test-coeffect-1
              (fn [cofx]
                (assoc cofx ::test-coeffect-1 1)))

             (rf/reg-cofx
              ::test-coeffect-2
              (fn [cofx]
                (assoc cofx ::test-coeffect-2 2)))

             (rf/reg-cofx
              ::test-coeffect-3
              (fn [cofx val]
                (assoc cofx ::test-coeffect-3 val)))

             (casync/go
               (interpreter-start! interpreter)
               (interpreter-send! interpreter :toggle)
               (is (= (casync/<! c) [1 2 3]) "All coeffects are injected")
               (done))))))


(deftest fx-action-interceptors-test
  (testing "FX action interceptors injection"
    (async done
           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:on {:toggle {:target :running
                                                                           :actions :to-running}}}
                                                     :running {:entry [:in-running :check-coeffects]}}}

                                           {:actions {:to-running (fx-action
                                                                   [::test-coeffect-1 ::test-coeffect-2]
                                                                   (fn []))
                                                      :in-running (fx-action
                                                                   [(rf/inject-cofx ::test-coeffect-3 3)]
                                                                   (fn []))
                                                      :check-coeffects (fn [re-ctx]
                                                                         (casync/put! c [(rf/get-coeffect re-ctx ::test-coeffect-1)
                                                                                         (rf/get-coeffect re-ctx ::test-coeffect-2)
                                                                                         (rf/get-coeffect re-ctx ::test-coeffect-3)]))}})]

             (rf/reg-cofx
              ::test-coeffect-1
              (fn [cofx]
                (assoc cofx ::test-coeffect-1 1)))

             (rf/reg-cofx
              ::test-coeffect-2
              (fn [cofx]
                (assoc cofx ::test-coeffect-2 2)))

             (rf/reg-cofx
              ::test-coeffect-3
              (fn [cofx val]
                (assoc cofx ::test-coeffect-3 val)))

             (casync/go
               (interpreter-start! interpreter)
               (interpreter-send! interpreter :toggle)
               (is (= (casync/<! c) [1 2 3]) "All coeffects are injected")
               (done))))))


(deftest ctx-action-interceptors-test
  (testing "CTX action interceptors injection"
    (async done
           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:on {:toggle {:target :running
                                                                           :actions :to-running}}}
                                                     :running {:entry [:in-running :check-coeffects]}}}

                                           {:actions {:to-running (ctx-action
                                                                   [::test-coeffect-1 ::test-coeffect-2]
                                                                   identity)
                                                      :in-running (ctx-action
                                                                   [(rf/inject-cofx ::test-coeffect-3 3)]
                                                                   identity)
                                                      :check-coeffects (fn [re-ctx]
                                                                         (casync/put! c [(rf/get-coeffect re-ctx ::test-coeffect-1)
                                                                                         (rf/get-coeffect re-ctx ::test-coeffect-2)
                                                                                         (rf/get-coeffect re-ctx ::test-coeffect-3)]))}})]

             (rf/reg-cofx
              ::test-coeffect-1
              (fn [cofx]
                (assoc cofx ::test-coeffect-1 1)))

             (rf/reg-cofx
              ::test-coeffect-2
              (fn [cofx]
                (assoc cofx ::test-coeffect-2 2)))

             (rf/reg-cofx
              ::test-coeffect-3
              (fn [cofx val]
                (assoc cofx ::test-coeffect-3 val)))

             (casync/go
               (interpreter-start! interpreter)
               (interpreter-send! interpreter :toggle)
               (is (= (casync/<! c) [1 2 3]) "All coeffects are injected")
               (done))))))
