(ns tetrisrf.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :field
 (fn [db]
   (:field db)))


(rf/reg-sub
 :score
 (fn [db]
   (db :score)))


(rf/reg-sub
 :game-over
 (fn [db]
   (db :game-over)))


(rf/reg-sub
 :level
 (fn [db]
   (:level db)))


(rf/reg-sub
 :next-tetramino-field
 (fn [db]
   (:next-tetramino-field db)))
