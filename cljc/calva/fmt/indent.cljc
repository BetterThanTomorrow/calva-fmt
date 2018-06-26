(ns calva.fmt.indent
  #?@(:clj  [(:require [ysera.test :refer [is= is is-not]]
                       [calva.fmt.formatter :refer [format-text]])]
      :cljs [(:require [ysera.test :include-macros true :refer [is= is is-not]]
                       [goog.string :as gstring]
                       [goog.string.format :as gformat]
                       [calva.fmt.formatter :refer [format-text]]
                       [calva.js-utils :refer [cljify]]
                       ["paredit.js" :as paredit])]))


(defn- log
  {:test (fn []
           (is= (log {:text ""} :text)
                {:text ""}))}
  [o & exlude-kws]
  (println (pr-str (if (map? o) (apply dissoc o exlude-kws) o)))
  o)


(defn- sprintf [fmt s]
  #?(:clj  (clojure.core/format fmt s)
     :cljs (gstring/format fmt s)))


(defn- minimal-range
  "Expands the range from pos up to any enclosing list/vector/map/string"
  {:test (fn []
           (is= [22 25] ;"[x]"
                (:range (minimal-range {:all-text "(def a 1)\n\n\n(defn foo [x] (let [bar 1] bar))" :idx 22})))
           (is= [10 10] ;""
                (:range (minimal-range {:all-text "(def a 1)\n\n\n(defn foo [x] (let [bar 1] bar))" :idx 10}))))}
  [{:keys [all-text idx] :as m}]
  (assoc m :range
         (let [ast (paredit/parse all-text)
               range (.sexpRange  (.-navigator paredit) ast idx)]
           (if (some? range)
             (loop [range range]
               (let [text (apply subs all-text range)]
                 (if (and (some? range) (not (contains? (set "{[(") (first text))))
                   (recur (.sexpRangeExpansion (.-navigator paredit) ast (first range) (last range)))
                   range)))
             [idx idx]))))


(defn- gen-indent-symbol
  "Generates a random Clojure symbol"
  {:test (fn []
           (is (some? (:indent-symbol (gen-indent-symbol {})))))}
  [m]
  (assoc m :indent-symbol (str "indent-symbol-" (sprintf "%012d" (rand-int 10000000)))))


(defn- split
  "Splits text at idx"
  {:test (fn []
           (is= ["(foo\n " "\n bar)"]
                (split "(foo\n  \n bar)" 6))
           (is= [" " " "]
                (split "  " 1)))}
  [text idx]
  [(subs text 0 idx) (subs text idx)])


(defn- inject-indent-symbol
  "Inject indent symbol in text. (To give the formatter has something to indent.)"
  {:test (fn []
           (is= "(foo\n  FOO \n bar)"
                (:text (inject-indent-symbol {:text "(foo\n  \n bar)"
                                              :indent-symbol "FOO"
                                              :local-idx 7}))))}
  [{:keys [text local-idx indent-symbol] :as m}]
  (let [[head tail] (split text local-idx)]
    (assoc m :text (str head indent-symbol " " tail))))


(defn- indent-before-range
  "Figures out how much extra indentation to add based on the length of the line before the range"
  {:test (fn []
           (is= 10
                (indent-before-range {:all-text "(def a 1)\n\n\n(defn foo [x] (let [bar 1] bar))"
                                      :range [11 11]})))}
  [{:keys [all-text range]}]
  (-> (subs all-text 0 (first range))
      (clojure.string/split #"\r?\n" -1)
      (last)
      (count)))


(defn- find-indent
  ""
  {:test (fn []
           (is= 4
                (find-indent {:all-text "(defn foo [x]\n  (let [bar 1]\n  FOO bar))"
                              :text "(let [bar 1] \n  FOO bar)"
                              :range [16 40]
                              :indent-symbol "FOO"})))}
  [{:keys [text indent-symbol] :as m}]
  (let [local-indent  (-> "([ \t]*)"
                          (str indent-symbol)
                          (re-pattern)
                          (re-find text)
                          (last)
                          (count))]
    (+ local-indent (indent-before-range m))))


(defn indent-for-index
  {:test (fn []
           (is= 5
                (:indent (indent-for-index {:all-text "(def a 1)
(defn a [b]
  (let [c 1]
    (foo

    bar)))"
                                            :idx 45})))
           (is= 9
                (:indent (indent-for-index {:all-text "(foo-bar a

)"
                                            :idx 11}))))}
  [{:keys [all-text idx config] :as m}]
  {:pre [(string? all-text)
         (number? idx)
         (or (map? config) (nil? config))]}
  (try
    (assoc m :indent  (-> m
                          (assoc-in [:config :remove-surrounding-whitespace?] false)
                          (gen-indent-symbol)
                          (minimal-range)
                          (#(assoc % :text (apply subs (:all-text %) (:range %))))
                          (#(assoc % :local-idx (- idx (first (:range %)))))
                          (inject-indent-symbol)
                          (format-text)
                          (find-indent)))
    (catch #?(:cljs js/Error :clj Exception) e
      {:error e})))