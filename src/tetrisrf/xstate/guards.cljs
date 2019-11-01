(ns tetrisrf.xstate.guards
  (:require [tetrisrf.xstate.utils :as utils]
            [re-frame.core :as rf]))

(defn ev-guard
  "Returns a guard function which adopts re-frame context to the `handler` providing it with just event sent.

   `handler` is a function which recieves destructured event vector sent as it's arguments (& event) -> boolean."
  [handler]
  (fn [re-ctx _ js-meta]
    (let [xs-event (utils/re-ctx->xs-event re-ctx)
          kv-meta (utils/js-meta->kv-argv (.-cond js-meta))]
      (apply handler (into xs-event kv-meta)))))


(defn db-guard
  "Returns a guard function which adopts re-frame context to the `handler` providing it with `db` co-effect.

  `handler` is a function similar to re-frame's reg-event-db handler but returns boolean: (db event-vector) -> boolean."

  [handler]
  (fn [re-ctx _ js-meta]
    (let [db (rf/get-coeffect re-ctx :db)
          xs-event (utils/re-ctx->xs-event re-ctx)
          kv-meta (utils/js-meta->kv-argv (.-cond js-meta))]
      (apply handler (into [db xs-event] kv-meta)))))


(defn fx-guard
  "Returns a guard function which adopts re-frame context to the `handler` providing it with co-effects map.

  `handler` is a function similar to re-frame's reg-event-fx handler but returns boolean: (cofx event-vector) -> boolean."

  [handler]
  (fn [re-ctx _ js-meta]
    (let [cofx (rf/get-coeffect re-ctx)
          xs-event (utils/re-ctx->xs-event re-ctx)
          kv-meta (utils/js-meta->kv-argv (.-cond js-meta))]
      (apply handler (into [cofx xs-event] kv-meta)))))


(defn ctx-guard
  "Returns a guard function which adopts re-frame context to the `handler` providing it with re-frame context.

  `handler` is a function similar to re-frame's reg-event-ctx handler but returns boolean: (re-ctx) -> boolean."

  [handler]
  (fn [re-ctx _ js-meta]
    (let [xs-event (utils/re-ctx->xs-event re-ctx)
          kv-meta (utils/js-meta->kv-argv (.-cond js-meta))]
      (apply handler (into [re-ctx xs-event] kv-meta)))))
