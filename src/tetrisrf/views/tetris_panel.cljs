(ns tetrisrf.views.tetris-panel
  (:require [tetrisrf.views.game-field :refer [game-field]]
            [tetrisrf.consts :refer [game-keys]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn dispatch-action [e]
  (let [key #(.-key %1)
        shift #(.-shiftKey %1)
        meta #(.-metaKey %1)
        ctrl #(.-ctrlKey %1)
        alt #(.-altKey %1)]))


(defn tetris-panel []
    (reagent/create-class
     {:display-name "Tetris"
      :reagent-render (fn []
                       [:div
                        {:style {:width "100%"
                                 :height "100%"
                                 :display :flex
                                 :flex-wrap :nowrap
                                 :box-sizing :border-box
                                 :justify-content :center
                                 :align-items :center
                                 :border "0.1em solid orange"}
                         :tab-index -1
                         :on-key-down (fn [e]
                                        (rf/dispatch [:key-down (.-key e)]))}
                        [game-field]])
      :component-did-mount (fn [cmp]
                            (let [node (reagent/dom-node cmp)]
                              (.focus node)))}))
