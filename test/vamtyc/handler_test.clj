(ns vamtyc.handler-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [vamtyc.handler :as sut]))

(deftest create-test
  (testing "Can handle POST /:type requests"
    (let [route {:type :Route
                 :method :POST
                 :path [{:name "_type"
                         :code "/Coding/wellknown-params?code=type"
                         :value "Resource"}]
                 :name :create-resource
                 :code "/Coding/handlers?code=create"
                 :resource "/Resource/resource"}
          db-create (fn [type body] (merge {:type type
                                            :id "person"
                                            :url (str "/" type "/person")
                                            :created "2023-04-16 16:36:14.291 +0200"
                                            :modified "2023-04-16 16:36:14.291 +0200"}
                                           body))]

      (is (= {:status 201
              :headers {"Location" "/Resource/person"}
              :body {:type "Resource"
                     :id "person"
                     :url "/Resource/person"
                     :created "2023-04-16 16:36:14.291 +0200"
                     :modified "2023-04-16 16:36:14.291 +0200"
                     :desc "Human being"
                     :of :Person
                     :status "/Coding/resource-statuses?code=pending"
                     :routes "/List?_of=Route&res-type=Person"}}
             (sut/create {:params {},
                          :body {:desc "Human being"
                                 :of :Person
                                 :status "/Coding/resource-statuses?code=pending"
                                 :routes "/List?_of=Route&res-type=Person"}}
                         route
                         db-create))))))

(deftest rread-test
  (testing "Can handle GET /:type/:id requests"
    (let [route {:type      :Route
                 :method    :GET
                 :path      [{:name     "_type"
                               :code     "/Coding/wellknown-params?code=type"
                               :value    "Resource"}
                             {:name     "_id"
                               :code     "/Coding/wellknown-params?code=id"}]
                 :name      :read-resource
                 :code      "/Coding/handlers?code=read"
                 :resource  "/Resource/resource"}
          resource {:type "Resource"
                    :id "person"
                    :url "/Resource/person"
                    :created "2023-04-16 16:36:14.291 +0200"
                    :modified "2023-04-16 16:36:14.291 +0200"
                    :desc "Human being"
                    :of :Person
                    :status "/Coding/resource-statuses?code=pending"
                    :routes "/List?_of=Route&res-type=Person"}
          db-fetch (fn [_type _id] resource)]

      (is (= {:status 200
              :headers {}
              :body resource}
             (sut/rread {:params {"_id" "person"}} route db-fetch))))))
