(ns tetrisrf.xstate.core
  (:require-macros [tetrisrf.xstate.core])
  (:require [tetrisrf.xstate.protocols :as protocols]
            [tetrisrf.xstate.impl.machine :as machine]
            [tetrisrf.xstate.impl.interpreter :as interpreter]
            [tetrisrf.xstate.impl.registry :as registry]
            [tetrisrf.xstate.guards :as guards]
            [tetrisrf.xstate.actions :as actions]
            [tetrisrf.xstate.utils :as utils]
            [tetrisrf.xstate.co-effects :as co-effects]
            [tetrisrf.xstate.effects :as effects]))

(def machine->config protocols/machine->config)
(def machine->options protocols/machine->options)
(def machine->interceptors machine/machine->interceptors)
(def machine->xs-machine machine/machine->xs-machine)

(def machine<-options machine/machine<-options)

(def Machine machine/Machine)
(def machine machine/machine)

(def machine! machine/machine!)
(def machine-add-guard! machine/machine-add-guard!)
(def machine-add-action! machine/machine-add-action!)

(def init-event ::interpreter/xs-init)

(def interpreter->path protocols/interpreter->path)
(def interpreter->machine protocols/interpreter->machine)
(def interpreter->state protocols/interpreter->state)
(def interpreter->started? protocols/interpreter->started?)
(def interpreter-stop! protocols/interpreter-stop!)
(def interpreter-start! protocols/interpreter-start!)
(def interpreter-send! protocols/interpreter-send!)

(def interpreter! interpreter/interpreter!)

(def db-action actions/db-action)
(def fx-action actions/fx-action)
(def ctx-action actions/ctx-action)
(def idb-action actions/idb-action)

(def ev-guard guards/ev-guard)
(def db-guard guards/db-guard)
(def fx-guard guards/fx-guard)
(def ctx-guard guards/ctx-guard)

(def register-interpreter! registry/register-interpreter!)
(def unregister-interpreter! registry/unregister-interpreter!)
(def has-interpreter? registry/has-interpreter?)
(def id->interpreter registry/id->interpreter)

(def cofx-instance ::co-effects/instance)
(def cofx-spawn ::co-effects/spawn)

(def fx-register ::effects/register)
(def fx-unregister ::effects/unregister)
(def fx-start ::effects/start)
(def fx-stop ::effects/stop)
(def fx-send ::effects/send)
