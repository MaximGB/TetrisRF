(ns tetrisrf.views.game-over-modal
  (:require [re-frame.core :as rf]
            [tetrisrf.views.game-field :refer [game-field]]))

(defn game-over-modal []
  [:div.ui.dimmer.active
    [:div.ui.card
     [:div.content
      [:div.header "Game over :("]
      [:div.description "Your score is " [:span.card-score @(rf/subscribe [:score])] " points!"]]
     [:div.ui.bottom.attached.large.green.button
      "Ok"]]])

(comment

(reagent/create-class
 {:display-name "Game over message"
  :reagent-render (fn [])

)
