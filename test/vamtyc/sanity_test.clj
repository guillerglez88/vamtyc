(ns vamtyc.sanity-test
  (:require  [clojure.test :refer [deftest is testing]]))

(deftest can-test
  (testing "Testing infractructure is properly working"
    (is (= 5 (+ 3 2)))))
