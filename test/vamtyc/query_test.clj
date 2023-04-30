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

(deftest select-all-test
  (testing "Can make base sql map to filter results on"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource")]
           (-> :Resource sut/all-by-type hsql/format)))))

(deftest extract-prop-test
  (testing "Can expose jsonb prop for filtering"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS code ON TRUE")
            "code"]
           (-> (sut/all-by-type :Resource)
               (sut/extract-prop :resource "code" :code)
               (hsql/format))))))

(deftest extract-coll-test
  (testing "Can expose jsonb prop collection elements for filtering"
    (is (= [(str "SELECT id, resource, created, modified "
                  "FROM Resource "
                  "INNER JOIN JSONB_EXTRACT_PATH(resource, ?) AS resource_path ON TRUE "
                  "INNER JOIN JSONB_ARRAY_ELEMENTS(resource_path) AS path ON TRUE")
            "path"]
           (-> (sut/all-by-type :Resource)
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
           (-> (sut/all-by-type :Route)
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
           (-> (sut/all-by-type :Route)
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
           (-> (sut/all-by-type :Resource)
               (sut/match-exact {:type  :Queryp
                                 :code  "/Coding/filters?code=keyword"
                                 :desc  "Filter Resource by of"
                                 :name  :of
                                 :path  [{:name     "of"}]
                                 :of    :Resource}
                                {"of" "Route"})
               (hsql/format))))))

(deftest paginate-test
  (testing "Can paginate"
    (is (= [(str "SELECT id, resource, created, modified "
                 "FROM Resource "
                 "LIMIT ? OFFSET ?")
            128, 10]
           (-> (sut/all-by-type :Resource)
               (sut/paginate 10 128)
               (hsql/format))))))

(deftest total-test
  (testing "Can calc total query items"
    (is (= [(str "SELECT COUNT(*) AS count "
                 "FROM Resource")]
           (-> (sut/all-by-type :Resource)
               (sut/total)
               (hsql/format))))))

(deftest calc-hash-test
  (testing "Can calc digest from string"
    (is (= "JO+kEGAUhfQ1yiplXuW8FHB/AwySOte+kynrRcB/xAw="
           (sut/calc-hash "SELECT id, resource, created, modified FROM Resource LIMIT ? OFFSET ?")))))

(deftest clean-url-test
  (testing "Can clean url queryp values"
    (is (= "/List?_of=&code=&name="
           (sut/clean-url "/List?_of=Resource&name=read-resource&code=my-code")))
    (is (= "/Resource/route?"
           (sut/clean-url "/Resource/route"))))
  (testing "Can clean url keeping some values"
    (is (= "/List?_of=Resource&code=&name="
           (sut/clean-url "/List?_of=Resource&name=read-resource&code=my-code"
                          #{:_of})))))

(deftest make-url-test
  (testing "Can make url from params"
    (is (= "/List?_created=&_id=ecf3cf94&_limit=128&_of=Resource"
           (sut/make-url [{"_of" "Resource"
                           "_created" ""
                           "_limit" 128
                           "_type" "List"
                           "_id" "ecf3cf94"}
                          "/Coding/wellknown-params?code=type&name=_type"
                          "/Coding/wellknown-params?code=id&name=_id"
                          "/Coding/wellknown-params?code=of&name=_of"
                          "/Coding/wellknown-params?code=limit&name=_limit"
                          "/Coding/filters?code=date&name=_created"])))))
