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
    (is (= nil
           (sut/route->param {:type :Route
                              :name :not-found})))))
