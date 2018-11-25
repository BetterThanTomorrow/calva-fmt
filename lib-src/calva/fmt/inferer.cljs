(ns calva.fmt.inferer
  (:require ["parinfer" :as parinfer]
            [calva.js-utils :refer [cljify jsify]]
            [calva.fmt.editor :as editor]))

(defn infer-parens
  "Calculate the edits needed for infering parens in `text`,
   and where the cursor should be placed to 'stay' in the right place."
  [^js m]
  (let [{:keys [text line character previous-line previous-character]} (cljify m)
        options {:cursorLine line
                 :cursorX character
                 :prevCursorLine previous-line
                 :prevCursorX previous-character}
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
  (let [o (jsify {:cursorLine 1 :cursorX 13})
        result (parinfer/indentMode "    (foo []\n      (bar)\n      (baz)))" o)]
    (cljify result))

  (infer-parens {:text "    (foo []\n      (bar)\n      (baz)))"
                 :line 2
                 :character 13})
  (infer-parens {:text "(f)))"
                 :line 0
                 :character 2}))