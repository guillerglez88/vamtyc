(ns vamtyc.handlers.inspect
  (:require [ring.util.response :refer [response]]))

(defn handler [req _tx _app]
  (response req))
