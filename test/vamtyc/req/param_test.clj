(ns vamtyc.req.param-test
  (:require [vamtyc.req.param :as sut]
            [clojure.test :as t :refer [deftest testing is]]))

(deftest queryp->param
  (testing "Can map Queryp to param map"
    (is (= {:_of :List}
           (sut/queryp->param {:type :Queryp, :name :_of, :value :List})))
    (is (= {:_limit 128}
           (sut/queryp->param {:type :Queryp, :name :_limit, :value 128})))
    (is (= {:_created nil}
           (sut/queryp->param {:type :Queryp, :name :_created})))))

(deftest queryps->param
  (testing "Can map Queryp coll to params map"
    (is (= {:_of :List
            :_created nil
            :_limit 128}
           (sut/queryps->param [{:type :Queryp, :name :_of, :value :List}
                                {:type :Queryp, :name :_limit, :value 128}
                                {:type :Queryp, :name :_created}])))))
