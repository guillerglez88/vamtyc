(ns vamtyc.utils.queryp-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [vamtyc.utils.queryp :as sut]))

(deftest resolve-queryp
  (testing "Can resolve Queryp value from req-params"
    (is (= {:name     :_limit
            :value    5
            :of       :List}
           (sut/resolve-queryp {"_limit" 5 "_of" "Resource"}
                               {:name :_limit
                                :of   :List})))))

(deftest find-by-code
  (testing "Find queryp value by code"
    (is (= "List"
           (sut/find-by-code [{:code "/Coding/wellknown-params?code=of"
                               :value "List"}]
                             "/Coding/wellknown-params?code=of")))
    (is (= "items.path.value"
           (sut/fields [{:code "/Coding/wellknown-params?code=fields"
                         :value "items.path.value"}])))
    (is (= :List
           (sut/of [{:code "/Coding/wellknown-params?code=of"
                     :value "List"}])))
    (is (= 0
           (sut/offset [{:code "/Coding/wellknown-params?code=offset"
                         :value 0}])))
    (is (= 128
           (sut/limit [{:code "/Coding/wellknown-params?code=limit"
                         :value 128}])))))
