(ns vamtyc.core
  (:require [clojure.pprint         :as     pp]
            [clojure.data.json      :as     json]
            [ring.adapter.jetty     :refer  [run-jetty]]
            [ring.middleware.params :refer  [wrap-params]]
            [compojure.core         :refer  [defroutes GET]]
            [compojure.route        :as     route]
            [vamtyc.config.env      :refer  [env]]
            [vamtyc.routes          :as     routes])
  (:gen-class))

(def app
  (wrap-params (routes/load-cpj-routes)))

(defn start
  [port]
  (-> (var app)
      (run-jetty {:port port
                  :join? false})))

(defn -main [& _args]
  (let [port (Integer/parseInt (:PORT env))]
    (start port)
    (println (str "Running webserver at http://127.0.0.1:" port "/"))))

(comment
  ;; start service from main
  (-main)
  ;; start service
  (def server
    (start (Integer/parseInt (:PORT env))))
  ;; stop service
  (.stop server)
  )
