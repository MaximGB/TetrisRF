(ns tetrisrf.views.game-over-modal
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn game-over-modal []
  (reagent/create-class
   {:display-name "Game over message"
    :reagent-render (fn []
                      [:div.ui.dimmer.active
                       [:div.ui.card
                        [:div.content
                         [:div.header "Game over :("]
                         [:div.description "Your score is " [:span.card-score @(rf/subscribe [:score])] " points!"]]
                        [:div#game-over-button.ui.bottom.attached.large.green.button
                         {:tab-index -1
                          :on-click (fn [] (rf/dispatch [:game-over-accept]))}
                         "Ok"]]])
    :component-did-mount (fn []
                           (let [button (.getElementById js/document "game-over-button")]
                             (.focus button)))}))
