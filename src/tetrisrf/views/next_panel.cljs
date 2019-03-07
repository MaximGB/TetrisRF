(ns tetrisrf.views.next-panel
  (:require [re-frame.core :as rf]
            [tetrisrf.views.game-field :refer [game-field]]))

(defn next-panel []
  [:div.ui.labeled.big.input
   [:div.ui.label "Next"]
   [:div {:style {:width "5em" :height "5em"}}
    [game-field (rf/subscribe [:next-tetramino-field])]]])
