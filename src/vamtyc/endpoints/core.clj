(ns vamtyc.endpoints.core
  (:require [vamtyc.data.schema :as schema]))

(defn provision []
  (schema/provision "endpoint"))

(comment
  (ns-map *ns*))
