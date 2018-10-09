(ns calva.fmt.util
  (:require [calva.js-utils :refer [cljify]]
            ["paredit.js" :as paredit]))


(defn log
  "logs out the object `o` excluding any keywords in `exclude-kws`"
  [o & exlude-kws]
  (println (pr-str (if (map? o) (apply dissoc o exlude-kws) o)))
  o)


(defn escapeRegExp
  "Escapes regexp characters in `s`"
  [s]
  (clojure.string/replace s #"([.*+?^${}()|\[\]\\])" "\\$1"))


(defn re-pos-first
  "Find position of first match of `re` in `s`"
  [re s]
  (if-let [m (.match s re)]
    (.-index m)
    -1))


(defn split
  "Splits text at idx"
  [text idx]
  [(subs text 0 idx) (subs text idx)])


(defn- gen-indent-symbol
  "Adds a random Clojure symbol to m"
  [m]
  (assoc m :indent-symbol (gensym "indent-symbol")))


(defn inject-indent-symbol
  "Inject indent symbol in text. (To give the formatter something to indent.)"
  [{:keys [text local-idx indent-symbol] :as m}]
  (let [[head tail] (split text local-idx)]
    (assoc m :text (str head indent-symbol " " tail))))


(defn indent-before-range
  "Figures out how much extra indentation to add based on the length of the line before the range"
  [{:keys [all-text range]}]
  (let [start (first range)
        end (last range)]
    (if (= start end)
      0
      (-> (subs all-text 0 (first range))
          (clojure.string/split #"\r?\n" -1)
          (last)
          (count)))))


(defn find-indent
  "Looks for indent symbol in text and reports how it is indented"
  [{:keys [text indent-symbol] :as m}]
  (let [local-indent  (-> "([ \t]*)"
                          (str indent-symbol)
                          (re-pattern)
                          (re-find text)
                          (last)
                          (count))]
    (+ local-indent (indent-before-range m))))


(defn current-line
  "Finds the text of the current line in `text` from cursor position `index`"
  [text index]
  (let [head (subs text 0 index)
        tail (subs text index)]
    (str (second (re-find #"\n?(.*)$" head))
         (second (re-find #"^(.*)\n?" tail)))))


(defn localize-index
  "Localize `:idx` in `m` based on `:range`"
  [{:keys [idx range] :as m}]
  (assoc m :local-idx (- idx (first range))))


(defn enclosing-range
  "Expands the range from pos up to any enclosing list/vector/map/string"
  [{:keys [all-text idx] :as m}]
  (assoc m :range
         (let [ast (paredit/parse all-text)
               range ((.. paredit -navigator -sexpRange) ast idx)]
           (if (some? range)
             (loop [range range]
               (let [text (apply subs all-text range)]
                 (if (and (some? range)
                          (or (= idx (first range))
                              (= idx (last range))
                              (not (contains? (set "{[(") (first text)))))
                   (let [expanded-range ((.. paredit -navigator -sexpRangeExpansion) ast (first range) (last range))]
                     (if (and (some? expanded-range) (not= expanded-range range))
                       (recur expanded-range)
                       (cljify range)))
                   (cljify range))))
             [idx idx]))))
