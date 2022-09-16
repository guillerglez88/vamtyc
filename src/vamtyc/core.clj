(ns vamtyc.core
  (:require [io.pedestal.http :as http]))

(defn respond-hello
  [request]
  {:status 200
   :body "Hello World!"})

(def routes
  #{["/hello" :get `respond-hello]})

(defn start
  []
  (-> {::http/routes routes
       ::http/port 8890
       ::http/type :jetty
       ::http/host "0.0.0.0"
       ::http/join? false}
      http/create-server
      http/start))

(defonce server (atom nil))

(defn start-dev []
  (reset! server (start)))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))

(comment
  (start-dev)
  (restart))
