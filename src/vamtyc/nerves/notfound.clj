(ns vamtyc.nerves.notfound
  (:require [ring.util.response :refer [not-found]]
            [clojure.string :as str]))

(defn handler [req _tx _app]
  (let [method  (-> req :method name str/upper-case)]
    (-> (str "Not found, "
             "explore available routes at: "
             "/List?_of=Route&method=" method)
        (not-found))))
