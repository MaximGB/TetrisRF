(ns tetrisrf.effects
  (:require [re-frame.core :as rf]
            [tetrisrf.timer :refer [start-timer stop-timer]]))


(rf/reg-fx
 :start-timer
 (fn [timer]
   (start-timer timer)))


(rf/reg-fx
 :stop-timer
 (fn [timer]
   (stop-timer timer)))
