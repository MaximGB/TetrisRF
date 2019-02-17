(ns tetrisrf.views.score-panel
  (:require [re-frame.core :as rf]))

(defn score-panel []
  (let [score (rf/subscribe [:score])]
    [:div "Score"
     [:div {:style {:border "1px solid black"}} @score]]))
