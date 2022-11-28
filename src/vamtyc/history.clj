(ns vamtyc.history
  (:require [next.jdbc              :as jdbc]
            [vamtyc.data.store      :as store]
            [vamtyc.data.datasource :refer  [ds]]
            [clojure.string :as str]))

(defn history-ddl [name]
  (str "CREATE TABLE IF NOT EXISTS public." name "(
            id          TEXT NOT NULL,
            resource    JSONB NULL,
            CONSTRAINT  " name "_pk PRIMARY KEY (id));"))

(defn init []
  (for [res     (store/list :Resource)
        :let    [hist-type  (-> res :type (str "History"))
                 ddl        (history-ddl hist-type)
                 res-type   (keyword hist-type)
                 desc       (str "Represents a " hist-type " Resource")
                 res        {:type res-type :desc desc}
                 id         (str/lower-case hist-type)]]
    (do
      (jdbc/execute! ds [ddl])
      (store/create :Resource id res))))

(comment
  (init)
  )
