(ns tetrisrf.actions
  (:require [clojure.core.matrix :as matrix]
            [tetrisrf.transformations
             :refer
             [transformation-move
              transformation-move-down
              transformation-move-left
              transformation-move-right
              transformation-rotate90ccw
              transformation-rotate90cw]]))

(defn save-prev-tetramino [field]
  (assoc field :tetramino-prev (:tetramino field)))


(defn reset-prev-tetramino [field]
  (assoc field :tetramino-prev nil))


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
            :tetramino-prev nil
            :tetramino (conj tetramino
                             {:cells (matrix/mmul initial-cells (transformation-move initial-x initial-y))
                              :x initial-x
                              :y initial-y})))))


(defn blend-tetramino [field]
  (let [tetramino (:tetramino field)]
    (assoc field
           :tetramino-prev :tetramino
           :tetramino nil
           :cells (into (:cells field) (:cells tetramino) ))))


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


(defn can-act? [field action]
  (validate-field (action field)))
