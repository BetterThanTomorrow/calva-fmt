(ns calva.fmt.formatter
  #?@(:clj  [(:require [ysera.test :refer [is= is is-not]]
                       [cljfmt.core :as cljfmt])]
      :cljs [(:require [ysera.test :include-macros true :refer [is= is is-not]]
                       [cljfmt.core :as cljfmt])]))


(defn format-text
  {:test (fn []
           (is= "(foo\n bar\n baz)"
                (:new-text (format-text {:text "(foo   \nbar\n      baz)"}))))}
  [{:keys [text config] :as m}]
  (try
    (assoc m :text (cljfmt/reformat-string text config))
    (catch #?(:cljs js/Error :clj Exception) e
      (assoc m :error (.-message e)))))
