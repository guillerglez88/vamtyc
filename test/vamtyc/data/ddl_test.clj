(ns vamtyc.data.ddl-test
  (:require [vamtyc.data.ddl :as sut]
            [clojure.test :as t :refer [deftest testing is]]))

(deftest resource->params-test
  (testing "Can extract DDL params from resource"
    (let [route {:type      :Route
                 :method    :POST
                 :path      [{:name     "_type"
                              :code     "/Coding/wellknown-params?code=type"
                              :value    "Resource"}]
                 :name      :create-resource
                 :code      "/Coding/handlers?code=create"
                 :resource  "/Resource/resource"}]
      (is (= ["route"]
             (sut/resource->params route [sut/tpl-param-res-type])))
      (is (= ["route" nil]
             (sut/resource->params route [sut/tpl-param-res-type
                                          sut/tpl-param-seq-id]))))))

(deftest seq->params-test
  (testing "Can extract DDL params from Seq"
    (let [seq {:type      :Seq
               :id        "etag"
               :desc      "E-Tag http header generator"
               :code      "/Coding/wellknown-resources?code=seq"
               :start     1
               :inc       1
               :cache     12}]
      (is (= ["etag" 1 1 12]
             (sut/seq->params seq [sut/tpl-param-seq-id
                                   sut/tpl-param-seq-start
                                   sut/tpl-param-seq-inc
                                   sut/tpl-param-seq-cache])))
      (is (= [12 1 "etag" 1 12]
             (sut/seq->params seq [sut/tpl-param-seq-cache
                                   sut/tpl-param-seq-start
                                   sut/tpl-param-seq-id
                                   sut/tpl-param-seq-inc
                                   sut/tpl-param-seq-cache])))
      (is (= ["etag" 1 1 nil 12]
             (sut/seq->params seq [sut/tpl-param-seq-id
                                   sut/tpl-param-seq-start
                                   sut/tpl-param-seq-inc
                                   sut/tpl-param-res-type
                                   sut/tpl-param-seq-cache]))))))

(deftest make-seq-ddl-test
  (testing "Can make Sequence DDL"
    (let [tpl ["CREATE SEQUENCE IF NOT EXISTS public.%1$s
                  MINVALUE 1
                  NO MAXVALUE
                  START %2$d
                  INCREMENT BY %3$d
                  CACHE %4$d
                  NO CYCLE"
                "/Coding/ddl-params?code=seq-id"
                "/Coding/ddl-params?code=seq-start"
                "/Coding/ddl-params?code=seq-inc"
                "/Coding/ddl-params?code=seq-cache"]
          seq {:type      :Seq
               :id        "etag"
               :desc      "E-Tag http header generator"
               :code      "/Coding/wellknown-resources?code=seq"
               :start     1
               :inc       1
               :cache     12}]
      (is (= "CREATE SEQUENCE IF NOT EXISTS public.etag
                  MINVALUE 1
                  NO MAXVALUE
                  START 1
                  INCREMENT BY 1
                  CACHE 12
                  NO CYCLE"
             (sut/make-seq-ddl tpl seq))))))

(deftest make-res-ddl-test
  (testing "Can make resource DDL"
    (let [tpl ["CREATE TABLE IF NOT EXISTS public.%1$s (
                  id          TEXT             NOT NULL,
                  resource    JSONB            NOT NULL,
                  created     timestamptz      NOT NULL,
                  modified    timestamptz      NOT NULL,
                CONSTRAINT %1$s_pk PRIMARY KEY (id));"
                "/Coding/ddl-params?code=res-type"]
          res {:type      :Resource
               :id        "resource"
               :desc      "REST resource"
               :status    "/Coding/resource-statuses?code=active"
               :of        :Resource
               :routes    "/List?_of=Route&res-type=Resource"}]
      (is (= "CREATE TABLE IF NOT EXISTS public.resource (
                  id          TEXT             NOT NULL,
                  resource    JSONB            NOT NULL,
                  created     timestamptz      NOT NULL,
                  modified    timestamptz      NOT NULL,
                CONSTRAINT resource_pk PRIMARY KEY (id));"
             (sut/make-res-ddl tpl res))))))
