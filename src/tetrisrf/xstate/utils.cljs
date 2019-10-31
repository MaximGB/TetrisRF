(ns tetrisrf.xstate.utils
  (:require [re-frame.core :as rf]
            [com.rpl.specter :as specter]))

(defn re-ctx->*interpreter
  "Gets interpreter instance from re-frame context's `:event` co-effect."
  [re-ctx]
  (let [[_ vinterpreter] (rf/get-coeffect re-ctx :event)]
    vinterpreter))


(defn re-ctx->xs-event
  "Gets `xs-event` structure from re-frame context's `:event` co-effect."
  [re-ctx]
  (let [[_ _ xs-event] (rf/get-coeffect re-ctx :event)]
    xs-event))


(defn re-ctx->xs-event-type
  "Gets `xs-event` type from re-frame context's `:event` co-effect."
  [re-ctx]
  (first (re-ctx->xs-event re-ctx)))


; TODO: simplify the path
(def MACHINE-CONFIG-ACTIONS (specter/recursive-path []
                                                    p
                                                    [:states
                                                     specter/MAP-VALS
                                                     (specter/multi-path [:on specter/MAP-VALS (specter/must :actions) (specter/if-path seqable? specter/ALL specter/STAY) #(instance? MetaFn %)]
                                                                         [(specter/must :entry) (specter/if-path seqable? specter/ALL specter/STAY) #(instance? MetaFn %)]
                                                                         [(specter/must :exit) (specter/if-path seqable? specter/ALL specter/STAY) #(instance? MetaFn %)]
                                                                         p)]))


(def MACHINE-OPTIONS-ACTIONS [])


(defn prepare-machine-config
  "Scans `config` of a XState machine and adopts it for JavaScript usage.

   There might be handlers with metadata, objects of MetaFn type which are not callable
   by JavaScript host, thus they should be converted back to normal `js/Function` type.
   Also the function does (clj->js) transformation."
  [config]
  (clj->js config))


(defn prepare-machine-options
  "Scans `options` of a XState machine and adopts it for JavaScript usage.

   There might be handlers with metadata, objects of MetaFn type which are not callable
   by JavaScript host, thus they should be converted back to normal `js/Function` type.
   Also the function does (clj->js) transformation."
  [options]
  (clj->js options))


(defn machine-config->actions-interceptors
  "Extracts interceptros metadata from actions given in machine configuration.

   Returns a map with original action functions as keys and handlers' metadata as value,
   such that it can be easily looked up during runtime."
  [config]
  (reduce (fn [m action-fn]
            (assoc m (.-afn action-fn) (:xs-interceptors (meta action-fn))))
          {}
          (specter/select MACHINE-CONFIG-ACTIONS config)))


(defn machine-options->actions-interceptors
  "Extracts interceptors metadata from actions given in machine options.

   Returns a map with original action functions as keys and handlers' metadata as value,
   such that it can be easily looked up during runtime."
  [options]
  {}
  #_(specter/select MACHINE-OPTIONS-ACTIONS options))


(defn normalize-machine-options
  "Normalizes machine `options` from guards/actions mixed definition to separated one, if needed."
  [options]
  options)