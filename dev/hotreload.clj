(ns hotreload
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [vamtyc.core :refer [handler]])
  (:gen-class))

(def dev-handler
  (wrap-reload #'handler))

(defn -main [& _args]
  (run-jetty dev-handler {:port 13000}))
