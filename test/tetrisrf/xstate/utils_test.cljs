(ns tetrisrf.xstate.utils-test
  (:require [cljs.test :refer [deftest is testing async use-fixtures]]
            [tetrisrf.xstate.core :refer [db-action]]
            [tetrisrf.xstate.utils :refer [prepare-machine-config
                                           prepare-machine-options
                                           machine-config->actions-interceptors
                                           machine-options->actions-interceptors]]))

(def test-machine-config {:id :test-2
                          :initial :ready
                          :states {:ready {:entry [:ready-entry]
                                           :exit (db-action
                                                  [:test-coeffect-1 :test-coeffect-2]
                                                  (fn [db]))
                                           :on {:toggle {:target :running
                                                         :actions [:ready-to-running (db-action
                                                                                      [:test-coeffect-3]
                                                                                      (fn [db]))]}}}
                                   :running {:initial :fast
                                             :states {:fast {:entry :in-running-fast
                                                             :initial :very
                                                             :states {:very {:exit :out-running-fast
                                                                             :on {:stop {:target :ready
                                                                                         :actions [(db-action
                                                                                                    [:test-coeffect-4]
                                                                                                      (fn [db]))]}}}}}}}}})

(def test-machine-options {:actions {:action-1 (db-action
                                                [:test-coeffect-1]
                                                (fn [db]))
                                     :action-2 (db-action
                                                [:test-coeffect-1 :test-coeffect-2]
                                                (fn [db]))}})


(deftest machine-config-meta-extraction-test
  (testing "Actions metadata extraction from machine config"
    (let [meta (machine-config->actions-interceptors test-machine-config)
          interceptors (into #{} (flatten (vals meta)))]
      (is (= interceptors #{:test-coeffect-1 :test-coeffect-2 :test-coeffect-3 :test-coeffect-4}) "Config actions interceptors collected"))))


(comment
  (require '[com.rpl.specter :as s])

  (def ACTIONS (s/recursive-path [] p [:states s/MAP-VALS (s/multi-path [:on s/MAP-VALS :actions s/ALL]
                                                                        (s/must :entry)
                                                                        (s/must :exit)
                                                                        p)]))

  (cljs.pprint/pprint "--------------------------------------")
  (cljs.pprint/pprint (s/select ACTIONS {})))
