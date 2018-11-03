(ns calva.fmt.formatter
  (:require [cljfmt.core :as cljfmt]
            [zprint.core :refer [zprint-str]]
            [calva.fmt.util :as util]))


(defn format-text
  [{:keys [text config] :as m}]
  (try
    #_(assoc m :text (zprint-str text {:parse-string-all? true
                                       :style :community
                                       :fn-force-nl #{:arg1-body}}))
    (assoc m :text (cljfmt/reformat-string text config))
    (catch js/Error e
      (assoc m :error (.-message e)))))


(defn- normalize-indents
  "Normalizes indents based on where the text starts on the first line"
  [{:keys [text] :as m}]
  (let [indent-before (apply str (repeat (util/indent-before-range m) " "))
        lines (clojure.string/split text #"\r?\n" -1)]
    (assoc m :text (clojure.string/join (str "\n" indent-before) lines))))


(defn index-for-tail-in-range
  "Find index for the `tail` in `text` disregarding whitespace"
  [{:keys [text range-tail on-type] :as m}]
  (let [leading-space-length (count (re-find #"^[ \t]*" range-tail))
        tail-pattern (-> range-tail
                         (util/escape-regexp)
                         (clojure.string/replace #"^[ \t]+" "")
                         (clojure.string/replace #"\s+" "\\s*"))
        tail-pattern (if (and on-type (re-find #"^\n" range-tail))
                       (str "\n+" tail-pattern)
                       tail-pattern)
        pos (util/re-pos-first (str " {0," leading-space-length "}" tail-pattern "$") text)]
    (assoc m :new-index pos)))


(defn format-text-at-range
  "Formats text from all-text at the range"
  [{:keys [all-text range idx config on-type] :as m}]
  (let [range-text (subs all-text (first range) (last range))
        range-index (- idx (first range))
        tail (subs range-text range-index)
        formatted-m (format-text (assoc m :text range-text))
        normalized-m (normalize-indents formatted-m)]
    (-> normalized-m
        (assoc :range-tail tail)
        (index-for-tail-in-range))))


(defn add-indent-token-if-empty-current-line
  "If `:current-line` is empty add an indent token at `:idx`"
  [{:keys [head tail] :as m}]
  (let [indent-token "0"]
    (if (util/current-line-empty? m)
      (assoc m :all-text (str head indent-token tail))
      m)))


(defn remove-indent-token-if-empty-current-line
  "If an indent token was added, lets remove it. Not forgetting to shrink `:range`"
  [{:keys [text range new-index] :as m}]
  (if (util/current-line-empty? m)
    (assoc m :text (str (subs text 0 new-index) (subs text (inc new-index)))
           :range [(first range) (dec (second range))])
    m))


(defn format-text-at-idx
  "Formats the enclosing range of text surrounding idx"
  [{:keys [all-text idx] :as m}]
  (-> m
      (util/add-head-and-tail)
      (util/add-current-line)
      (add-indent-token-if-empty-current-line)
      (util/enclosing-range)
      (format-text-at-range)
      (remove-indent-token-if-empty-current-line)))


(defn format-text-at-idx-on-type
  "Relax formating some when used as an on-type handler"
  [m]
  (-> m
      (assoc :on-type true)
      (assoc-in [:config :remove-surrounding-whitespace?] false)
      (assoc-in [:config :remove-trailing-whitespace?] false)
      (assoc-in [:config :remove-consecutive-blank-lines?] false)
      (format-text-at-idx)))