(ns calva.fmt.util
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.js-utils :refer [cljify]]
            ["paredit.js" :as paredit]))


(defn log
  {:test (fn []
           (is (= (log {:text ""} :text)
                  {:text ""})))}
  [o & exlude-kws]
  (println (pr-str (if (map? o) (apply dissoc o exlude-kws) o)))
  o)


(defn indent-before-range
  "Figures out how much extra indentation to add based on the length of the line before the range"
  {:test (fn []
           (is (= 11
                  (indent-before-range {:all-text "(def a 1)

(defn foo [x] (let [bar 1] bar))"
                                        :range [22 25]}))))}
  [{:keys [all-text range]}]
  (let [start (first range)
        end (last range)]
    (if (= start end)
      0
      (-> (subs all-text 0 (first range))
          (clojure.string/split #"\r?\n" -1)
          (last)
          (count)))))


(defn minimal-range
  "Expands the range from pos up to any enclosing list/vector/map/string"
  {:test (fn []
           (is (= [12 44] ;"[x]"
                  (:range (minimal-range {:all-text "(def a 1)


(defn foo [x] (let [bar 1] bar))" :idx 22}))))
           (is (= [12 44] ;"[x]"
                  (:range (minimal-range {:all-text "(def a 1)


(defn foo [x] (let [bar 1] bar))" :idx 21}))))
           (is (= [10 10] ;""
                  (:range (minimal-range {:all-text "(def a 1)\n\n\n(defn foo [x] (let [bar 1] bar))" :idx 10})))))}
  [{:keys [all-text idx] :as m}]
  (assoc m :range
         (let [ast (paredit/parse all-text)
               range (.sexpRange  (.-navigator paredit) ast idx)]
           (if (some? range)
             (loop [range range]
               (let [text (apply subs all-text range)]
                 (if (and (some? range)
                          (or (= idx (first range))
                              (= idx (last range))
                              (not (contains? (set "{[(") (first text)))))
                   (recur (.sexpRangeExpansion (.-navigator paredit) ast (first range) (last range)))
                   (cljify range))))
             [idx idx]))))
