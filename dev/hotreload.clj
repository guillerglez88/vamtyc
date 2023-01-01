(ns hotreload
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.reload :refer [wrap-reload]]
   [vamtyc.config.env :refer [env]]
   [vamtyc.seed :as seed]
   [vamtyc.api :as api])
  (:gen-class))

(defonce server (atom nil))

(defn start []
  (let [port (-> env :PORT Integer/parseInt)]
    (seed/init)
    (-> (api/init)
        (wrap-reload)
        (run-jetty {:port port :join? false})
        (->> (reset! server)))
    (println (str "Dev server running at: http://localhost:" port "/"))))

(defn stop []
  (when @server
    (.stop @server)
    (println "Dev server stoped")))

(defn -main [& _args]
  (start))

(comment
  (start)
  (stop)
  )
