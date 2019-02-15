(ns tetrisrf.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :field
 (fn [db]
   (:field db)))
