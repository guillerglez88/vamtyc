(ns vamtyc.config.env
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn parse-line [line]
  (let [[first second]  (str/split line #"=" 2)
        key             (keyword first)
        val             (str/replace second "\"" "")]
    [key val]))

(defn should-ignore? [line]
  (or (empty? line)
      (str/starts-with? line "#")))

(defn load-dot-env
  ([path]
   (if (.exists (io/as-file path))
     (->> (slurp path)
          (str/split-lines)
          (map str/trim)
          (filter #(not (should-ignore? %)))
          (map parse-line)
          (into {}))))
  ([] (load-dot-env ".env")))

(def def-env {:DB_CNX_STR "jdbc:postgresql://localhost:5432/vamtyc"
              :PORT       "3000"
              :LIMIT      "128"})

(def env
  (let [keys (keys def-env)
        dot-env (load-dot-env)
        sys-env (System/getenv)]
    (select-keys (merge def-env dot-env sys-env) keys)))

(def sec-env
  (select-keys env [:PORT :LIMIT]))
