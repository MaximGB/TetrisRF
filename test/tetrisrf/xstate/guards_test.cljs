(ns tetrisrf.xstate.guards-test
  (:require [cljs.core.async :as casync]
            [cljs.test :refer [deftest is testing async use-fixtures]]
            [tetrisrf.xstate.core :as xs
                                  :refer [interpreter!
                                          interpreter-start!
                                          interpreter-send!
                                          ev-guard
                                          db-guard
                                          fx-guard
                                          ctx-guard]]
            [re-frame.core :as rf]))


(def rf-checkpoint (volatile! nil))

(use-fixtures
  :each
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


(deftest ev-guard-test
  (testing "Event guard"
    (async done
           (let [c (casync/timeout 100)
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:on {:check [{:cond :filter-age-gte :actions #(casync/put! c :allow)}
                                                                          {:cond :filter-age-lt  :actions #(casync/put! c :deny) }]}}}}

                                           {:guards {:filter-age-lt (ev-guard
                                                                     (fn [_ age]
                                                                       (< age 18)))
                                                     :filter-age-gte (ev-guard
                                                                      (fn [_ age]
                                                                        (>= age 18)))}})]
             (casync/go
               (interpreter-start! interpreter)
               (interpreter-send! interpreter :check 16)
               (is (= (casync/<! c) :deny) "Underage is not allowed")
               (interpreter-send! interpreter :check 20)
               (is (= (casync/<! c) :allow) "Overage is allowed")
               (done))))))


(deftest db-guard-test
  (testing "DB guard"
    (async done
           (let [c (casync/timeout 100)
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:on {:toggle {:target :running
                                                                           :cond :can-run?}}}
                                                     :running {:entry :done}}}

                                           {:actions {:done #(casync/put! c :done)}
                                            :guards {:can-run? (db-guard
                                                                (fn [db [event arg]]
                                                                  (is (= event :toggle) "DB guard has recieved correct event.")
                                                                  (is (= arg :arg) "DB guard has recieved correct event payload.")
                                                                  (::can-run? db)))}})]
             (rf/reg-event-db
              ::db-guard-test-setup
              (fn [db]
                (assoc db ::can-run? true)))

             (rf/dispatch-sync [::db-guard-test-setup])

             (casync/go
               (interpreter-start! interpreter)
               (interpreter-send! interpreter :toggle :arg)
               (is (= (casync/<! c) :done) "Db guard passed truthy value")
               (done))))))


(deftest fx-guard-test
  (testing "FX guard")
    (async done
           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:on {:toggle {:target :running
                                                                           :cond :can-run?}}}
                                                     :running {:entry :done}}}

                                           {:actions {:done #(casync/put! c :done)}
                                            :guards {:can-run? (fx-guard
                                                                (fn [cofx [event arg]]
                                                                  (is (= event :toggle) "Fx-guard has recieved correct event.")
                                                                  (is (= arg :arg) "Fx-guard has recieved correct event payload.")
                                                                  (is (:event cofx) "Fx-handler recieved `cofx` map")
                                                                  (::can-run? (:db cofx))))}})]
             (rf/reg-event-db
              ::db-guard-test-setup
              (fn [db]
                (assoc db ::can-run? true)))

             (rf/dispatch-sync [::db-guard-test-setup])

             (casync/go
               (interpreter-start! interpreter)
               (interpreter-send! interpreter :toggle :arg)
               (is (= (casync/<! c) :done) "Fx guard passed truthy value")
               (done)))))


(deftest ctx-guard-test
  (testing "Ctx guard")
    (async done
           (let [c (casync/timeout 100) ;; If something goes wrong we shouldn't wait too long
                 interpreter (interpreter! {:id :simple-machine
                                            :initial :ready
                                            :states {:ready {:on {:toggle {:target :running
                                                                           :cond :can-run?}}}
                                                     :running {:entry :done}}}

                                           {:actions {:done #(casync/put! c :done)}
                                            :guards {:can-run? (ctx-guard
                                                                (fn [re-ctx & rest]
                                                                  (is (= (count rest) 0) "Ctx guard doesn't recieve event and args, just bare context.")
                                                                  (let [db (rf/get-coeffect re-ctx :db)]
                                                                    (::can-run? db))))}})]
             (rf/reg-event-db
              ::db-guard-test-setup
              (fn [db]
                (assoc db ::can-run? true)))

             (rf/dispatch-sync [::db-guard-test-setup])

             (casync/go
               (interpreter-start! interpreter)
               (interpreter-send! interpreter :toggle :arg)
               (is (= (casync/<! c) :done) "Ctx guard passed truthy value")
               (done)))))
