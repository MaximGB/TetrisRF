(ns tetrisrf.actions
  (:require [clojure.core.matrix :as matrix]
            [tetrisrf.consts :refer [score-per-line]]
            [tetrisrf.transformations
             :refer
             [transformation-move
              transformation-move-down
              transformation-move-left
              transformation-move-right
              transformation-rotate90ccw
              transformation-rotate90cw]]))

(defn move-left [field]
  (-> field
      (update-in [:tetramino :cells] matrix/mmul (transformation-move-left))
      (update-in [:tetramino :x] dec)))


(defn move-right [field]
  (-> field
      (update-in [:tetramino :cells] matrix/mmul (transformation-move-right))
      (update-in [:tetramino :x] inc)))


(defn move-down [field]
  (-> field
      (update-in [:tetramino :cells] matrix/mmul (transformation-move-down))
      (update-in [:tetramino :y] inc)))

(defn rotate [field rotation-transformation]
  (let [tetramino (:tetramino field)
        x      (:x tetramino)
        y      (:y tetramino)
        width  (:width tetramino)
        height (:height tetramino)
        [pivot-x pivot-y] (:pivot tetramino)
        offset-x (+ x pivot-x)
        offset-y (+ y pivot-y)
        cells (-> (:cells tetramino)
                  (matrix/mmul (transformation-move (- offset-x) (- offset-y)))
                  (matrix/mmul (rotation-transformation))
                  (matrix/mmul (transformation-move offset-x offset-y)))]
    (-> field
        (update :tetramino
                #(conj %1 {:cells  cells
                           :width  height
                           :height width})))))


(defn rotate-90cw [field]
  (rotate field transformation-rotate90cw))


(defn rotate-90ccw [field]
  (rotate field transformation-rotate90ccw))


(defn place-tetramino
  ([field tetramino]
   (let [field-width (:width field)
         tetramino-width (:width tetramino)
         initial-x (quot (- field-width tetramino-width) 2)
         initial-y 0]
     (place-tetramino field tetramino initial-x initial-y)))
  ([field tetramino initial-x initial-y]
   (let [initial-cells (:cells tetramino)]
     (assoc field
            :tetramino (conj tetramino
                             {:cells (matrix/mmul initial-cells (transformation-move initial-x initial-y))
                              :x initial-x
                              :y initial-y})))))


(defn blend-tetramino [field]
  (let [tetramino (:tetramino field)]
    (assoc field
           :tetramino nil
           :cells (into (:cells field) (:cells tetramino)))))


(defn cells-line-complete? [cells field-width line-y]
    (= field-width
       (reduce (fn [cnt [cx cy]]
                 (if (= line-y cy)
                   (inc cnt)
                   cnt))
               0
               cells)))


(defn cells-complete-line-count [cells field-width field-height]
  (reduce (fn [cnt line-y]
            (if (cells-line-complete? cells field-width line-y)
              (inc cnt)
              cnt))
          0
          (range 0 field-height)))


(defn cells-clear-line [cells clear-line-y]
  (filterv (fn [[_ y]]
             (not= y clear-line-y))
           cells))


(defn cells-v-shift
  ([cells cut-line-y]
   (cells-v-shift cells cut-line-y 1))
  ([cells cut-line-y shift-by]
   (reduce (fn [new-cells [x y]]
             (conj new-cells [x (if (< y cut-line-y) (+ y shift-by) y)]))
           []
           cells)))


(defn cells-first-complete-line-index
  ([cells field-width field-height]
   (cells-first-complete-line-index cells field-width field-height 0))
  ([cells field-width field-height start-y]
   (if (< start-y field-height)
     (if (cells-line-complete? cells field-width start-y)
       start-y
       (recur cells field-width field-height (inc start-y)))
     nil)
   ))


(defn cells-first-incomplete-line-index
  ([cells field-width field-height]
   (cells-first-incomplete-line-index cells field-width field-height 0))
  ([cells field-width field-height start-y]
   (if (< start-y field-height)
     (if (not (cells-line-complete? cells field-width start-y))
       start-y
       (recur cells field-width field-height (inc start-y)))
     nil)
   ))


(defn cells-remove-complete-lines
  ([cells field-width field-height]
   (cells-remove-complete-lines cells field-width field-height 0))
  ([cells field-width field-height start-y]
   ;; 1. Find first complete line from current position (which is 0 by default)
   ;; 2. Move down until first non empty line or line after the end of field, counting complete lines
   ;; 3. Clear all complete lines found
   ;; 4. Shift all lines above by complete lines count
   ;; 5. If end of field isn't reached repeat starting from current position
   (if (< start-y field-height)
     (let [complete-line-index (cells-first-complete-line-index cells field-width field-height start-y)]
       (if (not (nil? complete-line-index))
         (let [incomplete-line-index (cells-first-incomplete-line-index cells field-width field-height complete-line-index)
               new-start-y (if (nil? incomplete-line-index)
                             field-height
                             incomplete-line-index)
               new-cells (cells-v-shift (reduce (fn [cleared-cells y]
                                                  (cells-clear-line cleared-cells y))
                                                cells
                                                (range complete-line-index new-start-y))
                                        new-start-y
                                        (- new-start-y complete-line-index))]
           (recur new-cells field-width field-height new-start-y))
         cells))
     cells)))


(defn field-complete-lines-count [field]
  (cells-complete-line-count (:cells field) (:width field) (:height field)))


(defn field-remove-complete-lines [field]
  (update field :cells cells-remove-complete-lines (:width field) (:height field)))


(defn validate-field [field]
  (let [field-width (:width field)
        field-height (:height field)
        field-cells (:cells field)
        tetramino (:tetramino field)]
    (if tetramino
      (let [tetramino-cells (:cells tetramino)
            tetramino-cell-min-x (reduce (fn [r [x]]
                                           (if (< r x) r x))
                                         (.-MAX_SAFE_INTEGER js/Number)
                                         tetramino-cells)
            tetramino-cell-max-x (reduce (fn [r [x]]
                                           (if (< r x) x r))
                                         (.-MIN_SAFE_INTEGER js/Number)
                                         tetramino-cells)
            tetramino-cell-max-y (reduce (fn [r [_ y]]
                                           (if (< r y) y r))
                                         (.-MIN_SAFE_INTEGER js/Number)
                                         tetramino-cells)]
        (and
         ;; Tetramino min x is 0 or more
         (<= 0 tetramino-cell-min-x)
         ;; Tetramino max x is less then field width
         (<  tetramino-cell-max-x field-width)
         ;; Tetramino max y is less then field height
         (<  tetramino-cell-max-y field-height)
         ;; Non of tetramino cells intersects with field cells
         (every? (fn [[fx fy]]
                   (every? (fn [[tx ty]]
                             (or (not= fx tx)
                                 (not= fy ty)))
                           tetramino-cells))
                 field-cells)))
      true)))


(defn has-tetramino? [field]
  (not (nil? (:tetramino field))))


(defn can-act? [field action]
  (validate-field (action field)))


(defn calc-score [current-score lines-completed]
  (+ current-score (get lines-completed score-per-line)))
