(ns ^:figwheel-hooks tetrisrf.core
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [tetrisrf.effects]
            [tetrisrf.subscriptions]
            [tetrisrf.views.tetris-panel :refer [tetris-panel]]))


(defn ^:after-load -main []
  (reagent/render [:div
                   [:h1.ui.blue.header.centered "Tetris example."]
                   [tetris-panel]]
                  (.getElementById js/document "app")))


(.addEventListener js/window "load" (fn [] (-main)))
