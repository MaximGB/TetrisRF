(ns tetrisrf.db
  (:require [re-frame.core :as rf]
            [tetrisrf.consts
             :refer
             [default-timer-interval
              field-cell-height
              field-cell-width
              first-level-score]]
            [tetrisrf.timer :refer [create-timer]]))

(defn make-empty-field
  ([]
   (make-empty-field 10 20 [field-cell-width field-cell-height]))
  ([width height]
   (make-empty-field width height [field-cell-width field-cell-height]))
  ([width height cell-size]
   {:width width
    :height height
    :cell-size cell-size
    :tetramino nil
    :cells []}))


(defn make-next-tetramino-field
  ([]
   (make-next-tetramino-field nil))
  ([tetramino]
   (let [field (make-empty-field 6 6)]
     (if tetramino
       (assoc field :tetramino tetramino)
       field))))


(def initial-db {:field (make-empty-field)
                 :next-tetramino-field (make-next-tetramino-field)
                 :next-tetramino nil
                 :running false
                 :game-over false
                 :level 0
                 :score 0
                 :next-level-score first-level-score
                 :timer-interval default-timer-interval
                 :timer (create-timer default-timer-interval (fn [] (rf/dispatch [:tick])))})
