(ns tetrisrf.re-service.dsl-test
  (:require [cljs.test :refer [deftest is testing async use-fixtures]]
            [cljs.core.async :as casync]
            [re-frame.core :as rf]
            [tetrisrf.re-service.core :refer [def-re-service
                                              def-re-service-command
                                              def-re-service-command-raw]
                                      :include-macros true]))


(def rf-checkpoint (volatile! nil))

(use-fixtures
  :once
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


(deftest service-definition-test
  (testing "Definition of a servce and commands, the test only defines a service, real testing goes in other tests"

    (def-re-service ::my-service)

    (def-re-service-command ::my-service
                            ::my-sum
                            [& args]
                            (apply + args))

    (def-re-service-command-raw ::my-service
                                ::my-sum-raw
                                [service-id command-id & args]
                                {[service-id command-id] (apply + args)})))


(deftest service-command-invokation
  (testing "Service command invokation"
    (async done

           (let [c (casync/timeout 100)]

             (rf/reg-event-fx
              ::my-event
              [(rf/inject-cofx ::my-service [::as-my-key ::my-sum [1 2 3 4 5]])]
              (fn [cofx]
                (casync/put! c (::as-my-key cofx))))

             (rf/reg-event-fx
              ::my-event-raw
              [(rf/inject-cofx ::my-service [::as-my-key ::my-sum-raw [1 2 3 4 5]])]
              (fn [cofx]
                (casync/put! c (::as-my-key cofx))))

             (casync/go

               (rf/dispatch [::my-event])

               (is (= (casync/<! c) {::my-sum 15})
                   "Service command invoked via co-effect injection, correct sum calculated")

               (rf/dispatch [::my-event-raw])

               (is (= (casync/<! c) {::my-sum-raw {[::my-service ::my-sum-raw] 15}})
                   "Raw service command invoked via co-effect injection, correct sum calculated")

               (done))))))
