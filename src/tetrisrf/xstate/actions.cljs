(ns tetrisrf.xstate.actions
  (:require [tetrisrf.xstate.utils :as utils]
            [tetrisrf.xstate.co-effects :as co-effects]
            [re-frame.core :as rf]
            [tetrisrf.xstate.protocols :as protocols]))

(defn db-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is a function similar to re-frame's reg-event-db handler (db event-vector & meta) -> db.

   The function also contains required interceptors in it's metadata."

  ([handler]
   (db-action [] handler))

  ([interceptors handler]
   (with-meta
     (fn [re-ctx js-meta]
       (let [db (rf/get-coeffect re-ctx :db)
             xs-event (utils/re-ctx->xs-event re-ctx)
             kv-meta (utils/js-meta->kv-argv js-meta)
             new-db (or (apply handler (into [db xs-event] kv-meta)) db)]
         (-> re-ctx
             ;; Assoc into both since there might be other action handlers which
             ;; read from `db` coeffect
             (rf/assoc-effect :db new-db)
             (rf/assoc-coeffect :db new-db))))
     {utils/xs-interceptors interceptors})))


(defn idb-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  In contrast to `(db-action)` this function isolates DB using interpreter id as path into isolated DB section.
  `handler` is a function similar to re-frame's reg-event-db handler (db event-vector & meta) -> db.

  The function also adds ::co-effects/instance co-effect to required interceptors list and holds required interceptors in it's metadata."

  ([handler]
   (idb-action [] handler))

  ([interceptors handler]
   (with-meta
     (fn [re-ctx js-meta]
       (let [db (rf/get-coeffect re-ctx :db)
             interpreter (rf/get-coeffect re-ctx ::co-effects/instance)
             interpreter-path (protocols/interpreter->path interpreter)
             xs-event (utils/re-ctx->xs-event re-ctx)
             kv-meta (utils/js-meta->kv-argv js-meta)
             idb (get-in db interpreter-path {})
             new-db (assoc-in db
                              interpreter-path
                              (or (apply handler
                                         (into [idb xs-event]
                                               kv-meta))
                                  idb))]
         (-> re-ctx
             ;; Assoc into both since there might be other action handlers which
             ;; read from `db` coeffect
             (rf/assoc-effect :db new-db)
             (rf/assoc-coeffect :db new-db))))
     {utils/xs-interceptors (into [::co-effects/instance] interceptors)})))


(defn fx-action
  "Returns an intercepting function which adopts re-frame context to the `handler` and injects handler result back into re-frame context.

  `handler` is function similar to re-frame's reg-event-fx handler (cofx-map event-vector & meta) -> fx-map.

  The function also contains required interceptors in it's metadata."

  ([handler]
   (fx-action [] handler))

  ([interceptors handler]
   (with-meta
     (fn [re-ctx js-meta]
       (let [cofx (rf/get-coeffect re-ctx)
             xs-event (utils/re-ctx->xs-event re-ctx)
             kv-meta (utils/js-meta->kv-argv js-meta)
             new-effects (apply handler (into [cofx xs-event] kv-meta))]
         (-> re-ctx
             ;; TODO: maybe extract into util/merge-fx
             ((fn [re-ctx]
                (if (map? new-effects)
                  (reduce (fn [re-ctx [effect-key effect-val]]
                            (rf/assoc-effect re-ctx effect-key effect-val))
                          re-ctx
                          new-effects)
                  re-ctx)))
             ;; Special :db handling since there might be other action handlers
             ;; reading :db from :coeffects
             ((fn [re-ctx]
                (rf/assoc-coeffect re-ctx
                                   :db
                                   (rf/get-effect re-ctx :db)))))))
     {utils/xs-interceptors interceptors})))


(defn ctx-action
  "Returns an intercepting function which adopts re-frame context to the `handler`.

   `handler` is function similar to re-frame's reg-event-ctx handler (re-ctx event-vector & meta) -> re-ctx.

  The function also contains required interceptors in it's metadata."

  ([handler]
   (ctx-action [] handler))

  ([interceptors ctx-action]
   (with-meta
     (fn [re-ctx js-meta]
       (let [xs-event (utils/re-ctx->xs-event re-ctx)
             kv-meta (utils/js-meta->kv-argv js-meta)]
         (apply ctx-action (into [re-ctx xs-event] kv-meta))))
     {utils/xs-interceptors interceptors})))
