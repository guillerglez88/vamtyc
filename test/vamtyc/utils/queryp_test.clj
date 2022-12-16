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

(t/deftest of
  (t/testing "Extract type from _of queryp"
    (t/is (= :List
             (sut/of [{:code "/Coding/wellknown-params?code=of"
                       :value "List"}])))))
