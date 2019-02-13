(ns tetrisrf.tetrominos)

;; Coordinate system
;; 0,0
;;  +------>
;;  |
;;  |
;;  V

;; Tetrominos
;; http://tetris.wikia.com/wiki/SRS?file=SRS-pieces.png

;; [*][*][*][*]
(def tetromino-i {:cells  [[0 0 1] [1 0 1] [2 0 1] [3 0 1]]
                  :pivot  [1.5 0.5 1]
                  :width  4
                  :height 1})

;; [*]
;; [*][*][*]
(def tetromino-j {:cells  [[0 0 1] [0 1 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;;       [*]
;; [*][*][*]
(def tetromino-l {:cells  [[2 0 1] [0 1 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;; [*][*]
;; [*][*]
(def tetromino-o {:cells  [[0 0 1] [1 0 1] [0 1 1] [1 1 1]]
                   :pivot  [0.5 0.5 1]
                   :width  2
                   :height 2})

;;    [*][*]
;; [*][*]
(def tetromino-s {:cells  [[0 1 1] [1 1 1] [1 0 1] [2 0 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;;    [*]
;; [*][*][*]
(def tetromino-t {:cells  [[1 0 1] [0 1 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;; [*][*]
;;    [*][*]
(def tetromino-z {:cells  [[0 0 1] [1 0 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

(def tetrominos [tetromino-i
                 tetromino-l
                 tetromino-o
                 tetromino-j
                 tetromino-s
                 tetromino-t
                 tetromino-z])
