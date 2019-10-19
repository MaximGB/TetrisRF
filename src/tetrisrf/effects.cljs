(ns tetrisrf.effects
  (:require [re-frame.core :as rf]
            [tetrisrf.timer :refer [set-timer-interval start-timer stop-timer]]))

(rf/reg-fx
 :start-timer
 (fn [timer]
   (start-timer timer)))


(rf/reg-fx
 :stop-timer
 (fn [timer]
   (stop-timer timer)))

(rf/reg-fx
 :set-timer
 (fn [[timer interval]]
   (set-timer-interval timer interval)))

(rf/reg-fx
 :focus
 (fn [element-id]
   (->(.getElementById js/document element-id)
      (.focus))))

(rf/reg-fx
 :xsend
 (fn [[xservice event]]
   (.send xservice (name event))))
