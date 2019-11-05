(ns tetrisrf.xstate.core)


(defmacro def-machine
  "Defines a var which is a reference to XState based state machine with given config."
  [var-name config]
  `(def ~var-name (machine! ~config)))


(defmacro def-action-db
  "Adds a DB-action with given `id` and handler function `action-fn` plus `interceptors` if needed to the machine defined by `var-name`."

  ([var-name id action-fn]
   `(machine-add-action! ~var-name ~id (db-action ~action-fn)))

  ([var-name id interceptors action-fn]
   `(machine-add-action! ~var-name ~id (db-action ~interceptors ~action-fn))))


(defmacro def-action-fx
  "Adds a FX-action with given `id` and handler function `action-fn` plus `interceptors` if needed to the machine defined by `var-name`."

  ([var-name id action-fn]
   `(machine-add-action! ~var-name ~id (fx-action ~action-fn)))

  ([var-name id interceptors action-fn]
   `(machine-add-action! ~var-name ~id (fx-action ~interceptors ~action-fn))))


(defmacro def-action-ctx
  "Adds a CTX-action with given `id` and handler function `action-fn` plus `interceptors` if needed to the machine defined by `var-name`."

  ([var-name id action-fn]
   `(machine-add-action! ~var-name ~id (ctx-action ~action-fn)))

  ([var-name id interceptors action-fn]
   `(machine-add-action! ~var-name ~id (ctx-action ~interceptors ~action-fn))))


(defmacro def-guard-ev
  "Adds a EV-guard with given `id` and handler function `guard-fn` to the machine defined by `var-name`."

  ([var-name id guard-fn]
   `(machine-add-guard! ~var-name ~id (ev-guard ~guard-fn))))


(defmacro def-guard-db
  "Adds a DB-guard with given `id` and handler function `guard-fn` to the machine defined by `var-name`."

  [var-name id guard-fn]
  `(machine-add-guard! ~var-name ~id (db-guard ~guard-fn)))


(defmacro def-guard-fx
  "Adds a FX-guard with given `id` and handler function `guard-fn` to the machine defined by `var-name`."

  [var-name id guard-fn]
   `(machine-add-guard! ~var-name ~id (fx-guard ~guard-fn)))


(defmacro def-guard-ctx
  "Adds a CTX-guard with given `id` and handler function `guard-fn` to the machine defined by `var-name`."

  [var-name id guard-fn]
   `(machine-add-guard! ~var-name ~id (ctx-guard ~guard-fn)))


;; TODO: add isolated versions of those guards/actions
