(ns vamtyc.nerves.search
  (:require [ring.util.response :refer [response]]
            [vamtyc.utils.request :refer [relative-url]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as fields]
            [vamtyc.queries.core :as queries]
            [clojure.string :as str]
            [vamtyc.utils.routes :as uroutes]
            [vamtyc.utils.queryp :as uqueryp]))

(defn make-result-set [items url]
  {:type    :List
   :url     url
   :items   items
   :nav     {:first nil
             :prev  nil
             :next  nil
             :last  nil}})

(defn handler [req tx _app]
  (let [url     (relative-url req)
        fields  (-> req :params (get "_fields") (fields/flat-expr))
        type    (-> req :vamtyc/route :path uroutes/type)
        of      (-> req :vamtyc/queryp uqueryp/of)]
    (->> (queries/search-query req tx)
         (store/list tx (or of type))
         (into [])
         (#(make-result-set % url))
         (#(fields/select-fields % fields))
         (response))))
