(ns vamtyc.sanity-test
  (:require  [clojure.test :refer [deftest is]]))

(deftest can-test
  (is (= 5 (+ 3 2))))
