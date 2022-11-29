(ns vamtyc.routes
  (:require [clojure.string         :as     str]
            [next.jdbc              :as     jdbc]
            [vamtyc.data.datasource :refer  [ds]]
            [vamtyc.data.store      :as     store]
            [vamtyc.utils.path      :as     path]))

(defn ddl [name]
  (str "CREATE TABLE IF NOT EXISTS public." name "(
            id          TEXT    NOT NULL,
            resource    JSONB   NULL,
            CONSTRAINT  " name "_pk PRIMARY KEY (id));"))

(defn build-route [code method path]
  (let [res-type    (path/get-res-type path)
        res-name    (name res-type)
        res-name-lc (str/lower-case res-name)
        route-name  (str code "-" res-name-lc)
        resource    (str "/Resource/" res-name-lc)
        coding      (str "/Coding/core-handlers?code=" code)]
    {:method    method
     :path      path
     :name      route-name
     :resource  resource
     :code      coding}))

(defn build-routes [resourceType]
  (let [res-type    {:name "resourceType" :value resourceType}
        id          {:name "id"}]
    [(build-route "list"      :GET    [res-type   ])
     (build-route "read"      :GET    [res-type id])
     (build-route "create"    :POST   [res-type   ])
     (build-route "upsert"    :PUT    [res-type id])
     (build-route "delete"    :DELETE [res-type id])]))

(defn provision [res]
  (for [route (-> res :type keyword build-routes)]
    (store/create :Route route)))

(defn init []
  (let [res {:type "Route" :desc "Represents a REST route"}
        id  "route"
        ddl (ddl "Route")]
    (jdbc/execute! ds [ddl])
    (store/create :Resource id res)
    (for [item (store/list :Resource)]
      (provision item))))

(comment
  (init)
  )
