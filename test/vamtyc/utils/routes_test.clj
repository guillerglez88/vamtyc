(ns vamtyc.utils.routes-test
  (:require [vamtyc.utils.routes :as sut]
            [clojure.test :as t]))

(t/deftest can-make-params
  (t/testing "Can convert a route path into a hash-map"
    (t/is (= {"_type" "List"}
             (sut/make-params [{:name "_type" :value "List"}])))
    (t/is (= {"_type" "Resource"
              "_id" nil}
             (sut/make-params [{:name "_type" :value "Resource"}
                               {:name "_id" }])))
    (t/is (= {"_type" "Resource"
              "_id" "route"}
             (sut/make-params [{:name "_type" :value "Resource"}
                               {:name "_id" :value "route"}])))))
