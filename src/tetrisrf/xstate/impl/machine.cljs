(ns tetrisrf.xstate.impl.machine
  (:require [tetrisrf.xstate.protocols :as protocols]
            [xstate :as xs]))


(defrecord Machine [config options xs-machine]
  protocols/MachineProtocol

  (machine->config [this]
    (:config this))

  (machine->options [this]
    (:options this))

  (machine->xs-machine [this]
    (:xs-machine this)))


(defn machine
  "Creates a XState based machine record with given definition and optional options"

  ([config]
   (machine config {}))

  ([config options]
   (map->Machine {:config config
                  :options options
                  :xs-machine (xs/Machine (clj->js config)
                                          (clj->js options))})))
