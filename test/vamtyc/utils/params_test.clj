(ns vamtyc.utils.params-test
  (:require [vamtyc.utils.params :as sut]
            [clojure.test :as t]))

(defn make-request []
  {:params          {"_of" "Resource"
                     :_id "a98bc78"
                     "_limit" "2" }
   :query-params    {"_of" "Resource"
                     "_limit" "2" }
   :uri             "/List"
   :query-string    "_of=Resource&_limit=2"
   :request-method  :get})

(defn make-route []
  {:type        :Route
   :method      :GET
   :path        [{:name     "_type"
                  :value    "List"}]
   :name        :search-list
   :code        "/Coding/nerves?code=search"
   :resource    "/Resource/list"})

(defn make-query-param []
  [{:type       :QueryParam
    :code       "/Coding/filters?code=keyword"
    :desc       "Type of resource to search for"
    :name       :_of
    :default    :List
    :of         :List}
   {:type       :QueryParam
    :code       "/Coding/filters?code=number"
    :desc       "Limit items count in the result"
    :name       :_limit
    :default    128
    :of         :List}
   {:type       :QueryParam
    :code       "/Coding/filters?code=number"
    :desc       "Skip that many items before starting to count result items"
    :name       :_offset
    :default    0
    :of         :List}
   {:type       :QueryParam
    :code       "/Coding/filters?code=keyword"
    :desc       "Order results by specified query-param"
    :name       :_sort
    :default    :_created
    :of         :List}])

(defn make-env []
  {:LIMIT           "128"
   :PORT            "3000"})

(defn make-params []
  {"_of"        "Resource"
   "_id"        "a98bc78"
   "_limit"     "2"
   "_type"      "List"
   "_offset"    "0"
   "_sort"      "_created"
   "env/LIMIT"  "128"
   "env/PORT"   "3000"})

(t/deftest can-make-env-params
  (t/testing "Can convert env map into a params like map"
    (t/is (= {"env/LIMIT" "128"}
             (sut/env-params {:LIMIT "128"})))))

(t/deftest can-make-queryp-params
  (t/testing "Can convert a query-params coll into a params like map"
    (t/is (= {"_of" "List"
              "_limit" "128"
              "_offset" "0"
              "_sort" "_created"}
             (sut/queryp-params (make-query-param))))))

(t/deftest can-make-route-params
  (t/testing "Can convert a route path into a hash-map"
    (t/is (= {"_type" "List"}
             (sut/route-params {:path [{:name "_type" :value "List"}]})))
    (t/is (= {"_type" "Resource"
              "_id" nil}
             (sut/route-params {:path [{:name "_type" :value "Resource"}
                                            {:name "_id" }]})))
    (t/is (= {"_type" "Resource"
              "_id" "route"}
             (sut/route-params {:path [{:name "_type" :value "Resource"}
                                            {:name "_id" :value "route"}]})))))

(t/deftest can-make-req-params
  (t/testing "Can sanitize request params to str keys"
    (t/is (= {"_id" "a987df8"}
             (sut/req-params {:params {:_id "a987df8"}})))))

(t/deftest can-make-params
  (t/testing "Can make filter-params from: req, route, query-params, env"
    (t/is (= (make-params)
             (sut/make-params (make-request)
                              (make-env)
                              (make-route)
                              (make-query-param))))))
