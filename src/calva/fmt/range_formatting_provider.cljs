(ns calva.fmt.range-formatting-provider
  (:require
   ["vscode" :as vscode]
   [cljfmt.core :as cljfmt]))


(defn- parse-configuration [configuration]
  {:indentation?                    (.get configuration "indentation")
   :remove-surrounding-whitespace?  (.get configuration "removeSurroundingWhitespace")
   :remove-trailing-whitespace?     (.get configuration "removeTrailingWhitespace")
   :insert-missing-whitespace?      (.get configuration "insertMissingWhitespace")})


(deftype RangeEditProvider []
  Object
  (provideDocumentRangeFormattingEdits [_ document range options token]
    (let [configuration (parse-configuration (vscode/workspace.getConfiguration "calva.fmt"))
          configuration (assoc configuration :remove-consecutive-blank-lines? false)
          text          (.getText document range)
          pretty        (try
                          (cljfmt/reformat-string text configuration)
                          (catch js/Error e
                            (js/console.log (.-message e))))]
      (when pretty
        #js [(vscode/TextEdit.replace range pretty)]))))