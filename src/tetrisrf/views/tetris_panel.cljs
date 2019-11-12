(ns tetrisrf.views.tetris-panel
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [tetrisrf.consts :refer [game-keys]]
            [tetrisrf.views.game-field :refer [game-field]]
            [tetrisrf.views.level-panel :refer [level-panel]]
            [tetrisrf.views.next-panel :refer [next-panel]]
            [tetrisrf.views.score-panel :refer [score-panel]]
            [tetrisrf.machines.tetris-machine :refer [machine]]
            [tetrisrf.xstate.core :as xs]))


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


(defn dispatch-key-event [tm e game-keys]
  (let [key (build-key e)
        event (get game-keys key)]
    (if event
      (xs/interpreter-send! tm event))))


(defn tetris-panel
  ([]
   (tetris-panel (gensym "tetris-panel-")))
  ([db-path]
   (let [itm (xs/interpreter-start! (xs/interpreter! db-path machine))
         state-sub (xs/isubscribe-state itm)]
     (reagent/create-class
      {:display-name "Tetris"

       :reagent-render (fn []
                         [:div.tetris-panel.ui.four.column.centered.grid
                          {:style {:outline :none}
                           :tab-index -1
                           :on-key-down (fn [e]
                                          (.preventDefault e)
                                          (dispatch-key-event itm e game-keys))}
                          (case @state-sub
                            :playing
                            [:div.row
                             [:div.column.two.wide
                              {:style {:display :flex
                                       :justify-content :center}}
                              [:div.ui.raised.segment.field-frame
                               [game-field (rf/subscribe [:tetrisrf.core/field itm])]]]
                             [:div.column.two.wide
                              [score-panel (rf/subscribe [:tetrisrf.core/score itm])]
                              [level-panel (rf/subscribe [:tetrisrf.core/level itm])]
                              [next-panel  (rf/subscribe [:tetrisrf.core/next-tetramino-field itm])]]]

                            :game-over
                            [:div.row
                             [:div.column.four.wide.middle.aligned
                              "Game Over! Press Enter to restart."]]

                            ;;default
                            [:div.row
                             [:div.column.four.wide.middle.aligned
                              "Press entrer to start playing."]])])

       :component-did-mount (fn [cmp]
                              (let [node (reagent/dom-node cmp)]
                                (.focus node)))}))))
