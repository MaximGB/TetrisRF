(ns tetrisrf.db
  (:require [re-frame.core :as rf]
            [tetrisrf.timer :refer [create-timer]]
            [tetrisrf.consts :refer [default-timer-interval]]))

(def empty-field {:width  10
                  :height 20
                  :cell-size [10 10]
                  :tetromino-prev nil
                  :tetromino nil
                  :cells []})

(def initial-db {:field empty-field
                 :redrawn true
                 :timer (create-timer default-timer-interval (fn [] (rf/dispatch [:tick])))})
