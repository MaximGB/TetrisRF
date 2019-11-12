(ns tetrisrf.views.score-panel
  (:require [re-frame.core :as rf]))

(defn score-panel [score-sub]
    [:div.ui.fluid.labeled.big.input
     [:div.ui.label "Score"]
     [:input {:readOnly :readOnly
              :tab-index -1
              :value @score-sub}]])
