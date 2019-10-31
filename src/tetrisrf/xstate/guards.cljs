(ns tetrisrf.xstate.guards
  (:require [tetrisrf.xstate.utils :as utils]
            [re-frame.core :as rf]))

(defn db-guard
  "Returns a guard function which adopts re-frame context to the `handler` providing it with `db` co-effect.

  `handler` is a function similar to re-frame's reg-event-db handler but returns boolean: (db event-vector) -> boolean."

  [handler]
  (fn [re-ctx]
    (let [db (rf/get-coeffect re-ctx :db)
          xs-event (utils/re-ctx->xs-event re-ctx)]
      (handler db xs-event))))


(defn fx-guard
  "Returns a guard function which adopts re-frame context to the `handler` providing it with co-effects map.

  `handler` is a function similar to re-frame's reg-event-fx handler but returns boolean: (cofx event-vector) -> boolean."

  [handler]
  (fn [re-ctx]
    (let [cofx (rf/get-coeffect re-ctx)
          xs-event (utils/re-ctx->xs-event re-ctx)]
      (handler cofx xs-event))))


(defn ctx-guard
  "Returns a guard function which adopts re-frame context to the `handler` providing it with re-frame context.

  `handler` is a function similar to re-frame's reg-event-ctx handler but returns boolean: (re-ctx) -> boolean."

  [handler]
  (fn [re-ctx]
    (handler re-ctx)))