(ns tetrisrf.views.game-field
  (:require [reagent.core :as reagent]
            [quil.core :as q :include-macros true]
            [quil.middleware :as qm]
            [goog.dom :as dom]
            [tetrisrf.color :refer [rgb-darker rgb-lighter]]
            [tetrisrf.consts :refer [color-blue color-red color-white]]))

(defn draw-cell! [[cell-width cell-height] [x y] color]
  (let [ctx-x (* cell-width x)
        ctx-y (* cell-height y)
        lighter-c (rgb-lighter color 0.3)
        darker-c (rgb-darker color 0.3)]
    (q/no-stroke)
    (apply q/fill color)
    (q/rect (inc ctx-x) (inc ctx-y) (- cell-width 2) (- cell-height 2))
    (apply q/stroke darker-c)
    (q/line (+ ctx-x (dec cell-width)) ctx-y (+ ctx-x (dec cell-width)) (+ ctx-y (dec cell-height)))
    (q/line ctx-x (+ ctx-y (dec cell-height)) (+ ctx-x (dec cell-width)) (+ ctx-y (dec cell-height)))
    (apply q/stroke lighter-c)
    (q/line ctx-x ctx-y (+ ctx-x (dec cell-width)) ctx-y)
    (q/line ctx-x ctx-y ctx-x (+ ctx-y (dec cell-height)))))


(defn draw-tetramino!
  ([cellwh tetramino]
   (draw-tetramino! cellwh tetramino color-red))
  ([cellwh tetramino color]
   (doseq [cell (:cells tetramino)]
     (draw-cell! cellwh cell color))))


(defn draw-cells!
  ([cellwh cells]
   (draw-cells! cellwh cells color-blue))
  ([cellwh cells color]
   (doseq [cell cells]
     (draw-cell! cellwh cell color))))


(defn draw-field! [field]
  (let [field-width (:width field)
        field-height (:height field)
        cell-size (:cell-size field)
        tetramino (:tetramino field)]
    ;; Drawing tetramino if present
    (when tetramino
      (draw-tetramino! cell-size
                       tetramino))
    ;; Drawing field cells
    (draw-cells! cell-size (:cells field))))


(defn quil-setup-field! [field-subscription]
  (q/frame-rate 30)
  (q/background 255 255 255)
  {:field field-subscription})


(defn quil-draw-field! [state]
  (let [field (:field state)]
    (q/background 255 255 255)
    (draw-field! @field)
    state)
  #_(print @field-subscription)
  #_(print re-frame.db/app-db)
  #_((q/no-stroke)
   (q/background 255 255 236)
   (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
     (doseq [i (range 1000)]
       (let [v (+ (mod (q/frame-count) 3) i)
             ang (* v PHI q/TWO-PI)
             r   (* (Math/sqrt v) (q/width) (/ 70))
             x   (* (q/cos ang) r)
             y   (* (q/sin ang) r)
             sz  (+ 3 (* i 0.002))]
         (apply q/fill (nth palette i))
         (q/ellipse x y sz sz))))))


(defn game-field [field-subscription]
  (let [canvas-id (str (random-uuid))]
    (reagent/create-class
     {:display-name "Game field"

      :reagent-render (fn []
                        (let [field @field-subscription
                              [cell-w cell-h] (:cell-size field)
                              canvas-width  (* (:width field) cell-w)
                              canvas-height (* (:height field) cell-h)]
                          [:canvas
                           {:id canvas-id
                            :style {:width "100%"
                                    :height "100%"
                                    :border "1px solid black"} ;; TODO: get rid of styling
                            :width canvas-width
                            :height canvas-height}]))

      :component-did-mount (let [field @field-subscription
                                 [cell-w cell-h] (:cell-size field)
                                 canvas-width  (* (:width field) cell-w)
                                 canvas-height (* (:height field) cell-h)]
                             (fn [cmp]
                               (q/sketch :host canvas-id
                                         :size [canvas-width canvas-height]
                                         :middleware [qm/fun-mode]
                                         :setup (fn [] (quil-setup-field! field-subscription))
                                         :draw quil-draw-field!)))

      :component-did-update (fn [cmp]
                              (let [canvas (reagent/dom-node cmp)
                                    field @field-subscription]
                                #_(.requestAnimationFrame js/window
                                                        (fn []
                                                          (draw-field! canvas (:cell-size field) field)))))})))
