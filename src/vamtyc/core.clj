(ns vamtyc.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]

            [vamtyc.config.env :refer [env]]
            [vamtyc.handlers :refer [home-page
                                     simple-body-page
                                     request-example
                                     hello-name]])
  (:gen-class))

(defroutes app-routes
  (GET "/"                      []  home-page)
  (GET "/request"               []  request-example)
  (GET "/hello"                 []  hello-name)
  (route/not-found "Error, page not found!"))

(def app
  (wrap-params app-routes))

(defn start
  [port]
  (run-jetty (var app) {:port port
                        :join? false}))

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
  (.stop server))
