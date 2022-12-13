(ns vamtyc.data.queryp-test
  (:require [vamtyc.data.queryp :as sut]
            [clojure.test :as t]))


(t/deftest load-querp-sql
  (t/testing "Can build sql to load QueryParam"
    (t/is (= [(str "SELECT * "
                   "FROM QueryParam "
                   "WHERE (JSONB_EXTRACT_PATH_TEXT(resource, ?) IN (?, ?, ?)) "
                   "AND ((JSONB_EXTRACT_PATH_TEXT(resource, ?) IS NOT NULL) OR "
                   "(JSONB_EXTRACT_PATH_TEXT(resource, ?) IN (?, ?)))")
              "of" "List" "Routes" "*" "default" "name" "_of" "_limit"]

             (sut/queryps-sql ["List" "Routes" "*" nil]
                              ["_of" "_limit"])))))
