(ns vamtyc.data.queryp-test
  (:require [vamtyc.data.queryp :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest querp-sql-test
  (testing "Can build sql to load Queryp"
    (is (= [(str "SELECT * "
                 "FROM Queryp "
                 "WHERE ((JSONB_EXTRACT_PATH_TEXT(resource, ?) IN (?, ?, ?)) "
                 "AND (JSONB_EXTRACT_PATH_TEXT(resource, ?) IN (?, ?))) "
                 "OR (JSONB_EXTRACT_PATH_TEXT(resource, ?) IS NOT NULL)")
            "of" "List" "Routes" "*" "name" "_of" "_limit" "value"]

           (sut/queryps-sql ["List" "Routes" "*" nil]
                            ["_of" "_limit"])))))
