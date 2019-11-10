(ns tetrisrf.timer
  (:require [goog.Timer]
            [re-frame.core :as rf]))


(declare set-timer-interval
         set-timer-tick-handler)


(defn create-timer [& [ms tick-handler]]
  "Creates timer with the given `ms` and optional `tick-handler`."
  (let [t (goog.Timer. ms)]
    (if (not (nil? ms))
      (set-timer-interval t ms))
    (if tick-handler
      (set-timer-tick-handler t tick-handler))
    t))


(defn start-timer [t]
  "Starts the timer."
  (.start t))


(defn stop-timer [t]
  "Stops the timer."
  (.stop t))


(defn set-timer-interval [t i]
  "Sets timer interval."
  (.setInterval t i))


(defn set-timer-tick-handler [t tick-handler]
  "Sets timer tick handler."
  (.removeAllListeners t)
  (.listen t goog.Timer.TICK tick-handler))


(defn dispose-timer [t]
  "Disposes timer."
  (.dispose t))
