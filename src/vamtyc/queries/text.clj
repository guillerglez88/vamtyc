(ns vamtyc.queries.text
  (:require [honey.sql.helpers :refer [select from where inner-join]]
            [honey.sql :as hsql]))

;; (defn make-path-access [jsonb path]
;;   (let [prop    (:name path-item)
;;         prop-kw (keyword prop)]
;;     (if (:collection path-item)
;;       (-> (make-path-item-access jsonb {:name prop})
;;           (#(vector :jsonb_array_elements % :as [prop-kw "elem"])))
;;       [:jsonb_extract_path jsonb prop])))
;;

(defn make-prop-alias
  ([jsonb path-elem suffix]
   (let [prop-name (:name path-elem)]
     (-> (name jsonb)
         (str "_" prop-name suffix)
         (keyword))))
  ([jsonb path-elem]
   (make-prop-alias jsonb path-elem "")))

(defn jsonb-extract-prop [sql-map jsonb path-elem alias]
  (let [prop            (:name path-elem)]
    (-> sql-map
        (inner-join [[:jsonb_extract_path jsonb prop] alias] (= 1 1)))))

(defn jsonb-extract-coll [sql-map jsonb path-elem alias]
  (let [prop            (:name path-elem)
        prop-alias      (make-prop-alias jsonb path-elem)]
    (-> sql-map
        (jsonb-extract-prop jsonb path-elem prop-alias)
        (inner-join [[:jsonb_array_elements prop-alias] alias] (= 1 1)))))

(defn filter [req query-param sql-map]
  (let [name  (:name query-param)
        val   (-> req :params (get name))]
    (loop [acc              sql-map
           jsonb            :resource
           [curr & rest]    (:path query-param)]
      (cond
        (nil? curr)         (where acc [:like [:cast jsonb :text] (str "%" val "%")])
        (:collection curr)  (let [alias (make-prop-alias jsonb curr "_elem")]
                              (-> (jsonb-extract-coll acc jsonb curr alias)
                                  (recur alias rest)))
        :else               (let [alias (make-prop-alias jsonb curr)]
                              (-> (jsonb-extract-prop acc jsonb curr alias)
                                  (recur alias rest)))))))


(comment
  (hsql/format-expr [:cast :a :text])
  (-> (inner-join [[:jsonb_extract_path :resource "path"] :resourcepath] (:= 1 1))
      (inner-join [[:jsonb_array_elements :resourcepath] :resourcepathelem] (:= 1 1))
      (hsql/format-expr))

  (-> (select :*)
      (from :route)
      (inner-join (make-path-item-access :resource {:name "path" :collection true}) :on [:= 1 1])
      (hsql/format))
  )
