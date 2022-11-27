(ns vamtyc.utils.response
  (:require [clojure.data.json :as json]))

(defn ok [result]
  (let [body (json/write-str result)]
    {:status   200
     :headers  {"Content-Type" "application/json"}
     :body     body}))

(comment
  (ok {:geet    "Hello"
       :name    "John"})
  )
