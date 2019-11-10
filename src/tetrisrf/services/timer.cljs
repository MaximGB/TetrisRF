(ns tetrisrf.services.timer
  (:require [tetrisrf.timer :refer [create-timer
                                    start-timer
                                    stop-timer
                                    set-timer-interval
                                    set-timer-tick-handler
                                    dispose-timer]]
            [tetrisrf.re-service.core :as rs]))


(def *timers (volatile! {}))


(rs/def-re-service ::timer)


(rs/def-re-service-command
  ::timer
  ::create
  []
  (create-timer))


(rs/def-re-service-command
  ::timer
  ::register
  [timer-id timer]
  (vswap! *timers
          assoc
          timer-id
          timer))


(rs/def-re-service-command
  ::timer
  ::unregister
  [timer-id & {:keys [dispose?] :or {dispose? true}}]
  (if-let [timer (@*timers timer-id)]
    (if dispose?
      (dispose-timer timer)))
  (vswap! *timers
          dissoc
          timer-id))


(rs/def-re-service-command
  ::timer
  ::dispose
  [timer-id]
  (cljs.pprint/pprint "!!!")
  (if-let [timer (@*timers timer-id)]
    (dispose-timer timer)))


(rs/def-re-service-command
  ::timer
  ::start
  [timer-id]
  (if-let [timer (@*timers timer-id)]
    (start-timer timer)))


(rs/def-re-service-command
  ::timer
  ::stop
  [timer-id]
  (if-let [timer (@*timers timer-id)]
    (stop-timer timer)))


(rs/def-re-service-command
  ::timer
  ::set-interval
  [timer-id interval]
  (if-let [timer (@*timers timer-id)]
    (set-timer-interval timer interval)))


(rs/def-re-service-command
  ::timer
  ::set-tick-handler
  [timer-id tick-handler]
  (if-let [timer (@*timers timer-id)]
    (set-timer-tick-handler timer tick-handler)))
