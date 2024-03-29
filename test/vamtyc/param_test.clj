(ns vamtyc.param-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [vamtyc.param :as sut]))

(deftest queryp->param-test
  (testing "Can map Queryp to param map"
    (is (= [{"_of" :List}
            "/Coding/wellknown-params?code=of&name=_of"]
           (sut/queryp->param {:type :Queryp
                               :code "/Coding/wellknown-params?code=of"
                               :name :_of
                               :default :List})))
    (is (= [{"_limit" 128}
            "/Coding/wellknown-params?code=limit&name=_limit"]
           (sut/queryp->param {:type :Queryp
                               :code "/Coding/wellknown-params?code=limit"
                               :name :_limit
                               :default 128})))
    (is (= [{"_created" nil}
            "/Coding/filters?code=date&name=_created"]
           (sut/queryp->param {:type :Queryp
                               :code "/Coding/filters?code=date"
                               :name :_created})))))

(deftest merge-param-test
  (testing "Can merge multiple param maps"
    (is (= [{"_of" :List
             "_created" nil
             "_limit" 128}
            "/Coding/wellknown-params?code=of&name=_of"
            "/Coding/wellknown-params?code=limit&name=_limit"
            "/Coding/filters?code=date&name=_created"]
           (sut/merge-param [[{"_of" :List}, "/Coding/wellknown-params?code=of&name=_of"]
                             [{"_limit" 128}, "/Coding/wellknown-params?code=limit&name=_limit"]
                             [{"_created" nil}, "/Coding/filters?code=date&name=_created"]])))))

(deftest queryps->param-test
  (testing "Can map Queryp coll to params map"
    (is (= [{"_of" :List
             "_created" nil
             "_limit" 128}
            "/Coding/wellknown-params?code=of&name=_of"
            "/Coding/wellknown-params?code=limit&name=_limit"
            "/Coding/filters?code=date&name=_created"]
           (sut/queryps->param [{:type :Queryp
                                 :code "/Coding/wellknown-params?code=of"
                                 :name :_of
                                 :default :List}
                                {:type :Queryp
                                 :code "/Coding/wellknown-params?code=limit"
                                 :name :_limit
                                 :default 128}
                                {:type :Queryp
                                 :code "/Coding/filters?code=date"
                                 :name :_created}])))))

(deftest route-path-cmp->param-test
  (testing "Can map route-path-compontent to params map"
    (is (= [{"_type" "Resource"}
            "/Coding/wellknown-params?code=type&name=_type"]
           (sut/route-path-cmp->param {:name "_type"
                                       :code "/Coding/wellknown-params?code=type"
                                       :value "Resource"})))
    (is (= [{"_id" nil}
            "/Coding/wellknown-params?code=id&name=_id"]
           (sut/route-path-cmp->param {:name "_id"
                                       :code "/Coding/wellknown-params?code=id"})))))

(deftest route->param-test
  (testing "Can map route to params map"
    (is (= [{"_type" "Resource"}
            "/Coding/wellknown-params?code=type&name=_type"]
           (sut/route->param {:type :Route
                              :method :POST
                              :name :create-resource
                              :path [{:name "_type"
                                      :code "/Coding/wellknown-params?code=type"
                                      :value "Resource"}]})))
    (is (= [{"_type" "Resource"
             "_id" nil}
            "/Coding/wellknown-params?code=type&name=_type"
            "/Coding/wellknown-params?code=id&name=_id"]
           (sut/route->param {:type :Route
                              :method :GET
                              :name :read-resource
                              :path [{:name "_type"
                                      :code "/Coding/wellknown-params?code=type"
                                      :value "Resource"}
                                     {:name "_id"
                                      :code "/Coding/wellknown-params?code=id"}]})))
    (is (= [{}]
           (sut/route->param {:type :Route
                              :code "/Coding/handlers?code=not-found"
                              :name :not-found})))))

(deftest sanityze-qs-name-test
  (testing "Can extract querystring name without modifiers"
    (is (= "name"
           (sut/sanityze-qs-name "name")))
    (is (= "name"
           (sut/sanityze-qs-name :name)))
    (is (= "name"
           (sut/sanityze-qs-name "name:exact")))))

(deftest url-test
  (testing "Can extract relative ring request url"
    (is (= "/List?_of=Route&_limit=3"
           (sut/url {:uri "/List" :query-string "_of=Route&_limit=3"})))
    (is (= "/List"
           (sut/url {:uri "/List"})))
    (is (= "/"
           (sut/url {:uri "/"})))))

(deftest req->param-test
  (testing "Can map ring request to params map"
    (is (= [{"_id" "e6873cb6"
             "_fields" "path.name,id"}]
           (sut/req->param {:uri "/Person"
                            :query-string "_field=path.name,id"
                            :params {:_id "e6873cb6", :_fields "path.name,id"}})))
    (is (= [{"name" "john"}]
           (sut/req->param {:uri "/Person"
                            :query-string "name=john"
                            :params {:name "john"}})))
    (is (= [{"name" "John"}]
           (sut/req->param {:uri "/Person"
                            :query-string "name:exact=John"
                            :params {"name:exact" "John"}})))
    (is (= [{"name" "John"}]
           (sut/req->param {:uri "/Person"
                            :query-string "name=John"
                            :params {"name" "John"}})))))

(deftest get-value-test
  (testing "Can resolve /Coding/wellknown-params?code=type"
    (let [param [{"_of" :List
                  "_created" nil
                  "_limit" 128
                  "_type" "Resource"
                  "_id" "ecf3cf94"}
                 "/Coding/wellknown-params?code=type&name=_type"
                 "/Coding/wellknown-params?code=id&name=_id"
                 "/Coding/wellknown-params?code=of&name=_of"
                 "/Coding/wellknown-params?code=limit&name=_limit"
                 "/Coding/filters?code=date&name=_created"]]
      (is (= "Resource"
             (sut/get-value param "/Coding/wellknown-params?code=type")))
      (is (= "ecf3cf94"
             (sut/get-value param "/Coding/wellknown-params?code=id")))
      (is (= 128
             (sut/get-value param "/Coding/wellknown-params?code=limit")))
      (is (= nil
             (sut/get-value param "/Coding/wellknown-params?code=fields"))))))

(deftest get-values-test
  (testing "Can resolve bulk params in a single function call"
    (let [param [{"_of" :List
                  "_created" nil
                  "_limit" 128
                  "_type" "Resource"
                  "_id" "ecf3cf94"}
                 "/Coding/wellknown-params?code=type&name=_type"
                 "/Coding/wellknown-params?code=id&name=_id"
                 "/Coding/wellknown-params?code=of&name=_of"
                 "/Coding/wellknown-params?code=limit&name=_limit"
                 "/Coding/filters?code=date&name=_created"]]
      (is (= ["Resource" "ecf3cf94" 128 nil]
             (sut/get-values param "/Coding/wellknown-params?code=type"
                                   "/Coding/wellknown-params?code=id"
                                   "/Coding/wellknown-params?code=limit"
                                   "/Coding/wellknown-params?code=fields"))))))

(deftest get-name-test
  (testing "Can resolve param name by code"
    (let [param [{"_of" :List
                  "_created" nil
                  "_limit" 128
                  "_type" "Resource"
                  "_id" "ecf3cf94"}
                 "/Coding/wellknown-params?code=type&name=_type"
                 "/Coding/wellknown-params?code=id&name=_id"
                 "/Coding/wellknown-params?code=of&name=_of"
                 "/Coding/wellknown-params?code=limit&name=_limit"
                 "/Coding/filters?code=date&name=_created"]]
      (is (= "_type"
             (sut/get-name param "/Coding/wellknown-params?code=type")))
      (is (= "_limit"
             (sut/get-name param "/Coding/wellknown-params?code=limit")))
      (is (= "_id"
             (sut/get-name param "/Coding/wellknown-params?code=id")))
      (is (= "_of"
             (sut/get-name param "/Coding/wellknown-params?code=of"))))))

(deftest get-names-test
  (testing "Can resolve param names by codes"
    (let [param [{"_of" :List
                  "_created" nil
                  "_limit" 128
                  "_type" "Resource"
                  "_id" "ecf3cf94"}
                 "/Coding/wellknown-params?code=type&name=_type"
                 "/Coding/wellknown-params?code=id&name=_id"
                 "/Coding/wellknown-params?code=of&name=_of"
                 "/Coding/wellknown-params?code=limit&name=_limit"
                 "/Coding/filters?code=date&name=_created"]]
      (is (= ["_type" "_limit" "_id" "_of"]
             (sut/get-names param "/Coding/wellknown-params?code=type"
                                  "/Coding/wellknown-params?code=limit"
                                  "/Coding/wellknown-params?code=id"
                                  "/Coding/wellknown-params?code=of"))))))
