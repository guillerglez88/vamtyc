(ns vamtyc.modules.history
  (:require [clojure.string :as str]
            [next.jdbc :as jdbc]
            [vamtyc.data.store :as store]))

(defn ddl [name]
  (str "CREATE TABLE IF NOT EXISTS public." name "(
            id          TEXT    NOT NULL,
            resource    JSONB   NULL,
            CONSTRAINT  " name "_pk PRIMARY KEY (id));"))

(defn provision [res tx]
  (let [hist-type   (-> res :type (str "History"))
        res-type    (keyword hist-type)
        id          (str/lower-case hist-type)
        desc        (str "Represents a " hist-type " Resource")
        ddl         (ddl hist-type)
        hist-res    {:type res-type :desc desc}]
    (do
      (jdbc/execute! tx [ddl])
      (store/create tx :Resource id hist-res))))

(defn init [tx]
  (doseq [res (store/list tx :Resource)]
    (provision res tx)))
