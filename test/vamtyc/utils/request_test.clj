(ns vamtyc.utils.request-test
  (:require [vamtyc.utils.request :as sut]
            [clojure.test :as t]))

(t/deftest relative-url
  (t/testing "Can extract relative url from ring request"
    (t/is (= "/List?_of=Route&_limit=3"
             (sut/relative-url {:uri "/List" :query-string "_of=Route&_limit=3"})))))
