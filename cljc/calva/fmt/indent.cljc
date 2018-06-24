(ns calva.fmt.indent
  #?@(:clj  [(:require [ysera.test :refer [is= is is-not]]
                       [calva.fmt.formatter :refer [format-text]])]
      :cljs [(:require [ysera.test :include-macros true :refer [is= is is-not]]
                       [goog.string :as gstring]
                       [goog.string.format :as gformat]
                       [calva.fmt.formatter :refer [format-text]])]))


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


(defn- gen-indent-symbol
  "Generates a random Clojure symbol"
  {:test (fn []
           (is (some? (:indent-symbol (gen-indent-symbol {})))))}
  [m]
  (assoc m :indent-symbol (str "indent-symbol-" (sprintf "%012d" (rand-int 10000000)))))


(defn- localize-pos
  "Localizes the position within the range"
  {:test (fn []
           (is= {:line 2
                 :character 20}
                (:pos (localize-pos {:range {:start {:line 2 :character 0}
                                             :end   {:line 3 :character 50}}
                                     :pos {:line 4 :character 20}})))
           (is= {:line 0
                 :character 0}
                (:pos (localize-pos {:range {:start {:line 2 :character 20}
                                             :end   {:line 3 :character 50}}
                                     :pos {:line 2 :character 20}}))))}
  [{:keys [range pos] :as m}]
  (let [local-line (- (:line pos) (get-in range [:start :line]))
        local-character (if (= local-line 0)
                          (- (:character pos) (get-in range [:start :character]))
                          (:character pos))]
    (assoc m :pos {:line local-line
                   :character local-character})))


(defn- split-at-pos
  "Splits the text at the position"
  {:test (fn []
           (is= {:pre "(foo\n " :post "\n bar)"}
                (split-at-pos {:text "(foo\n \n bar)"
                               :pos {:line 1 :character 1}}))
           (is= {:pre " " :post " "}
                (split-at-pos {:text "  "
                               :pos {:line 0 :character 1}})))}
  [{:keys [text pos]}]
  (let [lines (clojure.string/split-lines text)
        line (nth lines (:line pos))
        line-pre (subs line 0 (:character pos))
        line-post (subs line (:character pos))]
    {:pre (clojure.string/join "\n" (conj (vec (take (:line pos) lines)) line-pre))
     :post (clojure.string/join "\n" (cons line-post (drop (inc (:line pos)) lines)))}))


(defn- inject-indent-symbol
  "Add indent symbol in text"
  {:test (fn []
           (is= "(foo\n  FOO\n bar)"
                (:text (inject-indent-symbol {:text "(foo\n  \n bar)"
                                              :indent-symbol "FOO"
                                              :pos {:line 1 :character 2}})))
           (is= " FOO "
                (:text (inject-indent-symbol {:text "  "
                                              :indent-symbol "FOO"
                                              :pos {:line 0 :character 1}}))))}
  [{:keys [indent-symbol] :as m}]
  (let [{:keys [pre post]} (split-at-pos m)]
    (assoc m :text (str pre indent-symbol post))))


(defn- find-indent
  ""
  {:test (fn []
           (is= 3
                (find-indent {:text "  (foo\n   FOO\nbar)"
                              :range {:start {:line 2 :character 0}
                                      :end   {:line 24 :character 50}}
                              :pos {:line 3 :character 0}
                              :indent-symbol "FOO"})))}
  [{:keys [text indent-symbol]}]
  (-> "([ \t]*)"
      (str indent-symbol)
      (re-pattern)
      (re-find text)
      (last)
      (count)))


(defn indent-for-position
  {:test (fn []
           (is= {:indent 5}
                (indent-for-position {:text "(foo\n\nbar)"
                                      :range {:start {:line 2 :character 4}
                                              :end   {:line 24 :character 50}}
                                      :pos {:line 3 :character 0}})))}
  [{:keys [text range pos config] :as m}]
  {:pre [(string? text)
         (map? range)
         (map? pos)
         (or (map? config) (nil? config))]}
  (try
    (assoc m :indent (if (= "" (:text m))
                       (:character (:pos m))
                       (-> m
                           (assoc-in [:config :remove-surrounding-whitespace?] false)
                           (gen-indent-symbol)
                           (localize-pos)
                           (inject-indent-symbol)
                           (format-text)
                           (find-indent)
                           (+ (:character (:start range))))))
    (catch #?(:cljs js/Error :clj Exception) e
      {:error e})))