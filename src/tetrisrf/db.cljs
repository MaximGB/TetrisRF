(ns tetrisrf.db
  (:require [re-frame.core :as rf]
            [tetrisrf.consts :refer [default-timer-interval first-level-score]]
            [tetrisrf.timer :refer [create-timer]]))

(def empty-field {:width  10
                  :height 20
                  :cell-size [20 20]
                  :tetramino nil
                  :cells []})

(def initial-db {:field empty-field
                 :runing false
                 :level 0
                 :score 0
                 :next-level-score first-level-score
                 :timer-interval default-timer-interval
                 :timer (create-timer default-timer-interval (fn [] (rf/dispatch [:tick])))})
