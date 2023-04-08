(ns vamtyc.data.queryp-test
  (:require [vamtyc.data.queryp :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest load-default
  (testing "Can build sql to load Queryp with default value"
    (is (= [(str "SELECT * "
                 "FROM Queryp "
                 "WHERE JSONB_EXTRACT_PATH_TEXT(resource, ?) IS NOT NULL")
            "value"]
           (sut/def-queryps-sql)))))

(deftest load-querp-sql
  (testing "Can build sql to load Queryp"
    (is (= [(str "SELECT * "
                 "FROM Queryp "
                 "WHERE (JSONB_EXTRACT_PATH_TEXT(resource, ?) IN (?, ?, ?)) "
                 "AND (JSONB_EXTRACT_PATH_TEXT(resource, ?) IN (?, ?))")
            "of" "List" "Routes" "*" "name" "_of" "_limit"]

           (sut/queryps-sql ["List" "Routes" "*" nil]
                            ["_of" "_limit"])))))
