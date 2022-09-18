(ns hotreload
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [vamtyc.core :refer [app]])
  (:gen-class))

(def dev-handler
  (wrap-reload #'app))

(defn -main [& _args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (run-jetty dev-handler {:port port})
    (println (str "Running webserver at http://127.0.0.1:" port "/"))))
