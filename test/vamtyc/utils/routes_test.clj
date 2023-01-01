(ns vamtyc.utils.routes-test
  (:require
   [vamtyc.utils.routes :as sut]
   [clojure.test :refer [deftest testing is]]))

(deftest str-path
  (testing "Can stringify route path"
    (is (= "/Resource/:_id"
           (sut/str-path [{:name "_type" :value "Resource"}
                          {:name "_id"}])))
    (is (= "/Resource"
           (sut/str-path [{:name "_type" :value "Resource"}])))))

(deftest calc-match-index
  (testing "Can calculate a match index in order to sort routes"
    (is (= 0
           (sut/calc-match-index [])))
    (is (= 1
           (sut/calc-match-index [{:name "_type" :value "Resource"}])))
    (is (= 1
           (sut/calc-match-index [{:name "_type" :value "Resource"}
                                  {:name "_id"}])))
    (is (= 2
           (sut/calc-match-index [{:name "_type" :value "Resource"}
                                  {:name "sub" :value "Vamtyc"}])))))

(deftest get-res-type
  (testing "Can get _type from route path"
    (is (= :Resource
           (sut/_type [{:name "_type"
                        :value "Resource"
                        :code "/Coding/wellknown-params?code=type"}
                       {:name "sub"
                        :value "Vamtyc"
                        :code "/Coding/wellknown-params?code=type"}])))
    (is (= nil
           (sut/_type [])))))

(deftest resolve-path
  (testing "Can resolve route-free path-components from req-params"
    (is (= {:path [{:name "_type" :value "Resource"}
                   {:name "_id" :value "a98bc78"}]}
           (sut/resolve-path {:path [{:name "_type"} {:name "_id"}]}
                             {"_type" "Resource" "_id" "a98bc78"})))))
