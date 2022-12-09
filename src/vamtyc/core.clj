(ns vamtyc.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [vamtyc.config.env :refer [env]]
            [vamtyc.seeds.core :as seeds]
            [vamtyc.api :as api])
  (:gen-class))

(defn start []
  (let [port (-> env :PORT Integer/parseInt)]
    (seeds/init)
    (-> (api/init)
        (run-jetty {:port port
                    :join? false}))))

(defn -main [& _args]
  (start))

(comment
  ;; start service from main
  (-main)
  ;; start service
  (def server (start))
  ;; stop service
  (.stop server)
  )
