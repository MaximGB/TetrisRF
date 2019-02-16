(ns tetrisrf.handlers
  (:require [re-frame.core :as rf]
            [tetrisrf.actions
             :refer
             [blend-tetramino
              can-act?
              move-down
              move-left
              move-right
              place-tetramino
              rotate-90ccw
              rotate-90cw]]
            [tetrisrf.db :refer [initial-db]]
            [tetrisrf.tetraminos :refer [tetraminos]]))

(rf/reg-event-db
 :initialize-db
 (fn [db]
   initial-db))


;; TODO: temporary implementation for visual testing
(rf/reg-event-db
 :action-drop
 (fn [db]
   (let [field (:field db)
         tetramino (rand-nth tetraminos)]
     (if (can-act? field #(place-tetramino %1 tetramino))
       (assoc db :field (place-tetramino field tetramino))
       (do (print "Game over!")
           (assoc db :field (-> (:field db)
                                (assoc :cells nil
                                       :tetramino-prev :nil))))))))


(rf/reg-event-db
 :action-left
 (fn [db]
   (let [field (:field db)]
     (if (can-act? field move-left)
       (update db :field move-left)
       db))))


(rf/reg-event-db
 :action-right
 (fn [db _]
   (let [field (:field db)]
     (if (can-act? field move-right)
       (update db :field move-right)
       db))))


(rf/reg-event-db
 :action-down
 (fn [db]
   (let [field (:field db)]
     (if (can-act? field move-down)
       (update db :field move-down)
       (update db :field blend-tetramino)))))


(rf/reg-event-db
 :action-rotate-cw
 (fn [db]
   (let [field (:field db)]
     (if (can-act? field rotate-90cw)
       (update db :field rotate-90cw)
       db))))


(rf/reg-event-db
 :action-rotate-ccw
 (fn [db]
   (let [field (:field db)]
     (if (can-act? field rotate-90ccw)
       (update db :field rotate-90ccw)
       db))))


(rf/reg-event-fx
 :tick
 (fn [db [_]]
   (print "tick")))
