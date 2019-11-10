(ns tetrisrf.re-service.core)


(defmacro def-re-service
  "Defines a service with the given `service-id`."
  [service-id]
  `(register-service ~service-id))


(defmacro def-re-service-command
  "Defines a service command."
  [service-id command-id args & fn-body]
  `(register-service-command ~service-id
                             ~command-id
                             (fn ~args ~@fn-body)))


(defmacro def-re-service-command-raw
  "Defines a raw service command."
  [service-id command-id args & fn-body]
  `(register-service-command-raw ~service-id
                                 ~command-id
                                 (fn ~args ~@fn-body)))
