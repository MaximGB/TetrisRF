(ns tetrisrf.xstate.utils
  (:require [re-frame.core :as rf]))

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
