(ns tetrisrf.views.game-over-modal)

(defn game-over-modal []
  [:div.ui.dimmer;;.active
   [:div.ui.modal.message.small;;.active
    [:i.close.icon]
    [:div.header "Message"]
    [:div.content
     [:p "Message text"]]
    [:div.actions
     [:div.ui.positive.large.button "OK"]]]])
