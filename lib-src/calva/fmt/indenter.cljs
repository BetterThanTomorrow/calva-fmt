(ns calva.fmt.indenter
  (:require [calva.fmt.util :as util]
            [calva.fmt.formatter :refer [format-text]]
            [calva.js-utils :refer [cljify]]
            ["paredit.js" :as paredit]))


(defn- gen-indent-symbol
  "Adds a random Clojure symbol to m"
  [m]
  (assoc m :indent-symbol (gensym "indent-symbol")))


(defn- split
  "Splits text at idx"
  [text idx]
  [(subs text 0 idx) (subs text idx)])


(defn- inject-indent-symbol
  "Inject indent symbol in text. (To give the formatter something to indent.)"
  [{:keys [text local-idx indent-symbol] :as m}]
  (let [[head tail] (split text local-idx)]
    (assoc m :text (str head indent-symbol " " tail))))


(defn find-indent
  "Looks for indent symbol in text and reports how it is indented"
  [{:keys [text indent-symbol] :as m}]
  (let [local-indent  (-> "([ \t]*)"
                          (str indent-symbol)
                          (re-pattern)
                          (re-find text)
                          (last)
                          (count))]
    (+ local-indent (util/indent-before-range m))))


(defn indent-for-index
  "Figures out how where a cursor placed at `idx` should be placed"
  [{:keys [all-text idx config] :as m}]
  {:pre [(string? all-text)
         (number? idx)
         (or (map? config) (nil? config))]}
  (try
    (assoc m :indent  (-> m
                          (assoc-in [:config :remove-surrounding-whitespace?] false)
                          (gen-indent-symbol)
                          (util/enclosing-range)
                          (#(assoc % :text (apply subs all-text (:range %))))
                          (util/localize-index)
                          (inject-indent-symbol)
                          (format-text)
                          (find-indent)))
    (catch js/Error e
      {:error e})))