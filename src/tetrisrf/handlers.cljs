(ns tetrisrf.handlers
  (:require [re-frame.core :as rf]
            [tetrisrf.actions
             :refer
             [can-act?
              move-down
              move-left
              move-right
              place-tetramino
              rotate-90ccw
              rotate-90cw
              save-prev-tetromino]]
            [tetrisrf.db :refer [initial-db]]
            [tetrisrf.tetrominos :refer [tetrominos]]))

(rf/reg-event-db
 :initialize-db
 (fn [db]
   initial-db))


(defn when-redrawn [db action-fn]
  (if (:redrawn db)
    (let [new-db (action-fn db)]
      (if (not= db new-db)
        (assoc new-db :redrawn false)
        db))
    db))


(rf/reg-event-db
 :action-drop
 (fn [db _]
   (when-redrawn db
     (fn [db]
       (let [field (:field db)
             tetromino (rand-nth tetrominos)]
         (if (can-act? field #(place-tetramino %1 tetromino))
           (assoc db :field (place-tetramino field tetromino))
           db))))))


(rf/reg-event-db
 :action-left
 (fn [db _]
   (when-redrawn db
     (fn [db]
       (let [field (:field db)]
         (if (can-act? field move-left)
           (update db :field move-left)
           db))))))


(rf/reg-event-db
 :action-right
 (fn [db _]
   (when-redrawn db
     (fn [db]
       (let [field (:field db)]
         (if (can-act? field move-right)
           (update db :field move-right)
           db))))))


(rf/reg-event-db
 :action-down
 (fn [db _]
   (when-redrawn db
     (fn [db]
       (let [field (:field db)]
         (if (can-act? field move-down)
           (update db :field move-down)
           db))))))


(rf/reg-event-db
 :action-rotate-cw
 (fn [db _]
   (when-redrawn db
     (fn [db]
       (let [field (:field db)]
         (if (can-act? field rotate-90cw)
           (update db :field rotate-90cw)
           db))))))


(rf/reg-event-db
 :action-rotate-ccw
 (fn [db _]
   (when-redrawn db
     (fn [db]
       (let [field (:field db)]
         (if (can-act? field rotate-90ccw)
           (update db :field rotate-90ccw)
           db))))))


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
