(ns tetrisrf.views.next-panel
  (:require [re-frame.core :as rf]
            [tetrisrf.views.game-field :refer [game-field]]))

(defn next-panel []
  [:div.ui.labeled.big.input.next-field
   [:div.ui.label.big "Next"]
   [:div.field-frame
    [game-field (rf/subscribe [:next-tetramino-field])]]])
