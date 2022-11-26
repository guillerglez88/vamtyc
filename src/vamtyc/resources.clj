(ns vamtyc.resources
  (:require [clojure.string :as str]
            [vamtyc.data.schema :as schema]
            [vamtyc.data.store :as store]))

(defn list [resourceType]
  (let [res-name    (name resourceType)
        route-name  (str "list-" (str/lower-case res-name))]
    {:method  "GET"
     :path    [{:name "resourceType" :value res-name}]
     :name    route-name
     :type    "core"
     :handler "list"}))

(defn read [resourceType]
  (let [res-name    (name resourceType)
        route-name  (str "read-" (str/lower-case res-name))]
    {:method  "GET"
     :path    [{:name "resourceType" :value res-name}
               {:name "id"}]
     :name    route-name
     :type    "core"
     :handler "read"}))

(defn create [resourceType]
  (let [res-name    (name resourceType)
        route-name  (str "create-" (str/lower-case res-name))]
    {:method  "POST"
     :path    [{:name "resourceType" :value res-name}]
     :name    route-name
     :type    "core"
     :handler "create"}))

(defn upsert [resourceType]
  (let [res-name    (name resourceType)
        route-name  (str "upsert-" (str/lower-case res-name))]
    {:method  "POST"
     :path    [{:name "resourceType" :value res-name}
               {:name "id"}]
     :name    route-name
     :type    "core"
     :handler "upsert"}))

(defn delete [resourceType]
  (let [res-name    (name resourceType)
        route-name  (str "delete-" (str/lower-case res-name))]
    {:method  "POST"
     :path    [{:name "resourceType" :value res-name}
               {:name "id"}]
     :name    route-name
     :type    "core"
     :handler "delete"}))

(defn build-routes [resourceType]
  [(list    resourceType)
   (read    resourceType)
   (create  resourceType)
   (upsert  resourceType)
   (delete  resourceType)])

(defn provision [resourceType]
  (let [routes (build-routes resourceType)]
    (schema/provision resourceType)
    (for [route routes]
      (store/create :HttpRoute route))))

;;
(comment
  (provision :HttpRoute)
  (provision :Currency)
  (provision :MoneyExchange)
  )
