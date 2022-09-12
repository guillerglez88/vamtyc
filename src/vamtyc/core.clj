(ns vamtyc.core
  (:require [io.pedestal.http :as http]))

(defn respond-hello
  [request]
  {:status 200
   :body "Hello World"})

(def routes
  #{["/hello" :get `respond-hello]})

(defn server
  []
  (-> {::http/routes routes
       ::http/port 8890
       ::http/type :jetty
       ::http/host "0.0.0.0"}
      http/create-server
      http/start))

(comment
  (server))
