(ns vamtyc.resources.core
  (:require [vamtyc.data.schema :as schema]
            [vamtyc.data.store :as store]))

(defn provision [resourceType]
  (let [res-name (name resourceType)
        res-type-path {:name "resourceType" :value res-name}
        res-id-path {:name "id"}
        endpoints [{:name (str "list-" res-name)
                    :method "GET"
                    :path [res-type-path]
                    :type "core"
                    :handler "list"}
                   {:name (str "read-" res-name)
                    :method "GET"
                    :path [res-type-path res-id-path]
                    :type "core"
                    :handler "read"}
                   {:name (str "create-" res-name)
                    :method "POST"
                    :path [res-type-path]
                    :type "core"
                    :handler "create"}
                   {:name (str "upsert-" res-name)
                    :method "PUT"
                    :path [res-type-path res-id-path]
                    :type "core"
                    :handler "upsert"}
                   {:name (str "delete-" res-name)
                    :method "DELETE"
                    :path [res-type-path res-id-path]
                    :type "core"
                    :handler "delete"}]]
    (schema/provision resourceType)
    (for [endpoint endpoints]
      (store/create :endpoint endpoint))))


(comment
  (provision :practitioner))
