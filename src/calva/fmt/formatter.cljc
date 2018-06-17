(ns calva.fmt.formatter
  (:require
   [cljfmt.core :as cljfmt]))

(defn format-text [{:keys [text config] :as m}]
  (try
    (assoc m :new-text (cljfmt/reformat-string text config))
    (catch #?(:cljs js/Error :clj Exception) e
      (assoc m :error (.-message e)))))
