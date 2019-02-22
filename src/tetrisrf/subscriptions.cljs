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
