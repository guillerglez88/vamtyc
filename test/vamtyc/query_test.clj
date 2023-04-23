(ns vamtyc.query-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [honey.sql :as hsql]
   [vamtyc.query :as sut]))

(deftest make-sql-map-test
  (testing "Can make base sql map to filter results on"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource")]
           (-> :Resource sut/make-sql-map hsql/format)))))

(deftest make-field-alias-test
  (testing "Can build sql field alias"
    (is (= :resource_code
           (sut/make-field-alias :resource "code")))
    (is (= :resource_path
           (sut/make-field-alias :resource "path")))
    (is (= :resource_path_elem
           (sut/make-field-alias :resource "path" "elem")))))

(deftest extract-prop-test
  (testing "Can expose jsonb prop for filtering"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS code ON TRUE")
            "code"]
           (-> (sut/make-sql-map :Resource)
               (sut/extract-prop :resource "code" :code)
               (hsql/format))))))

(deftest extract-coll-test
  (testing "Can expose jsonb prop collection elements for filtering"
    (is (= [(str "SELECT id, resource, created, modified "
                  "FROM Resource "
                  "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS resource_path ON TRUE "
                  "INNER JOIN JSONB_ARRAY_ELEMENTS(resource_path) AS path ON TRUE")
            "path"]
           (-> (sut/make-sql-map :Resource)
               (sut/extract-coll :resource "path" :path)
               (hsql/format))))))

(deftest jsonb-deep-access
  (testing "Can access deep jsonb property"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Route "
                 "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS resource_path ON TRUE "
                 "INNER JOIN JSONB_ARRAY_ELEMENTS(resource_path) AS resource_path_elem ON TRUE "
                 "INNER JOIN JSONB_EXTRACT_PATH(resource_path_elem, ?) AS res_type ON TRUE")
            "path" "value"]
           (-> (sut/make-sql-map :Route)
               (sut/extract-path :resource
                                 [{:name "path" :collection true}
                                  {:name "value"}]
                                 :res_type)
               (hsql/format))))))
