(ns vamtyc.utils.fields-test
  (:require
   [vamtyc.utils.fields :as sut]
   [clojure.test :refer [deftest testing is]]))

(defn make-route []
  {:type        :Route
   :method      :GET
   :path        [{:name     "_type"
                  :value    "Resource"}
                 {:name     "_id"}]
   :name        :read-resource
   :code        "/Coding/nerves?code=read"
   :resource    "/Resource/resource"})

(defn make-person []
  {:type        :Person
   :name        {:given ["John" "Adams"]
                 :family "Smith"}})

(deftest parse-fields
  (testing "Can parse _fields querp expression value"
    (is (= [["name"]]
           (sut/parse-expr "name")))
    (is (= [["name"] ["type"]]
           (sut/parse-expr "name,type")))
    (is (= [["name"] ["path" "name"]]
           (sut/parse-expr "name,path.name")))
    (is (= [["path" "name"]]
           (sut/parse-expr "path.name")))
    (is (= []
           (sut/parse-expr "")))))

(deftest select-path
  (testing "Can select deep path into a projected map"
    (is (= {:path [{:name "_type"} {:name "_id"}]}
           (sut/select-path-into ["path" "name"] (make-route) {})))
    (is (= {:name {:family "Smith"}}
           (sut/select-path-into ["name" "family"] (make-person) {})))
    (is (= {:name {:family "Smith"}
            :gender :male}
           (sut/select-path-into ["name" "family"] (make-person) {:gender :male})))
    (is (= {:type :Person}
           (sut/select-path-into ["type"] (make-person) {:type :Animal})))))

(deftest str-field-expr
  (testing "Flat multi-expr field value"
    (is (= "name,path.name"
           (sut/flat-expr ["name" "path.name"])))
    (is (= "name,path.name"
           (sut/flat-expr "name,path.name")))
    (is (= ""
           (sut/flat-expr "")))
    (is (= ""
           (sut/flat-expr nil)))))

(deftest select-fields
  (testing "Can select map projection from _fields expression value"
    (is (= {:type :Route
            :path [{:name "_type"} {:name "_id"}]}
           (sut/select-fields (make-route) "type,path.name")))
    (is (= {:type :Route
            :path [{} {}]}
           (sut/select-fields (make-route) "type,path.fake")))))
