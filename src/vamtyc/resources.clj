(ns vamtyc.resources
  (:require [vamtyc.data.schema :as schema]
            [vamtyc.data.store :as store]))

(defn init []
  (let [resource    {:type :Resource
                     :desc "Represents a REST resource"}
        id          "resource"]
    (schema/provision :Resource)
    (store/create :Resource id resource)))

(comment
  (init)
  )
