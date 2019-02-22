(ns tetrisrf.views.tetris-panel
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [tetrisrf.consts :refer [game-keys]]
            [tetrisrf.views.game-field :refer [game-field]]
            [tetrisrf.views.score-panel :refer [score-panel]]
            [tetrisrf.views.level-panel :refer [level-panel]]))

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
                        [:div.ui.three.column.grid.container
                         {:style {:outline :none}
                          :tab-index -1
                          :on-key-down (fn [e]
                                         (.preventDefault e)
                                         (dispatch-key-action e game-keys))}
                         [:div.column.sixteen.wide
                          [:h1.ui.header.center.aligned "Tetris"]]
                         [:div.row
                          [:div.column.six.wide]
                          [:div.column.four.wide
                           {:style {:display :flex
                                    :justify-content :center}}
                            [game-field]]
                          [:div.column.six.wide
                           [score-panel]
                           [level-panel]]]])
      :component-did-mount (fn [cmp]
                            (let [node (reagent/dom-node cmp)]
                              (.focus node)))}))
