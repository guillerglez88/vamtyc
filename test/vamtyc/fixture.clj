(ns vamtyc.fixture)

(defn make-route []
  {:type :Route
   :method      :GET
   :path        [{:name     "_type"
                  :value    "Resource"}
                 {:name     "_id"}]
   :name        :read-resource
   :code        "/Coding/nerves?code=read"
   :resource    "/Resource/resource"})

(defn make-person []
  {:type        :Person
   :name        {:given ["John" "Adams"]
                 :family "Smith"}})
