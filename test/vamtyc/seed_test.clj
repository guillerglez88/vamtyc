(ns vamtyc.seed-test
  (:require [vamtyc.seed :as sut]
            [clojure.test :as t :refer [deftest testing is]]))

(deftest hset-code-test
  (testing "Can convert Resource :code prop into a #{\"hash\" \"set\"}"
    (is (= #{"/Coding/wellknown-resources?code=ddl"
              "/Coding/wellknown-ddl?code=create-storage"
              "/Coding/ddl-langs?code=pg-sql"}
            (sut/hset-code ["/Coding/wellknown-resources?code=ddl"
                            "/Coding/wellknown-ddl?code=create-storage"
                            "/Coding/ddl-langs?code=pg-sql"])))
    (is (= #{"/Coding/wellknown-resources?code=ddl"}
           (sut/hset-code "/Coding/wellknown-resources?code=ddl")))))

(deftest assoc-by-code-test
  (testing "Can create map using codes as the keys"
    (let [res {:type      :Ddl
               :id        "table"
               :desc      "Create postgres table DDL"
               :code      ["/Coding/wellknown-resources?code=ddl"
                           "/Coding/wellknown-ddl?code=create-storage"
                           "/Coding/ddl-langs?code=pg-sql"]
               :template  ["
                           CREATE TABLE IF NOT EXISTS public.%1$s (
                             id          TEXT             NOT NULL,
                             resource    JSONB            NOT NULL,
                             created     timestamptz      NOT NULL,
                             modified    timestamptz      NOT NULL,
                           CONSTRAINT %1$s_pk PRIMARY KEY (id));"
                           "/Coding/ddl-params?code=res-type"]}]
      (is (= {"/Coding/wellknown-resources?code=ddl" res
              "/Coding/wellknown-ddl?code=create-storage" res
              "/Coding/ddl-langs?code=pg-sql" res}
             (sut/assoc-by-code res))))))
