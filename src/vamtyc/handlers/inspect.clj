(ns vamtyc.handlers.inspect
  (:require [clojure.data.json :as json]
            [ring.util.response :refer [content-type response]]))

(defn handler [_tx req]
  (-> (json/write-str req)
      (response)
      (content-type "application/json")))
