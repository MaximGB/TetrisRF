(ns tetrisrf.transformations)

;; Transformations

;; Move matrix
;; [1 0 0]
;; [0 1 0]
;; [x y 1]

;; Rotation matrix
;; [ cos Q  sin Q  0]
;; [-sin Q  cos Q  0]
;; [  0      0     1]

(defn transformation-identity []
  [[1 0 0]
   [0 1 0]
   [0 0 1]])

(defn transformation-move [ox oy]
  [[ 1  0  0]
   [ 0  1  0]
   [ox oy  1]])

(defn transformation-move-down
  ([] (transformation-move 0 1))
  ([steps] (transformation-move 0 steps)))

(defn transformation-move-left
  ([] (transformation-move-left 1))
  ([steps]
   [[     1    0 0]
    [     0    1 0]
    [(- steps) 0 1]]))

(defn transformation-move-right
  ([] (transformation-move-right 1))
  ([steps]
   [[  1   0 0]
    [  0   1 0]
    [steps 0 1]]))

(defn transformation-rotate90cw []
  [[ 0  1  0]
   [-1  0  0]
   [ 0  0  1]])

(defn transformation-rotate90ccw []
  [[ 0 -1  0]
   [ 1  0  0]
   [ 0  0  1]])
