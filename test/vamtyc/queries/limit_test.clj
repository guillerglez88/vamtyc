(ns vamtyc.queries.limit-test
  (:require [vamtyc.queries.limit :as sut]
            [clojure.test :as t]
            [honey.sql :refer [format]]
            [honey.sql.helpers :refer [select from limit]]))

(defn make-params [keys]
  (select-keys
   {"_of"        "Resource"
    "_limit"     "2"
    "_type"      "List"
    "_offset"    "0"
    "_sort"      "_created"
    "env/LIMIT"  "128"} keys))

(defn make-sql []
  (-> (select :*)
      (from :Resource)))

(t/deftest can-limit-results
  (t/testing "_limit param is choosen by default"
    (t/is (= ["SELECT * FROM Resource LIMIT ?" 2]
             (format (sut/filter (make-sql)
                                 (make-params ["_env/LIMIT" "_limit"]))))))
  (t/testing "if _limit not found, env is used"
    (t/is (= ["SELECT * FROM Resource LIMIT ?" 128]
             (format (sut/filter (make-sql)
                                 (make-params ["env/LIMIT"])))))))
