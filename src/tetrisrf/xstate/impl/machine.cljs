(ns tetrisrf.xstate.impl.machine
  (:require [tetrisrf.xstate.protocols :as protocols]
            [tetrisrf.xstate.utils :as utils]
            [xstate :as xs]))


(defrecord Machine [config options xs-machine meta]
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
   (let [options-normalized (utils/normalize-machine-options options)]
     (map->Machine {:config config
                    :options options
                    :meta (merge (utils/machine-config->actions-interceptors config)
                                 (utils/machine-options->actions-interceptors options-normalized))
                    :xs-machine (xs/Machine (utils/prepare-machine-config config)
                                            (utils/prepare-machine-options options-normalized))}))))
