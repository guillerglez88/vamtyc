(ns vamtyc.utils.params-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [vamtyc.fixture :as fixture]
   [vamtyc.utils.params :as sut]))

(deftest queryp-val-str
  (testing "Get Queryp :value as string"
    (is (= "List"
           (sut/queryp-val-str {:type      :Queryp
                                :value     :List})))
    (is (= "128"
           (sut/queryp-val-str {:type      :Queryp
                                :value     128})))
    (is (= ""
           (sut/queryp-val-str {:type      :Queryp})))))

(deftest queryp-to-params
  (testing "Can convert a queryps coll into a params map"
    (is (= {"_of"     "List"
            "_limit"  "128"
            "_offset" "0"
            "_sort"   "_created"}
           (sut/queryp-to-params (fixture/make-queryps))))))

(deftest route-to-params
  (testing "Can convert a route path into a hash-map"
    (is (= {"_type" "List"}
           (sut/route-to-params {:path [{:name "_type" :value "List"}]})))
    (is (= {"_type" "Resource"
            "_id" nil}
           (sut/route-to-params {:path [{:name "_type" :value "Resource"}
                                        {:name "_id"}]})))
    (is (= {"_type" "Resource"
            "_id"   "route"}
           (sut/route-to-params {:path [{:name "_type" :value "Resource"}
                                        {:name "_id" :value "route"}]})))))

(deftest req-to-params
  (testing "Can sanitize request params to str keys"
    (is (= {"_id" "a987df8"}
           (sut/req-to-params {:params {:_id "a987df8"}})))))
