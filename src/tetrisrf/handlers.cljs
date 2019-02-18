(ns tetrisrf.handlers
  (:require [re-frame.core :as rf]
            [tetrisrf.actions
             :refer
             [blend-tetramino
              calc-score
              can-act?
              field-complete-lines-count
              field-remove-complete-lines
              has-tetramino?
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


(rf/reg-event-fx
 :action-new
 (fn [cofx]
   (let [db (:db cofx)
         field (:field db)
         tetramino (rand-nth tetraminos)]
     (if (can-act? field #(place-tetramino %1 tetramino))
       {:db (assoc db :field (place-tetramino field tetramino))}
       (do (print "Game over!")
           {:stop-timer (:timer db)
            :db (assoc db
                       :running? false)})))))


(rf/reg-event-db
 :action-left
 (fn [db]
   (let [field (:field db)]
     (if (and (has-tetramino? field) (can-act? field move-left))
       (update db :field move-left)
       db))))


(rf/reg-event-db
 :action-right
 (fn [db _]
   (let [field (:field db)]
     (if (and (has-tetramino? field) (can-act? field move-right))
       (update db :field move-right)
       db))))


(rf/reg-event-db
 :action-down
 (fn [db]
   (let [field (:field db)]
     (if (has-tetramino? field)
       (if (can-act? field move-down)
         (update db :field move-down)
         (update db :field blend-tetramino))
       db))))


(rf/reg-event-db
 :action-rotate-cw
 (fn [db]
   (let [field (:field db)]
     (if (and (has-tetramino? field) (can-act? field rotate-90cw))
       (update db :field rotate-90cw)
       db))))


(rf/reg-event-db
 :action-rotate-ccw
 (fn [db]
   (let [field (:field db)]
     (if (and (has-tetramino? field) (can-act? field rotate-90ccw))
       (update db :field rotate-90ccw)
       db))))


(rf/reg-event-fx
 :action-run-stop
 (fn [cofx]
   (let [db (:db cofx)
         running? (:running? db)
         timer (:timer db)]
     (if running?
       {:stop-timer timer
        :db (assoc db :running? false)}
       {:start-timer timer
        :db (assoc db
                   :running? true
                   :score 0
                   :field (assoc (:field db) :cells []))
        :dispatch [:action-new]}))))


(rf/reg-event-fx
 :tick
 (fn [cofx]
   (let [db (:db cofx)
         field (:field db)]
     (if (has-tetramino? field)
       (if (can-act? field move-down)
         {:db (update db :field move-down)}
         (let [field-blended (blend-tetramino field)
               complete-lines-count (field-complete-lines-count field-blended)
               field-cleared (field-remove-complete-lines field-blended)
               score (calc-score (:score db) complete-lines-count)]
           {:db (assoc db
                       :field field-cleared
                       :score score)
            :dispatch [:action-new]}))
       {:dispatch [:action-new]}))))
