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

(defn dispose-timer [t]
  (.dispose t))
