(ns tetrisrf.views.tetris-panel
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [tetrisrf.consts :refer [game-keys]]
            [tetrisrf.views.game-field :refer [game-field]]
            [tetrisrf.views.score-panel :refer [score-panel]]))

(defn build-key [e]
  (let [key-template {:key #(.-key %1)
                      :shift #(.-shiftKey %1)
                      :meta #(.-metaKey %1)
                      :ctrl #(.-ctrlKey %1)
                      :alt #(.-altKey %1)}]
    (reduce-kv (fn [key-result key probe-fn]
                 (let [probe-result (probe-fn e)]
                   (cond
                     (= key :key) (conj key-result probe-result)
                     probe-result (conj key-result key)
                     :else key-result)))
               #{}
               key-template)))


(defn dispatch-key-action [e keys]
  (let [key (build-key e)
        action (get keys key)]
    (if action
      (rf/dispatch [action]))))


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
                                        (dispatch-key-action e game-keys))}
                        [game-field]
                        [score-panel]])
      :component-did-mount (fn [cmp]
                            (let [node (reagent/dom-node cmp)]
                              (.focus node)))}))
