(ns vamtyc.resources
  (:require [clojure.string :as str]
            [vamtyc.data.schema :as schema]
            [vamtyc.data.store :as store]))

(defn build-route [name-prefix method resourceType type]
  (let [res-name    (name resourceType)
        route-name  (str name-prefix "-" (str/lower-case res-name))
        resource    (str "/Resource/" res-name)]
    {:method  method
     :path    [{:name "resourceType" :value res-name}]
     :name    route-name
     :resource resource
     :type    "core"
     :handler "list"}))

(defn build-routes [resourceType]
  [(build-route "list"      :GET    resourceType    :core)
   (build-route "read"      :GET    resourceType    :core)
   (build-route "create"    :POST   resourceType    :core)
   (build-route "upsert"    :PUT    resourceType    :core)
   (build-route "delete"    :DELETE resourceType    :core)])

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
  (provision :Resource)
  )
