(ns tetrisrf.views.messages
  (:require [re-frame.core :as rf]
            [tetrisrf.views.game-over-modal :refer [game-over-modal]]))

(defn messages []
  (let [game-over @(rf/subscribe [:game-over])]
    (if game-over
      [game-over-modal])))
