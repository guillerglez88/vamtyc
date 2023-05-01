(ns vamtyc.core
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [vamtyc.cpj-api :as api]
   [vamtyc.env :refer [env]]
   [vamtyc.seed :as seed])
  (:gen-class))

(defonce server (atom nil))

(defn start [join?]
  (let [port (-> env :PORT Integer/parseInt)]
    (seed/init)
    (-> (api/init)
        (run-jetty {:port port
                    :join? join?})
        (#(reset! server %)))
    (println (str "Server running at: http://localhost:" port "/"))))

(defn stop []
  (when @server
    (.stop @server)
    (println "Server stoped")))

(defn -main [& _args]
  (start true))

(comment
  (start false)
  (stop))
