(ns tetrisrf.views.game-field
  (:require [tetrisrf.consts :refer [color-blue color-red color-white]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))


(defn draw-cell! [ctx2d [cell-width cell-height] [x y] color]
  (let [ctx-x (* cell-width x)
        ctx-y (* cell-height y)]
   (set! (.-fillStyle ctx2d) color)
   (.fillRect ctx2d ctx-x ctx-y cell-width cell-height)))


(defn draw-tetramino!
  ([ctx2d cellwh tetramino]
   (draw-tetramino! ctx2d cellwh tetramino color-red))
  ([ctx2d cellwh tetramino color]
   (doseq [cell (:cells tetramino)]
     (draw-cell! ctx2d cellwh cell color))))


(defn draw-cells!
  ([ctx2d cellwh cells]
   (draw-cells! ctx2d cellwh cells color-blue))
  ([ctx2d cellwh cells color]
   (doseq [cell cells]
     (draw-cell! ctx2d cellwh cell color))))


(defn draw-field! [canvas cell-size field]
  (let [ctx2d (.getContext canvas "2d")
        field-width (:width field)
        field-height (:height field)
        [cell-width cell-height] cell-size
        tetramino (:tetramino field)]
    ;; Erasing everything
    (set! (.-fillStyle ctx2d) color-white)
    (.fillRect ctx2d 0 0 (* field-width cell-width) (* field-height cell-height))
    ;; Drawing tetramino if present
    (when tetramino
      (draw-tetramino! ctx2d
                       cell-size
                       tetramino))
    ;; Drawing field cells
    (draw-cells! ctx2d cell-size (:cells field))))


(defn game-field []
  (let [field (rf/subscribe [:field])]
    (reagent/create-class
     {:display-name "Game field"

      :reagent-render (fn []
                        (let [[cell-w cell-h] (:cell-size @field)
                              canvas-width  (* (:width @field) cell-w)
                              canvas-height (* (:height @field) cell-h)]
                          [:div
                           {:style {:width "20ex"
                                    :height "40ex"
                                    :display :inline-block
                                    :border "0.3ex solid green"}}
                           [:canvas
                            {:style {:width "100%"
                                     :height "100%"}
                             :width canvas-width
                             :height canvas-height}]]))

      :component-did-update (fn [cmp]
                              (let [node (reagent/dom-node cmp)
                                    canvas (.-firstChild node)]
                                (.requestAnimationFrame js/window
                                                        (fn []
                                                          (draw-field! canvas (:cell-size @field) @field)))))})))