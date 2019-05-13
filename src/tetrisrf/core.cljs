(ns ^:figwheel-hooks tetrisrf.core
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [tetrisrf.effects]
            [tetrisrf.subscriptions]
            [tetrisrf.handlers]
            [tetrisrf.views.messages :refer [messages]]
            [tetrisrf.views.tetris-panel :refer [tetris-panel]]))

(defn ^:after-load -main []
  (rf/dispatch-sync [:initialize-db])
  (reagent/render [:div [tetris-panel] [messages]] (.getElementById js/document "app")))


(.addEventListener js/window "load" (fn [] (-main)))
