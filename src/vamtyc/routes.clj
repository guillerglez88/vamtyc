(ns vamtyc.routes
  (:require [clojure.string :as str]
            [next.jdbc :as jdbc]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]))

(def ddl
  "CREATE TABLE IF NOT EXISTS public.route(
       id          TEXT    NOT NULL,
       resource    JSONB   NULL,
       CONSTRAINT  route_pk PRIMARY KEY (id));")

(defn make-route-resource []
  {:type    "Route"
   :desc    "Represents a REST route resource"
   :routes  "/Route"})

(defn make-route [code method path]
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

(defn make-routes [resourceType]
  (let [res-type    {:name "resourceType" :value resourceType}
        id          {:name "id"}]
    [(make-route "list"      :GET    [res-type   ])
     (make-route "read"      :GET    [res-type id])
     (make-route "create"    :POST   [res-type   ])
     (make-route "upsert"    :PUT    [res-type id])
     (make-route "delete"    :DELETE [res-type id])]))

(defn provision [res tx]
  (doseq [route (-> res :type keyword make-routes)]
    (store/create tx :Route route)))

(defn init [tx]
  (let [res (make-route-resource)]
    (jdbc/execute! tx [ddl])
    (store/create tx :Resource "route" res)
    (doseq [item (store/list tx :Resource)]
      (provision item tx))
    {:ok "success!"}))
