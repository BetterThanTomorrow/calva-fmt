(ns calva.js-utils)

(defn jsify [o]
  (clj->js o))

(defn cljify [o]
  (js->clj o :keywordize-keys true))