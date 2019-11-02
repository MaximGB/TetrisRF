(ns tetrisrf.xstate.core
  (:require [tetrisrf.xstate.protocols :as protocols]
            [tetrisrf.xstate.impl.machine :as machine]
            [tetrisrf.xstate.impl.interpreter :as interpreter]
            [tetrisrf.xstate.guards :as guards]
            [tetrisrf.xstate.actions :as actions]
            [tetrisrf.xstate.co-effects :as co-effects]))


(def machine->config machine/machine->config)
(def machine->options machine/machine->options)
(def machine->interceptors machine/machine->interceptors)
(def machine->xs-machine machine/machine->xs-machine)

(def machine<-options machine/machine<-options)

(def Machine machine/Machine)
(def machine machine/machine)

(def machine! machine/machine!)
(def machine-add-guard! machine/machine-add-guard!)
(def machine-add-action! machine/machine-add-action!)

(def interpreter->id protocols/interpreter->id)
(def interpreter->machine protocols/interpreter->machine)
(def interpreter->state protocols/interpreter->state)
(def interpreter->started? protocols/interpreter->started?)
(def interpreter-start! protocols/interpreter-start!)
(def interpreter-stop! protocols/interpreter-stop!)

(def interpreter! interpreter/interpreter!)
(def interpreter-send! interpreter/interpreter-send!)

(def db-action actions/db-action)
(def fx-action actions/fx-action)
(def ctx-action actions/ctx-action)

(def ev-guard guards/ev-guard)
(def db-guard guards/db-guard)
(def fx-guard guards/fx-guard)
(def ctx-guard guards/ctx-guard)

(def cofx-interpreter-id ::co-effects/interpreter-id)
