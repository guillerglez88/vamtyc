(ns vamtyc.history
  (:require [clojure.string :as str]
            [next.jdbc :as jdbc]
            [vamtyc.data.store :as store]
            [vamtyc.data.datasource :refer [ds]]))

(defn ddl [name]
  (str "CREATE TABLE IF NOT EXISTS public." name "(
            id          TEXT    NOT NULL,
            resource    JSONB   NULL,
            CONSTRAINT  " name "_pk PRIMARY KEY (id));"))

(defn provision [res]
  (let [hist-type   (-> res :type (str "History"))
        res-type    (keyword hist-type)
        id          (str/lower-case hist-type)
        desc        (str "Represents a " hist-type " Resource")
        ddl         (ddl hist-type)
        hist-res    {:type res-type :desc desc}]
    (do
      (jdbc/execute! ds [ddl])
      (store/create :Resource id hist-res))))

(defn init []
  (for [res (store/list :Resource)]
    (provision res)))

(comment
  (init)
  )
