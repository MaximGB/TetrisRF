(ns tetrisrf.actions-tests
  (:require [cljs.test :refer [deftest is] :include-macros true]
            [tetrisrf.actions
             :refer
             [calc-score move-down move-left move-right place-tetramino]]
            [tetrisrf.db :refer [make-empty-field]]))

(deftest calc-score-by-lines-test
  (is (< (calc-score 0 1)
         (calc-score 0 2)
         (calc-score 0 3)
         (calc-score 0 4))
      "Player gets more score with more amount of lines removed"))


(def test-tetramino {:cells  [[0 0 1] [1 0 1]]
                     :pivot  [0.5 0.5 1]
                     :width  2
                     :height 2})


(deftest place-tetramino-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 0 0)
        tetramino (:tetramino field)
        x (:x tetramino)
        y (:y tetramino)]
    (is (= [x y] [0 0])
        "Tetramino initial coordiates should be as passed")
    (is (= (:cells tetramino) (:cells test-tetramino))
        "Since tetramino is placed at [0 0] it's cells should be untoched")))


(deftest move-left-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)
        field-w-move (move-left field)
        tetramino (:tetramino field-w-move)
        x (:x tetramino)
        y (:y tetramino)]
    (is (= x 0)
        "X coordinate should be less then initial by 1")
    (is (= y 1)
        "Y coordinate should be left untoched")
    (is (= (:cells tetramino)
           [[0 1 1] [1 1 1]])
        "Tetramino cells' coordinates should be moved to the left")))


(deftest move-right-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)
        field-w-move (move-right field)
        tetramino (:tetramino field-w-move)
        x (:x tetramino)
        y (:y tetramino)]
    (is (= x 2)
        "X coordinate should be more then initial by 1")
    (is (= y 1)
        "Y coordinate should be left untoched")
    (is (= (:cells tetramino)
           [[2 1 1] [3 1 1]])
        "Tetramino cells' coordinates should be moved to the right")))


(deftest move-down-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)
        field-w-move (move-down field)
        tetramino (:tetramino field-w-move)
        x (:x tetramino)
        y (:y tetramino)]
    (is (= x 1)
        "X coordinate should be left untouched")
    (is (= y 2)
        "Y coordinate should be more then initial by 1")
    (is (= (:cells tetramino)
           [[1 2 1] [2 2 1]])
        "Tetramino cells' coordinates should be moved down")))
