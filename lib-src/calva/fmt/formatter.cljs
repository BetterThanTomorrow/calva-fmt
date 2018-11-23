(ns calva.fmt.formatter
  (:require [cljfmt.core :as cljfmt]
            #_[zprint.core :refer [zprint-str]]
            ["paredit.js" :as paredit]
            ["parinfer" :as parinfer]
            [calva.js-utils :refer [cljify jsify]]
            [calva.fmt.util :as util]
            [calva.fmt.editor :as editor]))


(defn format-text
  [{:keys [range-text config] :as m}]
  (try
    #_(assoc m :range-text (zprint-str range-text {:parse-string-all? true
                                                   :style :community
                                                   :fn-force-nl #{:arg1-body}}))
    (assoc m :range-text (cljfmt/reformat-string range-text config))
    (catch js/Error e
      (assoc m :error (.-message e)))))


(defn current-line-empty?
  "Figure out if `:current-line` is empty"
  [{:keys [current-line]}]
  (some? (re-find #"^\s*$" current-line)))


(defn indent-before-range
  "Figures out how much extra indentation to add based on the length of the line before the range"
  [{:keys [all-text range]}]
  (let [start (first range)
        end (last range)]
    (if (= start end)
      0
      (-> (subs all-text 0 (first range))
          (util/split-into-lines)
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


(defn add-head-and-tail
  "Splits `:all-text` at `:idx` in `:head` and `:tail`"
  [{:keys [all-text idx] :as m}]
  (-> m
      (assoc :head (subs all-text 0 idx)
             :tail (subs all-text idx))))


(defn add-current-line
  "Finds the text of the current line in `text` from cursor position `index`"
  [{:keys [head tail] :as m}]
  (-> m
      (assoc :current-line
             (str (second (re-find #"\n?(.*)$" head))
                  (second (re-find #"^(.*)\n?" tail))))))


(defn- normalize-indents
  "Normalizes indents based on where the text starts on the first line"
  [{:keys [range-text] :as m}]
  (let [indent-before (apply str (repeat (indent-before-range m) " "))
        lines (clojure.string/split range-text #"\r?\n(?!\s*;)" -1)]
    (assoc m :range-text (clojure.string/join (str "\n" indent-before) lines))))


(defn index-for-tail-in-range
  "Find index for the `tail` in `text` disregarding whitespace"
  [{:keys [range-text range-tail on-type idx] :as m}]
  #_(if-not (= range-text "0")
      (assoc m :new-index idx))
  (let [leading-space-length (count (re-find #"^[ \t]*" range-tail))
        tail-pattern (-> range-tail
                         (util/escape-regexp)
                         (clojure.string/replace #"^[ \t]+" "")
                         (clojure.string/replace #"\s+" "\\s*"))
        tail-pattern (if (and on-type (re-find #"^\n" range-tail))
                       (str "\n+" tail-pattern)
                       tail-pattern)
        pos (util/re-pos-first (str " {0," leading-space-length "}" tail-pattern "$") range-text)]
    (assoc m :new-index pos)))


(defn format-text-at-range-new
  "Formats text from all-text at the range"
  [{:keys [all-text range idx config on-type] :as m}]
  (let [indent-before (indent-before-range m)
        padding (apply str (repeat indent-before " "))
        range-text (subs all-text (first range) (last range))
        padded-text (str padding range-text)
        range-index (- idx (first range))
        tail (subs range-text range-index)
        formatted-m (format-text (assoc m :range-text padded-text))]
    (-> (assoc m :range-text (subs (:range-text formatted-m) indent-before))
        (assoc :range-tail tail)
        (index-for-tail-in-range))))


(defn format-text-at-range
  "Formats text from all-text at the range"
  [{:keys [all-text range idx config on-type] :as m}]
  (let [range-text (subs all-text (first range) (last range))
        range-index (- idx (first range))
        tail (subs range-text range-index)
        formatted-m (format-text (assoc m :range-text range-text))
        normalized-m (normalize-indents formatted-m)]
    (-> normalized-m
        (assoc :range-tail tail)
        (index-for-tail-in-range))))


(defn add-indent-token-if-empty-current-line
  "If `:current-line` is empty add an indent token at `:idx`"
  [{:keys [head tail] :as m}]
  (let [indent-token "0"]
    (if (current-line-empty? m)
      (assoc m :all-text (str head indent-token tail))
      m)))


(defn remove-indent-token-if-empty-current-line
  "If an indent token was added, lets remove it. Not forgetting to shrink `:range`"
  [{:keys [range-text range new-index] :as m}]
  (if (current-line-empty? m)
    (assoc m :range-text (str (subs range-text 0 new-index) (subs range-text (inc new-index)))
           :range [(first range) (dec (second range))])
    m))


(defn format-text-at-idx
  "Formats the enclosing range of text surrounding idx"
  [{:keys [all-text idx] :as m}]
  (-> m
      (add-head-and-tail)
      (add-current-line)
      (add-indent-token-if-empty-current-line)
      (enclosing-range)
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


(defn infer-indents
  "Calculate the edits needed for infering indents in `text`,
   and where the cursor should be placed to 'stay' in the right place."
  [^js m]
  (let [{:keys [text line character previous-line previous-character changes]} (cljify m)
        options {:cursorLine line :cursorX character
                 :prevCursorLine previous-line
                 :prevCursorX previous-character
                 :changes (mapv (fn [change]
                                  {:lineNo (:line change)
                                   :x      (:character change)
                                   :oldText (:old-text change)
                                   :newText (:new-text change)})
                                changes)}
        ;;_ (println (pr-str options))
        result (cljify (parinfer/smartMode text (jsify options)))]
    (jsify
     (if (:success result)
       {:success true
        :line (:cursorLine result)
        :character (:cursorX result)
        :edits (editor/raplacement-edits-for-diffing-lines text (:text result))}
       {:success false
        :error-msg (get-in result [:error :message])}))))



(comment
  (let [o (jsify {:cursorLine 1 :cursorX 4
                  :changes {:lineNo 1 :x 0 :oldText "" :newText " "}})
        result (parinfer/parenMode "  (defn a []\n     (foo []\n     (bar)\n     (baz)))" o)]
    (cljify result))
  (let [o (jsify {:cursorLine 1
                  :cursorX 3
                  :prevCursorLine 1
                  :prevCursorX 2
                  :changes [{:lineNo 1
                             :x 2
                             :oldText ""
                             :newText " "}]})
        result (parinfer/parenMode "(comment\n   (foo bar\n     baz))" o)]
    (cljify result))
  (let [o (jsify {:cursorLine 1
                  :cursorX 3
                  :prevCursorLine 1
                  :prevCursorX 2})
        result (parinfer/parenMode "(comment\n    (foo bar\n    baz))" o)]
    (cljify result))
  (infer-indents {:text "(comment \n   (foo bar \n     baz))"
                  :line 1
                  :character 3
                  :previous-line 1
                  :previous-character 2
                  :changes [{:line 1
                             :character 2
                             :old-text ""
                             :new-text " "}]})
  (infer-indents {:text "  (defn a []\n     (foo []\n     (bar)\n     (baz)))"
                  :line 1
                  :character 4
                  :changes [{:line 4
                             :character 0
                             :old-text ""
                             :new-text " "}]}))
