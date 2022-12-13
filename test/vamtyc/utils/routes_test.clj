(ns vamtyc.utils.routes-test
  (:require [vamtyc.utils.routes :as sut]
            [clojure.test :as t]))

(t/deftest str-path
  (t/testing "Can stringify route path"
    (t/is (= "/Resource/:_id"
             (sut/str-path [{:name "_type" :value "Resource"}
                            {:name "_id"}])))
    (t/is (= "/Resource"
             (sut/str-path [{:name "_type" :value "Resource"}])))))

(t/deftest calc-match-index
  (t/testing "Can calculate a match index in order to sort routes"
    (t/is (= 0
             (sut/calc-match-index [])))
    (t/is (= 1
             (sut/calc-match-index [{:name "_type" :value "Resource"}])))
    (t/is (= 1
             (sut/calc-match-index [{:name "_type" :value "Resource"}
                                    {:name "_id"}])))
    (t/is (= 2
             (sut/calc-match-index [{:name "_type" :value "Resource"}
                                    {:name "sub" :value "Vamtyc"}])))))

(t/deftest get-res-type
  (t/testing "Can get _type from route path"
    (t/is (= "Resource"
             (sut/get-res-type [{:name "_type" :value "Resource"}
                                {:name "sub" :value "Vamtyc"}])))
    (t/is (= nil
             (sut/get-res-type [])))))
