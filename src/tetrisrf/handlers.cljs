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
              rotate-90cw
              save-prev-tetramino]]
            [tetrisrf.db :refer [initial-db]]
            [tetrisrf.tetraminos :refer [tetraminos]]))

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


;; TODO: temporary implementation for visual testing
(rf/reg-event-db
 :action-drop
 (fn [db _]
   (when-redrawn db
     (fn [db]
       (let [field (:field db)
             tetramino (rand-nth tetraminos)]
         (if (can-act? field #(place-tetramino %1 tetramino))
           (assoc db :field (place-tetramino field tetramino))
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
           (update db :field blend-tetramino)))))))


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
       (update :field save-prev-tetramino)
       (assoc :redrawn true))))


(rf/reg-event-fx
 :tick
 (fn [db [_]]
   (print "tick")))
