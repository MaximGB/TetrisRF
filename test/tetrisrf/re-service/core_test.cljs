(ns tetrisrf.re-service.core-test
  (:require [cljs.test :refer [deftest is testing async use-fixtures]]
            [cljs.core.async :as casync]
            [re-frame.core :as rf]
            [tetrisrf.re-service.core :refer [exec-service-command
                                              register-service]]))


(def rf-checkpoint (volatile! nil))

(use-fixtures
  :each
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


(deftest re-service-test
  (testing "Re-service feature service registration and implementation"
    (async done

           (let [c (casync/timeout 100)]

             (register-service ::test-service)


             (defmethod exec-service-command [::test-service ::command-1] [_ _ & args]
               (casync/put! c args)
               ::command-1-result)


             (defmethod exec-service-command [::test-service ::command-2] [_ _ & args]
               (casync/put! c (reverse args))
               ::command-2-result)


             (defmethod exec-service-command [::test-service ::command-3] [_ _ result]
               (casync/put! c result))


             (rf/reg-event-fx
              ::test-event
              [(rf/inject-cofx ::test-service [::test-result ::command-1 [1 2 3] ::command-2 [3 2 1]])]
              (fn [cofx]
                (casync/put! c (::test-result cofx))
                {::test-service [::command-3 [(::test-result cofx)]]}))

             (casync/go
               (rf/dispatch [::test-event])

               (is (= (casync/<! c) [1 2 3]) "First command executed correctly")
               (is (= (casync/<! c) [1 2 3]) "Second command executed correctly")
               (is (= (casync/<! c) {::command-1 ::command-1-result
                                     ::command-2 ::command-2-result}))

               (done))))))
