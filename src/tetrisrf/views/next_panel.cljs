(ns tetrisrf.views.next-panel
  (:require [re-frame.core :as rf]
            [tetrisrf.views.game-field :refer [game-field]]))

(defn next-panel [next-field-sub]
  [:div.next-field.fluid.ui.labeled.big.input
   [:div.ui.label.big "Next"]
   [:div.field-frame
    [game-field next-field-sub]]])
