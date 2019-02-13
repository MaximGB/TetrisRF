(ns ^:figwheel-hooks tetrisrf.core
  (:require [tetrisrf.actions
             :refer
             [can-act?
              move-down
              move-left
              move-right
              place-tetramino
              rotate-90cw
              save-prev-tetromino]]
            [tetrisrf.db :refer [initial-db]]
            [tetrisrf.tetrominos :refer [tetrominos]]
            [tetrisrf.timer :refer [start-timer stop-timer]]
            [tetrisrf.views.tetris-panel :refer [tetris-panel]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

;; Event handlers
(rf/reg-event-db
 :initialize-db
 (fn [db]
   initial-db))


(rf/reg-event-db
 :key-down
 (fn [db [_ key]]
   (let [redrawn? (:redrawn db)]
     (if redrawn?
       (let [field (:field db)]
         (case key
             " "          (let [tetromino (rand-nth tetrominos)]
                            (if (can-act? field #(place-tetramino %1 tetromino))
                              (assoc db
                                     :field (place-tetramino field tetromino)
                                     :redrawn false)
                              db))
             "ArrowRight" (if (can-act? field move-right)
                            (-> db
                                (update :field move-right)
                                (assoc :redrawn false))
                            db)
             "ArrowLeft"  (if (can-act? field move-left)
                            (-> db
                                (update :field move-left)
                                (assoc :redrawn false))
                            db)
             "ArrowDown"  (if (can-act? field move-down)
                            (-> db
                                (update :field move-down)
                                (assoc :redrawn false))
                            db)
             "ArrowUp"    (if (can-act? field rotate-90cw)
                            (-> db
                                (update :field rotate-90cw)
                                (assoc :redrawn false))
                            db)
             "Enter"      (-> db
                              (update :field assoc :tetromino nil :tetromino-prev nil :cells [])
                              (assoc :redrawn false))
             db))
       db))))


(rf/reg-event-db
 :redrawn
 (fn [db]
   (-> db
       (update :field save-prev-tetromino)
       (assoc :redrawn true))))


(rf/reg-event-fx
 :tick
 (fn [db [_]]
   (print "tick")))


;; Effects
(rf/reg-fx
 :start-timer
 (fn [timer]
   (start-timer timer)))


(rf/reg-fx
 :stop-timer
 (fn [timer]
   (stop-timer timer)))


;; Subscriptions
(rf/reg-sub
 :field
 (fn [db]
   (:field db)))

(defn ^:after-load -main []
  (rf/dispatch-sync [:initialize-db])
  (reagent/render [tetris-panel] (.getElementById js/document "app")))

(.addEventListener js/window "load" (fn [] (-main)))
