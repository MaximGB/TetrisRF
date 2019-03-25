(ns ^:figwheel-hooks tetrisrf.core
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [tetrisrf.subscriptions]
            [tetrisrf.handlers]
            [tetrisrf.effects]
            [tetrisrf.views.game-over-modal :refer [game-over-modal]]
            [tetrisrf.views.tetris-panel :refer [tetris-panel]]))


(defn ^:after-load -main []
  (rf/dispatch-sync [:initialize-db])
  (reagent/render [:div [tetris-panel] [game-over-modal]] (.getElementById js/document "app")))


(.addEventListener js/window "load" (fn [] (-main)))
