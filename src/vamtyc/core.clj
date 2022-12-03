(ns vamtyc.core
  (:require [clojure.pprint :as pp]
            [clojure.data.json :as json]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [vamtyc.config.env :refer [env]]
            [vamtyc.utils.cpjroutes :as cpjroutes]
            [vamtyc.modules :as modules])
  (:gen-class))

(def mod-init (modules/init))

(def app-routes (wrap-params (cpjroutes/load-routes)))

(defn start []
  (let [port (-> env :PORT Integer/parseInt)]
    (run-jetty (var app-routes) {:port port :join? false})))

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
