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

(defn save-prev-tetromino [field]
  (assoc field :tetromino-prev (:tetromino field)))


(defn reset-prev-tetromino [field]
  (assoc field :tetromino-prev nil))


(defn move-left [field]
  (-> field
      (update-in [:tetromino :cells] matrix/mmul (transformation-move-left))
      (update-in [:tetromino :x] dec)))


(defn move-right [field]
  (-> field
      (update-in [:tetromino :cells] matrix/mmul (transformation-move-right))
      (update-in [:tetromino :x] inc)))


(defn move-down [field]
  (-> field
      (update-in [:tetromino :cells] matrix/mmul (transformation-move-down))
      (update-in [:tetromino :y] inc)))

(defn rotate [field rotation-transformation]
  (let [tetromino (:tetromino field)
        x      (:x tetromino)
        y      (:y tetromino)
        width  (:width tetromino)
        height (:height tetromino)
        [pivot-x pivot-y] (:pivot tetromino)
        offset-x (+ x pivot-x)
        offset-y (+ y pivot-y)
        cells (-> (:cells tetromino)
                  (matrix/mmul (transformation-move (- offset-x) (- offset-y)))
                  (matrix/mmul (rotation-transformation))
                  (matrix/mmul (transformation-move offset-x offset-y)))]
    (-> field
        (update :tetromino
                #(conj %1 {:cells  cells
                           :width  height
                           :height width})))))


(defn rotate-90cw [field]
  (rotate field transformation-rotate90cw))


(defn rotate-90ccw [field]
  (rotate field transformation-rotate90ccw))


(defn place-tetramino
  ([field tetromino]
   (let [field-width (:width field)
         tetromino-width (:width tetromino)
         initial-x (quot (- field-width tetromino-width) 2)
         initial-y 0]
     (place-tetramino field tetromino initial-x initial-y)))
  ([field tetromino initial-x initial-y]
   (let [initial-cells (:cells tetromino)]
     (-> field
         (assoc :tetromino-prev nil)
         (assoc :tetromino
                (conj tetromino
                      {:cells (matrix/mmul initial-cells (transformation-move initial-x initial-y))
                       :x initial-x
                       :y initial-y}))))))


(defn validate-field [field]
  (let [field-width (:width field)
        field-height (:height field)
        field-cells (:cells field)
        tetromino-cells (get-in field [:tetromino :cells])
        tetromino-cell-min-x (reduce (fn [r, [x]]
                                       (if (< r x) r x))
                                     (.-MAX_SAFE_INTEGER js/Number)
                                     tetromino-cells)
        tetromino-cell-max-x (reduce (fn [r, [x]]
                                       (if (< r x) x r))
                                     (.-MIN_SAFE_INTEGER js/Number)
                                     tetromino-cells)
        tetromino-cell-max-y (reduce (fn [r, [_, y]]
                                       (if (< r y) y r))
                                     (.-MIN_SAFE_INTEGER js/Number)
                                     tetromino-cells)]
    (and
     ; Tetromino min x is 0 or more
     (<= 0 tetromino-cell-min-x)
     ; Tetromino max x is less then field width
     (<  tetromino-cell-max-x field-width)
     ; Tetromino max y is less then field height
     (<  tetromino-cell-max-y field-height)
     ; Non of tetromino cells intersects with field cells
     (every? (fn [[fx fy]]
               (every? (fn [[tx ty]]
                         (print "[" tx "," ty "] - [" fx "," fy "]")
                         (and (not= fx tx)
                              (not= fy ty)))
                       tetromino-cells))
             field-cells))))


(defn can-act? [field action]
  (validate-field (action field)))
