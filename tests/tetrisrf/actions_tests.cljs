(ns tetrisrf.actions-tests
  (:require [tetrisrf.actions
             :refer
             [calc-score]]
            [cljs.test
             :include-macros true
             :refer
             [deftest is testing run-tests]]))


(deftest calc-score-test
  (is (<
       (calc-score 0 1)
       (calc-score 0 2)
       (calc-score 0 3)
       (calc-score 0 4))
      "Player gets more score with more amount of lines removed"))
