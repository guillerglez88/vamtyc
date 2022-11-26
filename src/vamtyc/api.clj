(ns vamtyc.api
  (:require [vamtyc.data.store :as store]
            [clojure.data.json :as json]
            [compojure.core :refer [make-route routes]]
            [clojure.string :as str]))

(def core-functions
  {:list store/list
   :read store/read
   :create store/create
   :upsert (fn [p] p)
   :delete store/delete})

(defn core-handler [route]
  (fn [req]
    (let [parsed-req    (select-keys req [:uri :params :form-params :query-params])
          body          (merge parsed-req {:db-route route})]
        {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/write-str body)})))

(defn build-cpj-path [path]
  (->> path
       (map (fn [cmp] (or (:value cmp) (str ":" (:name cmp)))))
       (into [])
       (str/join "/")
       (str "/")))

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path build-cpj-path)
        handler (core-handler route)]
    (make-route method path handler)))

(defn load-routes []
  (->> (store/list :HttpRoute)
       (map build-cpj-route)
       (apply routes)))
