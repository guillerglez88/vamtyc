(ns vamtyc.nerves.list
  (:require [ring.util.response :refer [response]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.utils.fields :as fields]))

(defn make-result-set [items url]
  (let [id (str (java.util.UUID/randomUUID))]
    {:resourceType  :List
     :id            id
     :url           url
     :type          :result-set
     :items         items
     :nav           {:first nil
                     :prev  nil
                     :next  nil
                     :last  nil}}))

(defn handler [req tx _app]
  (let [url         (:url req)
        res-type    (-> req :body :resourceType)
        sql         (:sql req)
        fields      (-> req :params (get "_fields") (or []))]
    (->> (store/list tx res-type sql)
         (map #(fields/select-fields % fields))
         (into [])
         (#(make-result-set % url))
         (response))))
