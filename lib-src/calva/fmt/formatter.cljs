(ns calva.fmt.formatter
  (:require [cljs.test :include-macros true :refer [is]]
            [cljfmt.core :as cljfmt]
            [calva.fmt.util :refer [indent-before-range enclosing-range log]]))


(defn format-text
  [{:keys [text config] :as m}]
  (try
    (assoc m :text (cljfmt/reformat-string text config))
    (catch js/Error e
      (assoc m :error (.-message e)))))


(defn- normalize-indents
  "Normalizes indents based on where the text starts on the first line"
  [{:keys [text] :as m}]
  (let [indent-before (apply str (repeat (indent-before-range m) " "))
        lines (clojure.string/split text #"\r?\n" -1)]
    (update-in m [:text] #(clojure.string/join (str "\n" indent-before) lines))))


(defn format-text-at-range
  "Formats text from all-text at the range"
  [{:keys [all-text range config] :as m}]
  (-> m
      (assoc :text (subs all-text (first range) (last range)))
      (format-text)
      (normalize-indents)))

(defn tail-text-wo-whitespace [text idx]
  "Extracts tail of `text` from `idx` and squashes away any whitespace"
  (-> text
      (subs idx)
      (clojure.string/replace #"\s*" "")))


(defn format-text-at-idx
  "Formats the enclosing range of text surrounding idx"
  [{:keys [all-text idx] :as m}]
  (let [tail-text (subs all-text idx)])
  (-> m
      (enclosing-range)
      (format-text-at-range)))