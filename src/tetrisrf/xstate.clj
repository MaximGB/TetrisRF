(ns tetrisrf.xstate)

(defmacro defMachine
  "Defines a XState machine"
  [name definition options]
  `(def ~name (machine definition options)))


(defmacro defService
  "Defines an interpretor interpreting a machine difinition with given machine and service options"
  ([name definition]
   `(def ~name (service ~definition)))
  ([name definition machine-options]
   `(def ~name (service ~definition ~machine-options)))
  ([name definition machine-options service-options]
   `(def ~name (service ~definition ~machine-options ~service-options))))
