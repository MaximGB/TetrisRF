(ns tetrisrf.xstate.subscriptions-test
  (:require [cljs.test :refer [deftest is testing async use-fixtures]]
            [cljs.core.async :as casync]
            [re-frame.core :as rf]
            [tetrisrf.xstate.core :refer [machine
                                          interpreter!
                                          reg-isub
                                          isubscribe
                                          idb-action]]))

(def rf-checkpoint (volatile! nil))

(use-fixtures
  :each
  {:before (fn [] (vreset! rf-checkpoint (rf/make-restore-fn)))
   :after (fn [] (@rf-checkpoint))})


(deftest intrepreter-db-subscription-isolation-test
  (testing "Subscribing to an isolated by interpreter app db part"
    (async done
           (let [c (casync/timeout 100)
                 m (machine {:id :test-machine})
                 i (interpreter! [:a :b :c] m)]

             (rf/reg-event-db
              ::update-db
              (fn [db [_ path value]]
                (assoc-in db path value)))

             (rf/reg-event-db
              ::next
              (fn [db]
                (casync/put! c ::next)
                db))

             (reg-isub
              ::my-sub
              identity)

             (casync/go
               ;; Resetting db
               (rf/dispatch [::update-db nil {}])
               ;; Updating db part under interpreter part manually
               (rf/dispatch [::update-db [:a :b :c] ::my-value])
               ;; Waiting for dispatch to be handled
               (rf/dispatch [::next])
               (casync/<! c)
               ;; Checking
               (is (= @(rf/subscribe [::my-sub i]) ::my-value) "Subscription returned isolated app db part 1")
               (is (= @(isubscribe i) ::my-value) "Subscription returned isolated app db part 2")
               (done))))))
