(ns vamtyc.resources
  (:require [clojure.string :as str]
            [vamtyc.data.schema :as schema]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]))

(defn build-route [code method path]
  (let [res-type    (path/get-res-type path)
        res-name    (name res-type)
        res-name-lc (str/lower-case res-name)
        route-name  (str code "-" res-name-lc)
        resource    (str "/Resource/" res-name-lc)
        coding      (str "https://github.com/guillerglez88/vamtyc/wiki/core-handlers?code=" code)]
    {:method    method
     :path      path
     :name      route-name
     :resource  resource
     :type      :core
     :code      coding}))

(defn build-routes [resourceType]
  (let [res-type    {:name "resourceType" :value resourceType}
        id          {:name "id"}]
    [(build-route "list"      :GET    [res-type   ])
     (build-route "read"      :GET    [res-type id])
     (build-route "create"    :POST   [res-type   ])
     (build-route "upsert"    :PUT    [res-type id])
     (build-route "delete"    :DELETE [res-type id])]))

(def base-entities
  [{:id     "resource"
    :type   :Resource
    :desc   "Represents a REST resource"}
   {:id     "route"
    :type   :Route
    :desc   "Represents a REST route"}])

(defn bootstrap []
  (for [resource    base-entities
        :let        [resourceType   (:type  resource)
                     id             (:id    resource)]]
    (do
      (schema/provision resourceType)
      (store/create :Resource id resource)
      (for [route (build-routes resourceType)]
        (store/create :Route route)))))

(comment
  (bootstrap)
  )
