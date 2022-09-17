(ns vamtyc.core
  (:require [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn handler [_request]
  {:status 200
   :headers {"Content-Type" "text/plain; charset=UTF-8"}
   :body "Hello World!\n"})

(defn -main [& _args]
  (run-jetty handler {:port 3000}))
