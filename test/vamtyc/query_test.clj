(ns vamtyc.query-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [honey.sql :as hsql]
   [vamtyc.query :as sut]))

(deftest make-field-test
  (testing "Can make pg-sql compliant field name"
    (is (= :path
           (sut/make-field "path")))
    (is (= :path_to_field
           (sut/make-field "path-to-field")))
    (is (= :path_to_field
           (sut/make-field "path-to-field  ")))
    (is (= :resource_code
           (sut/make-field :resource "code")))
    (is (= :resource_path_elem
           (sut/make-field :resource "path" "elem ")))
    (is (= :resource_path_elem
           (sut/make-field :resource "path" :elem)))
    (is (= :resource_path
           (sut/make-field :resource "path" nil)))))

(deftest make-sql-map-test
  (testing "Can make base sql map to filter results on"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource")]
           (-> :Resource sut/make-sql-map hsql/format)))))

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

(deftest extract-path-test
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

(deftest contains-text-test
  (testing "Can filter for occurrence of term on text field"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Route "
                 "WHERE CAST(name AS text) LIKE ?")
            "%read-%"]
           (-> (sut/make-sql-map :Route)
               (sut/contains-text {:type  :Queryp
                                   :code  "/Coding/filters?code=text"
                                   :desc  "Filter Route by name"
                                   :name  :name
                                   :path  [{:name     "name"}]
                                   :of    :Route}
                                  {"name" "read-"})
               (hsql/format))))))

(deftest match-exact-test
  (testing "Can filter for exact matching of term with text field"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "WHERE CAST(of AS text) = ?")
            "\"Route\""]
           (-> (sut/make-sql-map :Resource)
               (sut/match-exact {:type  :Queryp
                                 :code  "/Coding/filters?code=keyword"
                                 :desc  "Filter Resource by of"
                                 :name  :of
                                 :path  [{:name     "of"}]
                                 :of    :Resource}
                                {"of" "Route"})
               (hsql/format))))))

(deftest page-offset-test
  (testing "Can set page offset"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "OFFSET ?")
            20]
           (-> (sut/make-sql-map :Resource)
               (sut/page-offset 20)
               (hsql/format))))
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "OFFSET ?")
            5]
           (-> (sut/make-sql-map :Resource)
               (sut/page-offset "5")
               (hsql/format))))))

(deftest page-size-test
  (testing "Can set page size"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "LIMIT ?")
            128]
           (-> (sut/make-sql-map :Resource)
               (sut/page-size 128)
               (hsql/format))))
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "LIMIT ?")
            10]
           (-> (sut/make-sql-map :Resource)
               (sut/page-size "10")
               (hsql/format))))))

(deftest total-test
  (testing "Can calc total query items"
    (is (= [(str "SELECT COUNT(*) AS count "
                 "FROM Resource")]
           (-> (sut/make-sql-map :Resource)
               (sut/total)
               (hsql/format))))))
