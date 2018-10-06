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
