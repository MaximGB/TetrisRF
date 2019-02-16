(ns tetrisrf.tetraminos)

;; Coordinate system
;; 0,0
;;  +------>
;;  |
;;  |
;;  V

;; Tetraminos
;; http://tetris.wikia.com/wiki/SRS?file=SRS-pieces.png

;; [*][*][*][*]
(def tetramino-i {:cells  [[0 0 1] [1 0 1] [2 0 1] [3 0 1]]
                  :pivot  [1.5 0.5 1]
                  :width  4
                  :height 1})

;; [*]
;; [*][*][*]
(def tetramino-j {:cells  [[0 0 1] [0 1 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;;       [*]
;; [*][*][*]
(def tetramino-l {:cells  [[2 0 1] [0 1 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;; [*][*]
;; [*][*]
(def tetramino-o {:cells  [[0 0 1] [1 0 1] [0 1 1] [1 1 1]]
                   :pivot  [0.5 0.5 1]
                   :width  2
                   :height 2})

;;    [*][*]
;; [*][*]
(def tetramino-s {:cells  [[0 1 1] [1 1 1] [1 0 1] [2 0 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;;    [*]
;; [*][*][*]
(def tetramino-t {:cells  [[1 0 1] [0 1 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

;; [*][*]
;;    [*][*]
(def tetramino-z {:cells  [[0 0 1] [1 0 1] [1 1 1] [2 1 1]]
                  :pivot  [1 1 1]
                  :width  3
                  :height 2})

(def tetraminos [tetramino-i
                 tetramino-l
                 tetramino-o
                 tetramino-j
                 tetramino-s
                 tetramino-t
                 tetramino-z])
