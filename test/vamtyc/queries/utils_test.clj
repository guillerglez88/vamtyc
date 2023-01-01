(ns vamtyc.queries.utils-test
  (:require
   [vamtyc.queries.utils :as sut]
   [clojure.test :refer [deftest testing is]]
   [honey.sql :as hsql]))

(deftest make-sql-map
  (testing "Can make base sql map to filter results on"
    (is (= [(str"SELECT id, resource, created, modified "
                "FROM Resource")]
           (-> :Resource sut/make-sql-map hsql/format)))))

(deftest jsonb-prop-access
  (testing "Can expose jsonb props for filtering"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS code ON TRUE")
            "code"]
           (-> (sut/make-sql-map :Resource)
               (sut/extract-prop :resource {:name "code"} :code)
               (hsql/format))))
    (is (= [(str "SELECT id, resource, created, modified "
                  "FROM Resource "
                  "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS resource_path ON TRUE "
                  "INNER JOIN JSONB_ARRAY_ELEMENTS(resource_path) AS path ON TRUE")
             "path"]
            (-> (sut/make-sql-map :Resource)
                (sut/extract-coll :resource {:name "path"} :path)
                (hsql/format))))))

(deftest prop-alias
  (testing "Can build prop alias"
    (is (= :resource_code
           (sut/make-prop-alias :resource {:name "code"})))
    (is (= :resource_path
           (sut/make-prop-alias :resource {:name "path"
                                           :collection true})))
    (is (= :resource_path_elem
           (sut/make-prop-alias :resource {:name "path"
                                             :collection true} "elem")))))

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
