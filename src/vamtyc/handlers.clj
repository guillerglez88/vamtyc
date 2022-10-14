(ns vamtyc.handlers
  (:require [clojure.pprint :as pp]
            [clojure.data.json :as json]))

(defn home-page [req]
  (def last-req req)
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {:status :ok})})

(defn simple-body-page [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World!"})

(defn request-example [req]
  {:status 200,
   :headers {"Content-Type" "text/html"}
   :body (->> (pp/pprint req)
              (str "Request: " req))})

(defn hello-name [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "Hello " (-> req :query-params (get "name")))})
