(ns tetrisrf.xstate.utils
  (:require [re-frame.core :as rf]
            [com.rpl.specter :as specter]
            [tetrisrf.xstate.protocols :as protocols]))

(defn arglist?
  "Opinionatedly checks if a sequence can be used as arglist"
  [s]
  (and s (seqable? s) (not (map? s))))


(defn re-ctx->*interpreter
  "Gets interpreter instance from re-frame context's `:event` co-effect."
  [re-ctx]
  (let [[_ interpreter] (rf/get-coeffect re-ctx :event)]
    interpreter))


(defn re-ctx->xs-event
  "Gets `xs-event` structure from re-frame context's `:event` co-effect."
  [re-ctx]
  (let [[_ _ xs-event] (rf/get-coeffect re-ctx :event)]
    xs-event))


(defn re-ctx->xs-event-type
  "Gets `xs-event` type from re-frame context's `:event` co-effect."
  [re-ctx]
  (first (re-ctx->xs-event re-ctx)))


(defn cofx->interpreter
  "Gets interpreter instance from re-frame `:event` co-effect."
  [cofx]
  (let [[_ interpreter] (:event cofx)]
    interpreter))


(defn js-meta->kv-argv
  "Transforms JS meta object into a vector of key value pairs."
  [js-meta]
  (->> (js->clj js-meta :keywordize-keys true)
      (seq)
      (flatten)
      (into [])))


(def xs-interceptors ::xs-interceptors)


;; TODO: simplify the path
(def MACHINE-CONFIG-ACTIONS (specter/recursive-path []
                                                    p
                                                    [:states
                                                     specter/MAP-VALS
                                                     (specter/multi-path [:on specter/MAP-VALS (specter/must :actions) (specter/if-path seqable? specter/ALL specter/STAY) #(instance? MetaFn %)]
                                                                         [(specter/must :entry) (specter/if-path seqable? specter/ALL specter/STAY) #(instance? MetaFn %)]
                                                                         [(specter/must :exit) (specter/if-path seqable? specter/ALL specter/STAY) #(instance? MetaFn %)]
                                                                         p)]))


;; TODO: simplify the path
(def MACHINE-OPTIONS-ACTIONS [(specter/must :actions)
                              specter/MAP-VALS
                              (specter/if-path seqable? specter/ALL specter/STAY)
                              #(instance? MetaFn %)])


(defn prepare-machine-config
  "Scans `config` of a XState machine and adopts it for JavaScript usage.

   There might be handlers with metadata, objects of MetaFn type which are not callable
   by JavaScript host, thus they should be converted back to normal `js/Function` type.
   Also the function does (clj->js) transformation."
  [config]
  (->> config
       (specter/transform MACHINE-CONFIG-ACTIONS
                          (fn [meta-fn]
                            (.-afn meta-fn)))
       (clj->js)))


(defn prepare-machine-options
  "Scans `options` of a XState machine and adopts it for JavaScript usage.

   There might be handlers with metadata, objects of MetaFn type which are not callable
   by JavaScript host, thus they should be converted back to normal `js/Function` type.
   Also the function does (clj->js) transformation."
  [options]
  (->> options
       (specter/transform MACHINE-OPTIONS-ACTIONS
                          (fn [meta-fn]
                            (.-afn meta-fn)))
       (clj->js)))


(defn meta-actions->interceptors-map
  "Transforms sequence of actions with interceptors metadata into a map where keys are metaless normall JS functions and values are list of interceptors."
  [actions & {:keys [bare?] :or {bare? true}}]
  (reduce (fn [m action-fn]
            (let [interceptors (xs-interceptors (meta action-fn))]
              (assoc m
                     (.-afn action-fn)
                     (if-not bare?
                       (map (fn [interceptor]
                              (if (or (keyword? interceptor) (symbol? interceptor) (string? interceptor) (number? interceptor))
                                (rf/inject-cofx interceptor)
                                interceptor))
                            interceptors)
                       interceptors))))
          {}
          actions))


(defn machine-config->actions-interceptors
  "Extracts interceptros metadata from actions given in machine configuration.

   Returns a map with original action functions as keys and handlers' metadata as value,
   such that it can be easily looked up during runtime."
  [config & {:keys [bare?] :or {bare? true}}]
  (meta-actions->interceptors-map (specter/select MACHINE-CONFIG-ACTIONS config)
                                  :bare? bare?))


(defn machine-options->actions-interceptors
  "Extracts interceptors metadata from actions given in machine options.

   Returns a map with original action functions as keys and handlers' metadata as value,
   such that it can be easily looked up during runtime."
  [options & {:keys [bare?] :or {bare? true}}]
  (meta-actions->interceptors-map (specter/select MACHINE-OPTIONS-ACTIONS options)
                                  :bare? bare?))
