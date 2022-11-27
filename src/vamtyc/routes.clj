(ns vamtyc.routes
  (:require [clojure.data.json      :as     json]
            [clojure.string         :as     str]
            [compojure.core         :refer  [make-route routes]]
            [lambdaisland.uri       :refer  [uri query-string->map]]
            [vamtyc.data.schema     :as     schema]
            [vamtyc.data.store      :as     store]
            [vamtyc.utils.path      :as     path]
            [vamtyc.handlers.list   :as     list]
            [vamtyc.handlers.read   :as     read]
            [vamtyc.handlers.create :as     create]))

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

(defn meta-handler [route]
  (fn [req]
    (let [parsed-req    (select-keys req [:uri :params :form-params :query-params])
          body          (merge parsed-req {:route route})]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str body)})))

(def handlers
  {:core/list   list/handler
   :core/read   read/handler
   :core/create create/handler})

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)
        code    (-> route :code uri :query query-string->map :code)
        hkey    (-> route :type (keyword code))
        handler (-> hkey handlers (or meta-handler) (#(% route)))]
    (make-route method path handler)))

(defn init []
  (let [resource  {:type :Route
                   :desc "Represents a REST route"}
        id         "route"]
    (schema/provision :Route)
    (store/create :Resource id resource)
    (for [resource  (store/list :Resource)
          route     (-> resource :type keyword build-routes)]
      (store/create :Route route))))

(defn load-cpj-routes []
  (->> (store/list :Route)
       (map build-cpj-route)
       (apply routes)))

(comment
  (init)
  (load-cpj-routes)
  (uri "/Coding/core-handlers?code=list")
  )
