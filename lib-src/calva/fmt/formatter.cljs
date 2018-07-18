(ns calva.fmt.formatter
  (:require [cljs.test :include-macros true :refer [is]]
            [cljfmt.core :as cljfmt]
            [calva.fmt.util :refer [indent-before-range minimal-range log]]))


(defn format-text
  {:test (fn []
           (is (= "  (foo\n   bar\n   baz)\n  gazonk"
                  (:text (format-text {:text "  (foo   \nbar\n      baz)\ngazonk"})))))}
  [{:keys [text config] :as m}]
  (try
    (assoc m :text (cljfmt/reformat-string text config))
    (catch js/Error e
      (assoc m :error (.-message e)))))


(defn- normalize-indents
  "Normalizes indents based on where the text starts on the first line"
  {:test (fn []
           (is (= "(foo)\n  (defn bar\n    [x]\n    baz)"
                  (:text (normalize-indents {:all-text "  (foo)\n(defn bar\n[x]\nbaz)"
                                             :range [2 26]
                                             :text "(foo)\n(defn bar\n  [x]\n  baz)"})))))}
  [{:keys [text] :as m}]
  (let [indent-before (apply str (repeat (indent-before-range m) " "))
        lines (clojure.string/split text #"\r?\n" -1)]
    (update-in m [:text] #(clojure.string/join (str "\n" indent-before) lines))))


(defn format-text-at-range
  "Formats text from all-text at the range"
  {:test (fn []
           (is (= "(foo)\n  (defn bar\n    [x]\n    baz)"
                  (:text (format-text-at-range {:all-text "  (foo)\n(defn bar\n[x]\nbaz)" :range [2 26]})))))}
  [{:keys [all-text range config] :as m}]
  (-> m
      (assoc :text (subs all-text (first range) (last range)))
      (format-text)
      (normalize-indents)))

(defn format-text-at-idx
  "Formats a minimal range of text surrounding idx"
  {:test (fn []
           (let [formatted (format-text-at-idx {:all-text "  (foo)\n  (defn bar\n[x]\nbaz)" :idx 11})
                 formatted {:text (:text formatted) :range (:range formatted)}]
             (is (= formatted
                    {:text "(defn bar\n    [x]\n    baz)"
                     :range [10 28]}))))}
  [{:keys [all-text idx] :as m}]
  (-> m
      (minimal-range)
      (format-text-at-range)))