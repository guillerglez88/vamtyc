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

(defn make-queryps []
  [{:type       :Queryp
    :code       "/Coding/filters?code=keyword"
    :desc       "Type of resource to search for"
    :name       :_of
    :value      :List
    :of         :List}
   {:type       :Queryp
    :code       "/Coding/filters?code=number"
    :desc       "Limit items count in the result"
    :name       :_limit
    :value      128
    :of         :List}
   {:type       :Queryp
    :code       "/Coding/filters?code=number"
    :desc       "Skip that many items before starting to count result items"
    :name       :_offset
    :value      0
    :of         :List}
   {:type       :Queryp
    :code       "/Coding/filters?code=keyword"
    :desc       "Order results by specified queryp"
    :name       :_sort
    :value      :_created
    :of         :List}])
