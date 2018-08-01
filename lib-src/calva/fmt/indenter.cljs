(ns calva.fmt.indenter
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.util :refer [minimal-range indent-before-range log]]
            [calva.fmt.formatter :refer [format-text]]
            [calva.js-utils :refer [cljify]]
            ["paredit.js" :as paredit]))


(defn- gen-indent-symbol
  "Adds a random Clojure symbol to m"
  {:test (fn []
           (is (some? (:indent-symbol (gen-indent-symbol {})))))}
  [m]
  (assoc m :indent-symbol (gensym "indent-symbol")))


(defn- split
  "Splits text at idx"
  {:test (fn []
           (is (= [" " " "]
                  (split "  " 1)))
           (is (= ["(foo\n " "\n bar)"]
                  (split "(foo\n  \n bar)" 6))))}
  [text idx]
  [(subs text 0 idx) (subs text idx)])


(defn- inject-indent-symbol
  "Inject indent symbol in text. (To give the formatter has something to indent.)"
  {:test (fn []
           (is (= "(foo\n  FOO \n bar)"
                  (:text (inject-indent-symbol {:text "(foo\n  \n bar)"
                                                :indent-symbol "FOO"
                                                :local-idx 7})))))}
  [{:keys [text local-idx indent-symbol] :as m}]
  (let [[head tail] (split text local-idx)]
    (assoc m :text (str head indent-symbol " " tail))))


(defn find-indent
  "Looks for indent symbol in text and reports how it is indented"
  {:test (fn []
           (is (= 0
                  (find-indent {:all-text "(defn foo [x] 
  (let [bar 1] 
    bar))
FOO"
                                :text "FOO"
                                :indent-symbol "FOO"
                                :range [45 45]})))
           (is (= 4
                  (find-indent {:all-text "(defn foo [x]\n  (let [bar 1]\n  FOO bar))"
                                :text "(let [bar 1] \n  FOO bar)"
                                :range [16 40]
                                :indent-symbol "FOO"}))))}
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
           (is (= 5
                  (:indent (indent-for-index {:all-text "(def a 1)
(defn a [b]
  (let [c 1]
    (foo

    bar)))"
                                              :idx 45}))))
           (is (= 0
                  (:indent (indent-for-index {:all-text "(defn a
  []
  do-some-stuff)

(def a :b)"
                                              :idx 30}))))
           (is (= 9
                  (:indent (indent-for-index {:all-text "(foo-bar a

)"
                                              :idx 11})))))}
  [{:keys [all-text idx config] :as m}]
  {:pre [(string? all-text)
         (number? idx)
         (or (map? config) (nil? config))]}
  (try
    (assoc m :indent  (-> m
                          (assoc-in [:config :remove-surrounding-whitespace?] false)
                          (gen-indent-symbol)
                          (minimal-range)
                          (#(assoc % :text (apply subs all-text (:range %))))
                          (#(assoc % :local-idx (- idx (first (:range %)))))
                          (inject-indent-symbol)
                          (format-text)
                          (find-indent)))
    (catch js/Error e
      {:error e})))