(ns tetrisrf.views.level-panel
  (:require [re-frame.core :as rf]))

(defn level-panel []
  (let [level (rf/subscribe [:level])]
    [:div.ui.labeled.big.input
     [:div.ui.label "Level"]
     [:input {:readOnly :readOnly
              :tab-index -1
              :value @level}]]))