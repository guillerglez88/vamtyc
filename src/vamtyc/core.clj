(ns vamtyc.core
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [vamtyc.config.env :refer [env]]
   [vamtyc.seed :as seed]
   [vamtyc.api :as api])
  (:gen-class))

(defn start []
  (let [port (-> env :PORT Integer/parseInt)]
    (seed/init)
    (-> (api/init)
        (run-jetty {:port port}))
    (println (str "Server running at: http://localhost:" port "/"))))

(defn -main [& _args]
  (start))
