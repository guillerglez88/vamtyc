(ns vamtyc.utils.routes-test
  (:require
   [vamtyc.utils.routes :as sut]
   [clojure.test :refer [deftest testing is]]))

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
