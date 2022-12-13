(ns vamtyc.utils.fields-test
  (:require [vamtyc.utils.fields :as sut]
            [clojure.test :as t]))

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

(t/deftest parse-fields
  (t/testing "Can parse _fields querp expression value"
    (t/is (= [["name"]]
             (sut/parse-expr "name")))
    (t/is (= [["name"] ["type"]]
             (sut/parse-expr "name,type")))
    (t/is (= [["name"] ["path" "name"]]
             (sut/parse-expr "name,path.name")))
    (t/is (= [["path" "name"]]
             (sut/parse-expr "path.name")))
    (t/is (= []
             (sut/parse-expr "")))))

(t/deftest select-path
  (t/testing "Can select deep path into a projected map"
    (t/is (= {:path [{:name "_type"} {:name "_id"}]}
             (sut/select-path-into ["path" "name"] (make-route) {})))
    (t/is (= {:name {:family "Smith"}}
             (sut/select-path-into ["name" "family"] (make-person) {})))
    (t/is (= {:name {:family "Smith"}
              :gender :male}
             (sut/select-path-into ["name" "family"] (make-person) {:gender :male})))
    (t/is (= {:type :Person}
             (sut/select-path-into ["type"] (make-person) {:type :Animal})))))

(t/deftest str-field-expr
  (t/testing "Flat multi-expr field value"
    (t/is (= "name,path.name"
             (sut/flat-expr ["name" "path.name"])))
    (t/is (= "name,path.name"
             (sut/flat-expr "name,path.name")))
    (t/is (= ""
             (sut/flat-expr "")))
    (t/is (= ""
             (sut/flat-expr nil)))))

(t/deftest select-fields
  (t/testing "Can select map projection from _fields expression value"
    (t/is (= {:type :Route
              :path [{:name "_type"} {:name "_id"}]}
             (sut/select-fields (make-route) "type,path.name")))
    (t/is (= {:type :Route
              :path [{} {}]}
             (sut/select-fields (make-route) "type,path.fake")))))
