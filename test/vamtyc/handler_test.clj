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
          db-queryps (fn [_types _params] [])
          db-create (fn [type body] (merge {:type type
                                            :id "person"
                                            :url (str "/" type "/person")
                                            :created "2023-04-16 16:36:14.291 +0200"
                                            :modified "2023-04-16 16:36:14.291 +0200"
                                            :etag "999"}
                                           body))]

      (is (= {:status 201
              :headers {"Location" "/Resource/person"
                        "ETag" "999"}
              :body {:type "Resource"
                     :id "person"
                     :url "/Resource/person"
                     :created "2023-04-16 16:36:14.291 +0200"
                     :modified "2023-04-16 16:36:14.291 +0200"
                     :etag "999"
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
                         db-create
                         db-queryps))))))

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
          fields-queryp {:type :Queryp
                         :code "/Coding/wellknown-params?code=fields"
                         :desc "Reduce response payload by filtering properties"
                         :name :_fields
                         :of :*}
          resource {:type "Resource"
                    :id "person"
                    :url "/Resource/person"
                    :created "2023-04-16 16:36:14.291 +0200"
                    :modified "2023-04-16 16:36:14.291 +0200"
                    :etag "999"
                    :desc "Human being"
                    :of :Person
                    :status "/Coding/resource-statuses?code=pending"
                    :routes "/List?_of=Route&res-type=Person"}
          db-fetch (fn [_type id] (if (= "person" id) resource nil))
          db-queryp (fn [_types _params] [fields-queryp])]
      (is (= {:status 200
              :headers {"ETag" "999"}
              :body resource}
             (sut/rread {:params {"_id" "person"}}
                        route
                        db-fetch
                        db-queryp)))
      (is (= {:status 200
              :headers {"ETag" "999"}
              :body {:type "Resource", :id "person"}}
             (sut/rread {:params {"_id" "person", "_fields" "id,type"}}
                        route
                        db-fetch
                        db-queryp)))
      (is (= {:status 404
              :headers {}
              :body "Not found"}
             (sut/rread {:params {"_id" "non-existing"}}
                        route
                        db-fetch
                        db-queryp))))))

(deftest upsert-test
  (testing "Can handle PUT /:type/:id requests"
    (let [route {:type :Route
                 :method :PUT
                 :path [{:name "_type"
                         :code "/Coding/wellknown-params?code=type"
                         :value "Resource"}
                        {:name "_id"
                         :code "/Coding/wellknown-params?code=id"}]
                 :name "upsert-resource"
                 :code "/Coding/handlers?code=upsert"
                 :resource "/Resource/resource"}
          resource {:type "Resource"
                    :id "person"
                    :url "/Resource/person"
                    :created "2023-04-16 16:36:14.291 +0200"
                    :modified "2023-04-16 16:36:14.291 +0200"
                    :desc "Human being"
                    :of :Person
                    :status "/Coding/resource-statuses?code=pending"
                    :routes "/List?_of=Route&res-type=Person"}
          db-queryps (fn [_types _params] [])
          db-create (fn [type id body] (merge {:type type
                                               :id id
                                               :url (str "/" type "/" id)
                                               :created "2023-04-16 16:36:14.291 +0200"
                                               :modified "2023-04-16 16:36:14.291 +0200"}
                                              body))
          db-edit (fn [_type _id body] (merge resource
                                              body
                                              {:modified "2023-04-17 16:36:14.291 +0200"}))
          db-fetch (fn [_type id] (if (= "person" id) resource nil))]

      (is (= {:status 201
              :headers {"Location" "/Resource/new-person"}
              :body {:type "Resource"
                     :id "new-person"
                     :url "/Resource/new-person"
                     :created "2023-04-16 16:36:14.291 +0200"
                     :modified "2023-04-16 16:36:14.291 +0200"
                     :desc "Human being"
                     :of :Person
                     :status "/Coding/resource-statuses?code=pending"
                     :routes "/List?_of=Route&res-type=Person"}}
             (sut/upsert {:params {"_id" "new-person"}
                          :body {:desc "Human being"
                                 :of :Person
                                 :status "/Coding/resource-statuses?code=pending"
                                 :routes "/List?_of=Route&res-type=Person"}}
                         route
                         db-fetch
                         db-edit
                         db-create
                         db-queryps)))
      (is (= {:status 200
              :headers {}
              :body {:type "Resource"
                     :id "person"
                     :url "/Resource/person"
                     :created "2023-04-16 16:36:14.291 +0200"
                     :modified "2023-04-17 16:36:14.291 +0200"
                     :desc "Existing person"
                     :of :Person
                     :status "/Coding/resource-statuses?code=pending"
                     :routes "/List?_of=Route&res-type=Person"}}
             (sut/upsert {:params {"_id" "person"}
                          :body {:desc "Existing person"
                                 :of :Person
                                 :status "/Coding/resource-statuses?code=pending"
                                 :routes "/List?_of=Route&res-type=Person"}}
                         route
                         db-fetch
                         db-edit
                         db-create
                         db-queryps))))))
(deftest delete-test
  (testing "Can handle DELETE /:type/:id requests"
    (let [route {:type :Route
                 :method :DELETE
                 :path [{:name "_type"
                         :code "/Coding/wellknown-params?code=type"
                         :value "Resource"}
                        {:name "_id"
                         :code "/Coding/wellknown-params?code=id"}]
                 :name :delete-resource
                 :code "/Coding/handlers?code=delete"
                 :resource "/Resource/resource"}
          resource {:type "Resource"
                    :id "person"
                    :url "/Resource/person"
                    :created "2023-04-16 16:36:14.291 +0200"
                    :modified "2023-04-16 16:36:14.291 +0200"
                    :desc "Human being"
                    :of :Person
                    :status "/Coding/resource-statuses?code=pending"
                    :routes "/List?_of=Route&res-type=Person"}
          db-queryps (fn [_types _params] [])
          db-delete (fn [_type id] (if (= "person" id) resource nil))]

      (is (= {:status 204
              :headers {}
              :body nil}
             (sut/delete {:params {"_id" "person"}}
                         route
                         db-delete
                         db-queryps)))
      (is (= {:status 404
              :headers {}
              :body "Not found"}
             (sut/delete {:params {"_id" "non-existing"}}
                         route
                         db-delete
                         db-queryps))))))

(deftest notfound-test
  (testing "Can handle not-found"
    (let [route {:type :Route
                 :name :not-found
                 :code "/Coding/handlers?code=not-found"
                 :routes "/List?_of=Route"}]
      (is (= {:status 404
              :headers {}
              :body "Not found, explore available routes at: /List?_of=Route&method=GET"}
             (sut/notfound {:request-method :GET
                            :params {"_id" "465a55be"}}
                           route)))
      (is (= {:status 404
              :headers {}
              :body "Not found, explore available routes at: /List?_of=Route&method=POST"}
             (sut/notfound {:request-method :POST
                            :params {"_id" "465a55be"}}
                           route))))))
