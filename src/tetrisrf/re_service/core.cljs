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
  "Registers service with the given `service-id`, in re-frame co-effects/effects infracstructure.

   Having service and service commands (using (register-service-command) call ) registered, re-frame event handlers
   can request service command execution results to be injected as co-effects as well as issue effects which will be
   handled by service commands.

   Example:
   --------

   (register-service ::my-service)

   (rf/reg-event-fx
   ::my-event-handler
   [(rf/inject-cofx ::my-service [::command-id command-args ::another-command-id command-args])]
   (fn [cofx]
     {::my-service [::command-id command-args ::command-id command-args]}))

   Service commands results invoked as co-effects will be returned in co-effects map unders `service-id` key,
   the key value will be a map keyed by command-id with map values as command execution results.

   Example:
   --------
   {::my-service {::command-1 results-1
                  ::command-2 results-2}}"
  [service-id]
  (rf/reg-cofx
   service-id
   (make-cofx-service-handler service-id))
  (rf/reg-fx
   service-id
   (make-fx-service-handler service-id)))


(defn register-service-command-raw
  "Register command with the given `command-id` for the service with the given `service-id`.

   `handler` will be called with the `service-id` `command-id` as the first and second arguments, the rest arguments
    will be taken from user's co-effects/effects service command invokation request.

    Example:
    --------
    (inject-cofx ::service-id [::command-id rest-arguments])

    {::service-id [::command-id rest-arguments]}"
  [service-id command-id handler]
  (defmethod exec-service-command [service-id command-id] [& args] (apply handler args)))


(defn register-service-command
  "Register command with the given `command-id` for the service with the given `service-id`.

   `handler` will be called with the arguments taken from user's co-effects/effects service command invokation request.

    Example:
    --------
    (inject-cofx ::service-id [::command-id rest-arguments])

    {::service-id [::command-id rest-arguments]}"
  [service-id command-id handler]
  (register-service-command-raw service-id
                                command-id
                                (fn [_ _ & args]
                                  (apply handler args))))
