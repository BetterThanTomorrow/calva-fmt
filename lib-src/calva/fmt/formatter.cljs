(ns calva.fmt.formatter
  (:require [cljfmt.core :as cljfmt]
            [calva.fmt.util :as util]))


(defn format-text
  [{:keys [text config] :as m}]
  (try
    (assoc m :text (cljfmt/reformat-string text config))
    (catch js/Error e
      (assoc m :error (.-message e)))))


(defn- normalize-indents
  "Normalizes indents based on where the text starts on the first line"
  [{:keys [text] :as m}]
  (let [indent-before (apply str (repeat (util/indent-before-range m) " "))
        lines (clojure.string/split text #"\r?\n" -1)]
    (update-in m [:text] #(clojure.string/join (str "\n" indent-before) lines))))


(defn index-for-tail-in-text
  "Find index for the `tail` in `text` disregarding whitespace"
  [text tail]
  (let [leading-space-length (count (re-find #"^[ \t]*" tail))
        tail-pattern (-> tail
                         (util/escapeRegExp)
                         (clojure.string/replace #"^[ \t]+" "")
                         (clojure.string/replace #"\s+" "\\s*"))]
    (util/re-pos-first (str " {0," leading-space-length "}" tail-pattern "$") text)))


(defn format-text-at-range
  "Formats text from all-text at the range"
  [{:keys [all-text range idx config] :as m}]
  (let [range-text (subs all-text (first range) (last range))
        tail (subs range-text (- idx (first range)))
        formatted-m (format-text (assoc m :text range-text))
        normalized-m (normalize-indents formatted-m)]
    (assoc normalized-m :new-index (index-for-tail-in-text (:text normalized-m) tail))))


(defn format-text-at-idx
  "Formats the enclosing range of text surrounding idx"
  [{:keys [all-text idx] :as m}]
  (-> m
      (util/enclosing-range)
      (format-text-at-range)))