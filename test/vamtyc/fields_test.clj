(ns vamtyc.fields-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [vamtyc.fields :as sut]))

(deftest parse-expr
  (testing "Can parse _fields querparam expression value"
    (is (= [["name"]]
           (sut/parse-expr "name")))
    (is (= [["name"] ["type"]]
           (sut/parse-expr "name,type")))
    (is (= [["name"] ["path" "name"]]
           (sut/parse-expr "name,path.name")))
    (is (= [["path" "name"]]
           (sut/parse-expr "path.name")))
    (is (= []
           (sut/parse-expr "")))
    (is (= []
           (sut/parse-expr nil)))))

(deftest select-path-into
  (testing "Can select deep path into a projected map"
    (let [route {:type :Route
                 :method :GET
                 :path [{:name "_type", :value "Resource"}
                        {:name "_id"}]}
          person {:type :Person
                  :name {:given ["John" "Adams"]
                         :family "Smith"}}]
      (is (= {:path [{:name "_type"} {:name "_id"}]}
            (sut/select-path-into ["path" "name"] route {})))
      (is (= {:name {:family "Smith"}}
            (sut/select-path-into ["name" "family"] person {})))
      (is (= {:name {:family "Smith"} :gender :male}
            (sut/select-path-into ["name" "family"] person {:gender :male})))
      (is (= {:type :Person}
            (sut/select-path-into ["type"] person {:type :Animal}))))))

(deftest flat-expr
  (testing "Flat multi-valued _fields queryparam"
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
    (let [route {:type :Route
                 :method :GET
                 :path [{:name "_type", :value "Resource"}
                        {:name "_id"}]}
          person {:type :Person
                  :name {:given ["John" "Adams"]
                         :family "Smith"}}]
      (is (= {:type :Route
              :path [{:name "_type"} {:name "_id"}]}
             (sut/select-fields route "type,path.name")))
      (is (= {:type :Route
              :path [{} {}]}
             (sut/select-fields route "type,path.fake")))
      (is (= person
             (sut/select-fields person nil))))))
