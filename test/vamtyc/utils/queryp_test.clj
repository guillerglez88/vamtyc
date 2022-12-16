(ns vamtyc.utils.queryp-test
  (:require [vamtyc.utils.queryp :as sut]
            [clojure.test :as t]))

(t/deftest resolve
  (t/testing "Can resolve QueryParam value from req-params"
    (t/is (= {:name     :_limit
              :value    5
              :of       :List}
             (sut/resolve-queryp {"_limit" 5 "_of" "Resource"}
                                 {:name :_limit
                                  :of   :List})))))

(t/deftest find-by-code
  (t/testing "Find queryp value by code"
    (t/is (= "List"
             (sut/find-by-code [{:code "/Coding/wellknown-params?code=of"
                                 :value "List"}]
                               :/Coding/wellknown-params?code=of)))
    (t/is (= "items.path.value"
             (sut/fields [{:code "/Coding/wellknown-params?code=fields"
                           :value "items.path.value"}])))
    (t/is (= :List
             (sut/of [{:code "/Coding/wellknown-params?code=of"
                       :value "List"}])))))
