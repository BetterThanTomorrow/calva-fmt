(ns calva.js-utils)

(defn jsify [o]
  #?(:cljs (clj->js o)
     :clj  0))

(defn cljify [o]
  #?(:cljs (js->clj o :keywordize-keys true)
     :clj  o))