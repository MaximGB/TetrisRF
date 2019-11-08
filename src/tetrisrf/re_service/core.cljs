(ns tetrisrf.re-service.core
  (:require [re-frame.core :as rf]))


(defmulti exec-service-command (fn [service-id command-id]
                                 [service-id command-id]))


(defmethod exec-service-command :default [service-id command-id]
  (throw (js/Error. (str "Can't execute " service-id " service " command-id " command, no execute method defined!"))))


(defn- make-cofx-service-handler
  [service-id]
  (fn [cofx [maybe-result-key & commands-w-key :as commands-wo-key]]
    (let [[result-key commands] (if (odd? (count commands-wo-key))
                                  [maybe-result-key commands-w-key]
                                  [service-id commands-wo-key])
          command-tupples (partition 2 commands)]
      (assoc cofx
             result-key
             (reduce (fn [result [command-id args]]
                       (assoc result
                              command-id
                              (apply exec-service-command service-id command-id args)))
                     {}
                     command-tupples)))))


(defn- make-fx-service-handler
  [service-id]
  (fn [commands]
    (let [command-tupples (partition 2 commands)]
      (doseq [[command-id args] command-tupples]
        (apply exec-service-command service-id command-id args)))))


(defn register-service
  "TODO: document"
  [service-id]
  (rf/reg-cofx
   service-id
   (make-cofx-service-handler service-id))
  (rf/reg-fx
   service-id
   (make-fx-service-handler service-id)))
