(ns vamtyc.queries.utils-test
  (:require [vamtyc.queries.utils :as sut]
            [clojure.test :as t]
            [honey.sql :as hsql]))

(t/deftest make-sql-map
  (t/testing "Can make base sql map to filter results on"
    (t/is (= [(str"SELECT id, resource, created, modified "
                  "FROM Resource")]
             (-> :Resource sut/make-sql-map hsql/format)))))

(t/deftest jsonb-prop-access
  (t/testing "Can expose jsonb props for filtering"
    (t/is (= [(str "SELECT id, resource, created, modified "
                   "FROM Resource "
                   "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS code ON TRUE")
              "code"]
             (-> (sut/make-sql-map :Resource)
                 (sut/extract-prop :resource {:name "code"} :code)
                 (hsql/format))))
    (t/is (= [(str "SELECT id, resource, created, modified "
                   "FROM Resource "
                   "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS resource_path ON TRUE "
                   "INNER JOIN JSONB_ARRAY_ELEMENTS(resource_path) AS path ON TRUE")
              "path"]
             (-> (sut/make-sql-map :Resource)
                 (sut/extract-coll :resource {:name "path"} :path)
                 (hsql/format))))))

(t/deftest prop-alias
  (t/testing "Can build prop alias"
    (t/is (= :resource_code
             (sut/make-prop-alias :resource {:name "code"})))
    (t/is (= :resource_path
             (sut/make-prop-alias :resource {:name "path"
                                             :collection true})))
    (t/is (= :resource_path_elem
             (sut/make-prop-alias :resource {:name "path"
                                             :collection true} "elem")))))

(t/deftest jsonb-deep-access
  (t/testing "Can access deep jsonb property"
    (t/is (= [(str "SELECT id, resource, created, modified "
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
