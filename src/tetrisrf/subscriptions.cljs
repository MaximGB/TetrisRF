(ns tetrisrf.subscriptions
  (:require [tetrisrf.xstate.core :as xs]))

(xs/reg-isub
 :tetrisrf.core/field
 (fn [db]
   (:field db)))


(xs/reg-isub
 :tetrisrf.core/score
 (fn [db]
   (:score db)))


(xs/reg-isub
 :tetrisrf.core/game-over
 (fn [db]
   (:game-over db)))


(xs/reg-isub
 :tetrisrf.core/level
 (fn [db]
   (:level db)))


(xs/reg-isub
 :tetrisrf.core/next-tetramino-field
 (fn [db]
   (:next-tetramino-field db)))
