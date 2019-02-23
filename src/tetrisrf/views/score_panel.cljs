(ns tetrisrf.views.score-panel
  (:require [re-frame.core :as rf]))

(defn score-panel []
  (let [score (rf/subscribe [:score])]
    [:div.ui.labeled.big.input
     [:div.ui.label "Score"]
     [:input {:readOnly :readOnly
              :tab-index -1
              :value @score}]]))
