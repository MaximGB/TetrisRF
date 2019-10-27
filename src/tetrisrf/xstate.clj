(ns tetrisrf.xstate)

(defmacro db-action
  "Defines an action similar to the re-frame's `reg-event-db` (db event-vector) -> db"

  ([]
   `(wrap-db-action (fn [])))

  ([params]
   `(wrap-db-action (fn ~params)))

  ([params body]
   `(wrap-db-action (fn ~params ~body)))

  ([interceptors params body]
   `(wrap-db-action ~interceptors (fn ~params ~body))))


(defmacro fx-action
  "Defines an action similar to the re-frame's `reg-event-fx` (cofx-map event-vector) -> effects-map"

  ([]
   `(wrap-fx-action (fn [])))

  ([params]
   `(wrap-fx-action (fn ~params)))

  ([params body]
   `(wrap-fx-action (fn ~params ~body)))

  ([interceptors params body]
   `(wrap-fx-action ~interceptors (fn ~params ~body))))


(defmacro ctx-action
  "Defines an action similar to the re-frame's `reg-event-ctx` (context-map event-vector) -> context-map"

  ([]
   `(wrap-ctx-action (fn [])))

  ([params]
   `(wrap-ctx-action (fn ~params)))

  ([params body]
   `(wrap-ctx-action (fn ~params ~body)))

  ([interceptors params body]
   `(wrap-ctx-action ~interceptors (fn ~params ~body))))


(defmacro db-guard
  "Defines a guard recieving db and event, similar to the re-frame's `reg-event-db` (db event-vector) -> boolean"

  ([]
   `(wrap-db-guard (fn [] true)))

  ([params]
   `(wrap-db-guard (fn ~params true)))

  ([params body]
   `(wrap-db-guard (fn ~params ~body)))

  ([interceptors params body]
   `(wrap-db-guard ~interceptors (fn ~params ~body))))


(defmacro fx-guard
  "Defines a guard recieving co-effects map and event, similar to the re-frame's `reg-event-fx` (cofx-map event-vector) -> boolean"

  ([]
   `(wrap-fx-guard (fn [] true)))

  ([params]
   `(wrap-fx-guard (fn ~params true)))

  ([params body]
   `(wrap-fx-guard (fn ~params ~body)))

  ([interceptors params body]
   `(wrap-fx-guard ~interceptors (fn ~params ~body))))


(defmacro ctx-guard
  "Defines a guard recieving context map and event, similar to the re-frame's `reg-event-ctx` (cofx-map event-vector) -> boolean"

  ([]
   `(wrap-ctx-guard (fn [] true)))

  ([params]
   `(wrap-ctx-guard (fn ~params true)))

  ([params body]
   `(wrap-ctx-action (fn ~params ~body)))

  ([interceptors params body]
   `(wrap-ctx-action ~interceptors (fn ~params ~body))))
