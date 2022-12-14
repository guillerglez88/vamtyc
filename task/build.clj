(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'guillerglez88/vamtyc)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def uber-file (format "target/%s-uber.jar" (name lib)))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file})
  (println (format "Jar created: \"%s\"" jar-file)))

(defn uber [_]
  (clean nil)

  (b/copy-dir {:src-dirs   ["resources"]
               :target-dir class-dir})

  (b/compile-clj {:basis     basis
                  :src-dirs  ["src"]
                  :class-dir class-dir})

  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis     basis
           :main      'vamtyc.core})

  (println (format "Uber-Jar created: \"%s\"" uber-file)))
