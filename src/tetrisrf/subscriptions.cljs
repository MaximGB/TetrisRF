(ns tetrisrf.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :field
 (fn [db]
   (:field db)))


(rf/reg-sub
 :score
 (fn [db]
   (:score db)))


(rf/reg-sub
 :level
 (fn [db]
   (:level db)))


(rf/reg-sub
 :next-tetramino-field
 (fn [db]
   (get-in db [:next-tetramino-field])))
