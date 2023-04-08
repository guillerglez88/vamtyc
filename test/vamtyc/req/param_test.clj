(ns vamtyc.req.param-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [vamtyc.req.param :as sut]))

(deftest queryp->param-test
  (testing "Can map Queryp to param map"
    (is (= {:_of :List}
           (sut/queryp->param {:type :Queryp, :name :_of, :value :List})))
    (is (= {:_limit 128}
           (sut/queryp->param {:type :Queryp, :name :_limit, :value 128})))
    (is (= {:_created nil}
           (sut/queryp->param {:type :Queryp, :name :_created})))))

(deftest queryps->param-test
  (testing "Can map Queryp coll to params map"
    (is (= {:_of :List
            :_created nil
            :_limit 128}
           (sut/queryps->param [{:type :Queryp, :name :_of, :value :List}
                                {:type :Queryp, :name :_limit, :value 128}
                                {:type :Queryp, :name :_created}])))))

(deftest route-path-cmp->param-test
  (testing "Can map route-path-compontent to params map"
    (is (= {"_type" "Resource"}
           (sut/route-path-cmp->param {:name "_type" :value "Resource"})))
    (is (= {"_id" nil}
           (sut/route-path-cmp->param {:name "_id"})))))

(deftest route->param-test
  (testing "Can map route to params map"
    (is (= {"_type" "Resource"}
           (sut/route->param {:type :Route
                              :method :POST
                              :name :create-resource
                              :path [{:name "_type", :value "Resource"}]})))
    (is (= {"_type" "Resource", "_id" nil}
           (sut/route->param {:type :Route
                              :method :GET
                              :name :read-resource
                              :path [{:name "_type" :value "Resource"}
                                     {:name "_id"}]})))
    (is (= {}
           (sut/route->param {:type :Route
                              :name :not-found})))))

(deftest sanityze-qs-name-test
  (testing "Can extract querystring name without modifiers"
    (is (= :name
           (sut/sanityze-qs-name "name")))
    (is (= :name
           (sut/sanityze-qs-name :name)))
    (is (= :name
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
    (is (= {:__url "/Person?_field=path.name,id", :_id "e6873cb6", :_fields "path.name,id"}
           (sut/req->param {:uri "/Person"
                            :query-string "_field=path.name,id"
                            :params {:_id "e6873cb6", :_fields "path.name,id"}})))
    (is (= {:__url "/"}
           (sut/req->param {:uri "/"})))
    (is (= {:__url "/Person?name=john", :name "john"}
           (sut/req->param {:uri "/Person"
                            :query-string "name=john"
                            :params {:name "john"}})))
    (is (= {:__url "/Person?name:exact=John", :name "John"}
           (sut/req->param {:uri "/Person"
                            :query-string "name:exact=John"
                            :params {"name:exact" "John"}})))
    (is (= {:__url "/Person?name=John", :name "John"}
           (sut/req->param {:uri "/Person"
                            :query-string "name=John"
                            :params {"name" "John"}})))))
