(ns vamtyc.queries.utils-test
  (:require [vamtyc.queries.utils :as sut]
            [clojure.test :as t]))

(defn make-request []
  {:params          {"_of" "Resource"
                     "_limit" "2" }
   :query-params    {"_of" "Resource"
                     "_limit" "2" }
   :uri             "/List"
   :query-string    "_of=Resource&_limit=2"
   :request-method  :get})

(defn make-route []
  {:resourceType    :Route
   :method          :GET
   :path            [{:name     "_type"
                      :value    "List"}]
   :name            :search-list
   :code            "/Coding/nerves?code=search"
   :resource        "/Resource/list"})

(defn make-query-param []
  [{:resourceType   :QueryParam
    :code           "/Coding/filters?code=keyword"
    :desc           "Type of resource to search for"
    :name           :_of
    :default        :List
    :type           :List}
   {:resourceType   :QueryParam
    :code           "/Coding/filters?code=number"
    :desc           "Limit items count in the result"
    :name           :_limit
    :default        128
    :type           :List}
   {:resourceType   :QueryParam
    :code           "/Coding/filters?code=number"
    :desc           "Skip that many items before starting to count result items"
    :name           :_offset
    :default        0
    :type           :List}
   {:resourceType   :QueryParam
    :code           "/Coding/filters?code=keyword"
    :desc           "Order results by specified query-param"
    :name           :_sort
    :default        :_created
    :type           :List}])

(defn make-env []
  {:LIMIT           "128"
   :PORT            "3000"})

(defn make-params []
  {"_of"        "Resource"
   "_limit"     "2"
   "_type"      "List"
   "_offset"    "0"
   "_sort"      "_created"
   "env/LIMIT"  "128"
   "env/PORT"   "3000"})

(t/deftest can-make-env-params
  (t/testing "Can convert env map into a params like map"
    (t/is (= {"env/LIMIT" "128"}
             (sut/make-env-params {:LIMIT "128"})))))

(t/deftest can-make-queryp-params
  (t/testing "Can convert a query-params coll into a params like map"
    (t/is (= {"_of" "List"
              "_limit" "128"
              "_offset" "0"
              "_sort" "_created"}
             (sut/make-queryp-params (make-query-param))))))

(t/deftest can-make-params
  (t/testing "Can make filter-params from: req, route, query-params, env"
    (t/is (= (make-params)
             (sut/make-params (make-request)
                              (make-route)
                              (make-query-param)
                              (make-env))))))
