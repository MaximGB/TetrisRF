(ns tetrisrf.actions-tests
  (:require [cljs.test :refer [deftest is testing] :include-macros true]
            [tetrisrf.actions
             :refer
             [blend-tetramino
              calc-score
              field-complete-lines-count
              field-remove-complete-lines
              move-down
              move-left
              move-right
              place-tetramino
              place-tetramino-centered
              rotate-90ccw
              rotate-90cw
              validate-field]]
            [tetrisrf.db :refer [make-empty-field]]))

(deftest calc-score-by-lines-test
  (is (< (calc-score 0 1)
         (calc-score 0 2)
         (calc-score 0 3)
         (calc-score 0 4))
      "Player gets more score with more amount of lines removed"))


;; [*][*]
(def test-tetramino {:cells  [[0 0 1] [1 0 1]]
                     :pivot  [0.5 0.5 1]
                     :width  2
                     :height 1})


(deftest place-tetramino-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 0 0)
        tetramino (:tetramino field)
        x (:x tetramino)
        y (:y tetramino)]
    (is (= [x y] [0 0])
        "Tetramino initial coordiates should be as passed")
    (is (= (:cells tetramino) (:cells test-tetramino))
        "Since tetramino is placed at [0 0] it's cells should be untoched")))


(deftest place-tetramino-centered-test
  (testing "Horizontal centering"
    (let [field (place-tetramino-centered (make-empty-field 6 4) test-tetramino :center-h true)
          tetramino (:tetramino field)
          x (:x tetramino)
          y (:y tetramino)]
      (is (= x 2)
          "Tetramino is horizontaly centered")
      (is (= y 0)
          "Tetramino vertical coordinate is untouched (0)")))
  (testing "Vertical centering"
    (let [field (place-tetramino-centered (make-empty-field 6 4) test-tetramino :center-h false :center-v true)
          tetramino (:tetramino field)
          x (:x tetramino)
          y (:y tetramino)]
      (is (= x 0)
          "Tetramino horizontal coordinate is untoched (0)")
      (is (= y 1)
          "Tetramino is vertically centered")))
  (testing "Full centering"
    (let [field (place-tetramino-centered (make-empty-field 6 4) test-tetramino :center-h true :center-v true)
          tetramino (:tetramino field)
          x (:x tetramino)
          y (:y tetramino)]
      (is (= x 2)
          "Tetramino is horizontaly centered")
      (is (= y 1)
          "Tetramino is vertically centered"))))


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


(deftest rotate-90cw-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)
        field-w-rotation (rotate-90cw field)
        tetramino (:tetramino field-w-rotation)
        x (:x tetramino)
        y (:y tetramino)
        cells (:cells tetramino)]
    (is (= x 1)
        "Rotation doesn't change tetramino coordinates")
    (is (= y 1)
        "Rotation doesn't change tetramino coordinates")
    (is (= cells
           [[2 1 1][2 2 1]])
        "Rotation change tetramino cells' coordinates, they are rotated alongisde pivot point")))


(deftest rotate-90ccw-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)
        field-w-rotation (rotate-90ccw field)
        tetramino (:tetramino field-w-rotation)
        x (:x tetramino)
        y (:y tetramino)
        cells (:cells tetramino)]
    (is (= x 1)
        "Rotation doesn't change tetramino coordinates")
    (is (= y 1)
        "Rotation doesn't change tetramino coordinates")
    (is (= cells
           [[1 2 1][1 1 1]])
        "Rotation change tetramino cells' coordinates, they are rotated alongisde pivot point")))


(deftest rotate-360cw-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)
        field-w-rotation (nth (iterate rotate-90cw field) 4)
        initial-tetramino (:tetramino field)
        rotated-tetramino (:tetramino field-w-rotation)]
    (is (= initial-tetramino
           rotated-tetramino)
        "Full rotation doesn't change tetramino")))


(deftest rotate-360ccw-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)
        field-w-rotation (nth (iterate rotate-90ccw field) 4)
        initial-tetramino (:tetramino field)
        rotated-tetramino (:tetramino field-w-rotation)]
    (is (= initial-tetramino
           rotated-tetramino)
        "Full rotation doesn't change tetramino")))


(deftest blend-tetramino-test
  (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 3)
        field-blended (blend-tetramino field)
        tetramino-cells (get-in field [:tetramino :cells])
        field-cells (:cells field-blended)]
    (is (= tetramino-cells
           field-cells)
        "After blend tetramino cells should became field cells")))


(deftest validate-field-test
  (testing "Normal case"
    (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 1 1)]
      (is (= (validate-field field)
             true)
          "Valid case detected")))
  (testing "Right overflow"
    (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 3 1)]
      (is (= (validate-field field)
             false)
          "Right overflow detected")))
  (testing "Left overflow"
    (let [field (place-tetramino (make-empty-field 3 4) test-tetramino -1 1)]
      (is (= (validate-field field)
             false)
          "Left overflow detected")))
  (testing "Bottom overflow"
    (let [field (place-tetramino (make-empty-field 3 4) test-tetramino 4 1)]
      (is (= (validate-field field)
             false)
          "Bottom overflow detected")))
  (testing "Cells hit"
    (let [field (-> (make-empty-field 3 4)
                    (place-tetramino test-tetramino 1 1)
                    (blend-tetramino)
                    (place-tetramino test-tetramino 2 1))]
      (is (= (validate-field field)
             false)
          "Cells hit detected"))))


(deftest field-complete-lines-count-test
  (testing "0 complete lines count case"
    (let [field (make-empty-field 3 4)]
      (is (= (field-complete-lines-count field)
             0)
          "No complete lines found")))
  (testing "1 complete line count case"
    (let [field (-> (make-empty-field 2 4)
                    (place-tetramino test-tetramino 0 1)
                    (blend-tetramino))]
      (is (= (field-complete-lines-count field)
             1)
          "1 complete line found")))
  (testing "> 1 complete lines count case"
    (let [field (-> (make-empty-field 2 4)
                    (place-tetramino test-tetramino 0 3)
                    (blend-tetramino)
                    (place-tetramino test-tetramino 0 2)
                    (blend-tetramino)
                    (place-tetramino test-tetramino 0 1)
                    (blend-tetramino))]
      (is (= (field-complete-lines-count field)
             3)
          "3 complete lines found"))))


(deftest field-remove-complete-lines-test
  (testing "0 complete lines removal"
    (let [field (-> (make-empty-field 4 4)
                    (place-tetramino test-tetramino 1 2)
                    (blend-tetramino))
          field0 (field-remove-complete-lines field)]
      (is (= field
             field0)
          "Field is untoched by complete lines removal code if no complete lines present")))
  (testing "1 complete line removal"
    (let [field (-> (make-empty-field 2 4)
                    (place-tetramino test-tetramino 0 1)
                    (blend-tetramino))
          complete-lines-initial (field-complete-lines-count field)
          field0 (field-remove-complete-lines field)]
      (is (= complete-lines-initial
             1)
          "1 complete line has been found before removal")
      (is (= (field-complete-lines-count field0)
             0)
          "No complete lines has been found after removal")))
  (testing "2 complete lines removal"
    (let [field (-> (make-empty-field 2 4)
                    (place-tetramino test-tetramino 0 3)
                    (blend-tetramino)
                    (place-tetramino test-tetramino 0 2)
                    (blend-tetramino))
          complete-lines-initial (field-complete-lines-count field)
          field0 (field-remove-complete-lines field)]
      (is (= complete-lines-initial
             2)
          "2 complete lines has been found before removal")
      (is (= (field-complete-lines-count field0)
             0)
          "No complete lines has been found after removal")))
  (testing "> 2 complete lines removal"
    (let [field (-> (make-empty-field 2 4)
                    (place-tetramino test-tetramino 0 3)
                    (blend-tetramino)
                    (place-tetramino test-tetramino 0 2)
                    (blend-tetramino)
                    (place-tetramino test-tetramino 0 0)
                    (blend-tetramino))
          complete-lines-initial (field-complete-lines-count field)
          field0 (field-remove-complete-lines field)]
      (is (= complete-lines-initial
             3)
          "3 complete lines has been found before removal")
      (is (= (field-complete-lines-count field0)
             0)
          "No complete lines has been found after removal"))))
