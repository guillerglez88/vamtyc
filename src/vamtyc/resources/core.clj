(ns vamtyc.resources.core
  (:require [vamtyc.data.schema :as schema]
            [vamtyc.data.store :as store]
            [clojure.string :as str]))

(defn list [resourceType]
  (let [res-name    (-> resourceType name str/lower-case)
        route-name  (str "list-" res-name)]
    {:method  "GET"
     :path    [{:name "resourceType" :value res-name}]
     :name    route-name
     :type    "core"
     :handler "list"}))

(defn read [resourceType]
  (let [res-name    (-> resourceType name str/lower-case)
        route-name  (str "read-" res-name)]
    {:method  "GET"
     :path    [{:name "resourceType" :value res-name}
               {:name "id"}]
     :name    route-name
     :type    "core"
     :handler "read"}))

(defn create [resourceType]
  (let [res-name    (-> resourceType name str/lower-case)
        route-name  (str "create-" res-name)]
    {:method  "POST"
     :path    [{:name "resourceType" :value res-name}]
     :name    route-name
     :type    "core"
     :handler "create"}))

(defn upsert [resourceType]
  (let [res-name    (-> resourceType name str/lower-case)
        route-name  (str "upsert-" res-name)]
    {:method  "POST"
     :path    [{:name "resourceType" :value res-name}
               {:name "id"}]
     :name    route-name
     :type    "core"
     :handler "upsert"}))

(defn delete [resourceType]
  (let [res-name    (-> resourceType name str/lower-case)
        route-name  (str "delete-" res-name)]
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
  (provision :Currency)
  (provision :HttpRoute)
  (provision :MoneyExchange)
  )
