(ns vamtyc.data.ddl
  (:require
    [clojure.string :as str]))

(def tpl-param-res-type   "/Coding/ddl-params?code=res-type")

(def tpl-param-seq-id     "/Coding/ddl-params?code=seq-id")
(def tpl-param-seq-start  "/Coding/ddl-params?code=seq-start")
(def tpl-param-seq-inc    "/Coding/ddl-params?code=seq-inc")
(def tpl-param-seq-cache  "/Coding/ddl-params?code=seq-cache")

(defn resource->params [res params]
  (let [param-map {tpl-param-res-type (-> res :type name str/lower-case)}]
    (-> (partial get param-map)
        (map params)
        (vec))))

(defn seq->params [seq params]
  (let [param-map {tpl-param-seq-id (-> seq :id name str/lower-case)
                   tpl-param-seq-start (-> seq :start)
                   tpl-param-seq-inc (-> seq :inc)
                   tpl-param-seq-cache (-> seq :cache)}]
    (-> (partial get param-map)
        (map params)
        (vec))))

(defn make-seq-ddl [[tpl & params] seq]
  (->> (seq->params seq params)
       (apply format tpl)))

(defn make-res-ddl [[tpl & params] res]
  (->> (resource->params res params)
       (apply format tpl)))
