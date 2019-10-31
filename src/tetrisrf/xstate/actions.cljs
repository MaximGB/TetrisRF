(ns tetrisrf.xstate.actions
  (:require [tetrisrf.xstate.utils :as utils]
            [re-frame.core :as rf]))

(defn db-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is a function similar to re-frame's reg-event-db handler (db event-vector) -> db
  The function also contains required interceptors in it's metadata."

  ([handler]
   (db-action [] handler))

  ([interceptors handler]
   (with-meta
     (fn [re-ctx]
       (let [db (rf/get-coeffect re-ctx :db)
             xs-event (utils/re-ctx->xs-event re-ctx)
             new-db (or (handler db xs-event) db)]
         (-> re-ctx
             ;; Assoc into both since there might be other action handlers which
             ;; read from `db` coeffect
             (rf/assoc-effect :db new-db)
             (rf/assoc-coeffect :db new-db))))
     {utils/xs-interceptors interceptors})))


(defn fx-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is function similar to re-frame's reg-event-fx handler (cofx-map event-vector) -> fx-map.
  The function also contains required interceptors in it's metadata."

  ([handler]
   (fx-action [] handler))

  ([interceptors handler]
   (with-meta
     (fn [re-ctx]
       (let [cofx (rf/get-coeffect re-ctx)
             xs-event (utils/re-ctx->xs-event re-ctx)
             new-effects (handler cofx xs-event)]
         (-> re-ctx
             ;; TODO: extract into util/merge-fx
             ((fn [re-ctx]
                (reduce (fn [re-ctx [effect-key effect-val]]
                          (rf/assoc-effect re-ctx effect-key effect-val))
                        re-ctx
                        new-effects)))
             ;; Special :db handling since there might be other action handlers
             ;; reading :db from :coeffects
             ((fn [re-ctx]
                (rf/assoc-coeffect re-ctx
                                   :db
                                   (rf/get-effect re-ctx :db)))))))
     {utils/xs-interceptors interceptors})))


(defn ctx-action
  "Unlike `db-action` and `fx-action` this function doesn't wrap adopting code over given `handler`. There's no need.

  It just adds list of required interceptors to the handler metadata."

  ([handler]
   (ctx-action [] handler))

  ([interceptors ctx-action]
   (with-meta
     (fn [re-ctx]
       (ctx-action re-ctx))
     {utils/xs-interceptors interceptors})))
