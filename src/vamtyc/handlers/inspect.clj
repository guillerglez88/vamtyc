(ns vamtyc.handlers.inspect
  (:require [ring.util.response :refer [response]]))

(defn handler [req _tx]
  (response req))
