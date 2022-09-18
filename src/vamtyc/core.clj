(ns vamtyc.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [vamtyc.handlers :refer [simple-body-page
                                     request-example
                                     hello-name]])
  (:gen-class))

(defroutes app-routes
  (GET "/"                      []  simple-body-page)
  (GET "/request"               []  request-example)
  (GET "/hello"                 []  hello-name)
  (route/not-found "Error, page not found!"))

(def app
  (wrap-params app-routes))

(defn -main [& _args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (run-jetty app {:port port})
    (println (str "Running webserver at http://127.0.0.1:" port "/"))))
