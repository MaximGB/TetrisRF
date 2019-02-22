(ns tetrisrf.timer
  (:require [goog.Timer]))

(defn create-timer [ms tick-handler]
  (let [t (goog.Timer. ms)]
    (.listen t goog.Timer.TICK tick-handler)
    t))

(defn start-timer [t]
  (.start t))

(defn stop-timer [t]
  (.stop t))

(defn set-timer-interval [t i]
  (.setInterval t i))

(defn dispose-timer [t]
  (.dispose t))
