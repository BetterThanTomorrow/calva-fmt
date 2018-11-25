(ns calva.fmt.util
  (:require [clojure.string]))


(defn log
  "logs out the object `o` excluding any keywords in `exclude-kws`"
  [o & exlude-kws]
  (println (pr-str (if (map? o) (apply dissoc o exlude-kws) o)))
  o)


(defn escape-regexp
  "Escapes regexp characters in `s`"
  [s]
  (clojure.string/replace s #"([.*+?^${}()|\[\]\\])" "\\$1"))


(defn current-line
  "Finds the text of the current line in `text` from cursor position `index`"
  [text index]
  (let [head (subs text 0 index)
        tail (subs text index)]
    (str (second (re-find #"\n?(.*)$" head))
         (second (re-find #"^(.*)\n?" tail)))))


(defn re-pos-first
  "Find position of first match of `re` in `s`"
  [re s]
  (if-let [m (.match s re)]
    (.-index m)
    -1))

(defn split-into-lines
  [s]
  (clojure.string/split s #"\r?\n" -1))

;; Thanks to @lilactown at Clojurians Slack for this way to check
;; if we have an enclosing range. He refers to this:
;; https://codereview.stackexchange.com/a/180569 

(def opening (set "{[("))

(def closing (set "}])"))

(def mapping (apply assoc {} (interleave opening closing)))

(defn -enclosing? [{:keys [queue first?]} c]
  (if (and (not first?) (empty? queue))
    (reduced {:queue (conj queue c)})
    {:first? false
     :queue (cond
              (opening c) (conj queue (mapping c))
              (closing c) (if (= c (peek queue))
                            (pop queue)
                            (reduced (conj queue c)))
              :else queue)}))

(defn enclosing? [text]
  (-> (reduce -enclosing? {:queue [] :first? true} text)
      (:queue)
      (empty?)))

(comment
  (enclosing? "[][]")
  (enclosing? "([][])"))