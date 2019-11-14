(ns tetrisrf.subscriptions
  (:require [maximgb.re-state.core :as xs]))

(xs/reg-isub
 :tetrisrf.core/field
 (fn [db]
   (:field db)))


(xs/reg-isub
 :tetrisrf.core/next-tetramino-field
 (fn [db]
   (:next-tetramino-field db)))


(xs/reg-isub
 :tetrisrf.core/score
 (fn [db]
   (or (:score db) 0)))


(xs/reg-isub
 :tetrisrf.core/level
 (fn [db]
   (or (:level db) 0)))


(xs/reg-isub
 :tetrisrf.core/game-over
 (fn [db]
   (or (:game-over db) false)))
