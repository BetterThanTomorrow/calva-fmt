(ns calva.fmt.core
  (:require
   ["vscode" :as vscode]

   [cljfmt.core :as cljfmt]))

(defn- parse-configuration [configuration]
  {:indentation?                    (.get configuration "indentation")
   :remove-surrounding-whitespace?  (.get configuration "removeSurroundingWhitespace")
   :remove-trailing-whitespace?     (.get configuration "removeTrailingWhitespace")
   :insert-missing-whitespace?      (.get configuration "insertMissingWhitespace")
   :remove-consecutive-blank-lines? false})

(deftype ClojureDocumentRangeFormattingEditProvider []
  Object
  (provideDocumentRangeFormattingEdits [_ document range options token]
    (let [configuration (parse-configuration (vscode/workspace.getConfiguration "calva.fmt"))

          text          (.getText document range)

          pretty        (try
                          (cljfmt/reformat-string text configuration)
                          (catch js/Error e
                            (js/console.log (.-message e))))]

      (when pretty
        #js [(vscode/TextEdit.replace range pretty)]))))

(defn activate [^js context]
  (let [scheme        #js {:language "clojure" :scheme "file"}
        provider      (ClojureDocumentRangeFormattingEditProvider.)]
    (.push context.subscriptions (vscode/languages.registerDocumentRangeFormattingEditProvider scheme provider))))