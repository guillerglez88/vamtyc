(ns vamtyc.utils.request-test
  (:require
   [vamtyc.utils.request :as sut]
   [clojure.test :refer [deftest testing is]]))

(deftest relative-url
  (testing "Can extract relative url from ring request"
    (is (= "/List?_of=Route&_limit=3"
           (sut/relative-url {:uri "/List" :query-string "_of=Route&_limit=3"})))))
