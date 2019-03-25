(ns tetrisrf.handlers
  (:require [re-frame.core :as rf]
            [tetrisrf.actions
             :refer
             [blend-tetramino
              calc-next-level-score
              calc-next-level-timer-interval
              calc-score
              can-act?
              field-complete-lines-count
              field-remove-complete-lines
              has-tetramino?
              move-down
              move-left
              move-right
              place-tetramino-centered
              rotate-90ccw
              rotate-90cw]]
            [tetrisrf.db :refer [initial-db make-next-tetramino-field]]
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
         next-tetramino (:next-tetramino db)
         next-tetramino-field (:next-tetramino-field db)
         next-next-tetramino (rand-nth tetraminos)]
     (if (can-act? field #(place-tetramino-centered %1 next-tetramino))
       {:db (assoc db
                   :field (place-tetramino-centered field next-tetramino)
                   :next-tetramino next-next-tetramino
                   :next-tetramino-field (place-tetramino-centered next-tetramino-field next-next-tetramino :center-v true))}
       {:stop-timer (:timer db)
        :db (assoc db
                   :running false)
        :dispatch [:game-over]}))))


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


(rf/reg-event-fx
 :action-down
 (fn [cofx]
   (let [db (:db cofx)
         field (:field db)]
     (if (has-tetramino? field)
       {:dispatch [:tick]}))))


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
 :action-run-pause
 (fn [cofx]
   (let [db (:db cofx)
         running? (:running db)
         timer (:timer db)]
     (if running?
       {:stop-timer timer
        :db (assoc db
                   :running false)}
       {:start-timer timer
        :set-timer [timer (:timer-interval initial-db)]
        :db (let [next-tetramino (rand-nth tetraminos)]
              (assoc initial-db
                     :running true
                     :game-over false
                     :next-tetramino next-tetramino
                     :next-tetramino-field (place-tetramino-centered (make-next-tetramino-field) next-tetramino :center-v true)))
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
               score (calc-score (:score db) complete-lines-count)
               next-level-score (:next-level-score db)
               level-up (>= score next-level-score)
               new-next-level-score (if level-up (calc-next-level-score score))
               level (:level db)
               timer-interval (:timer-interval db)
               new-timer-interval (if level-up (calc-next-level-timer-interval timer-interval))
               timer (:timer db)]
           {:db (assoc db
                       :field field-cleared
                       :score score
                       :level (if level-up (inc level) level)
                       :next-level-score (if level-up new-next-level-score next-level-score)
                       :timer-interval (if level-up new-timer-interval timer-interval))
            :set-timer [timer (if level-up new-timer-interval timer-interval)]
            :dispatch [:action-new]}))
       {:dispatch [:action-new]}))))


(rf/reg-event-db
 :game-over
 (fn [db]
   (assoc db :game-over true)))
