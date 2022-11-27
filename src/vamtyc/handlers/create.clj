(ns vamtyc.handlers.create
  (:require [ring.util.response :refer  [content-type response]]
            [ring.util.request  :refer  [body-string]]
            [vamtyc.data.store  :as     store]
            [vamtyc.utils.path  :as     path]
            [clojure.data.json  :as     json]))

(defn handler [route]
  (fn [req]
    (let [url       (:uri req)
          res-type  (-> route :path path/get-res-type keyword)
          body      (-> req body-string (json/read-str :key-fn keyword))]
      (-> res-type
          (store/create body)
          (json/write-str)
          (response)
          (content-type "application/json")))))
