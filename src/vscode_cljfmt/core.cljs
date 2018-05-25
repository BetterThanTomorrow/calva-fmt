(ns vscode-cljfmt.core
  (:require
   ["vscode" :as vscode]

   [cljfmt.core :as cljfmt]))


(defn- parse-configuration [configuration]
  {:indentation?                    (.get configuration "indentation")
   :remove-surrounding-whitespace?  (.get configuration "removeSurroundingWhitespace")
   :remove-trailing-whitespace?     (.get configuration "removeTrailingWhitespace")
   :insert-missing-whitespace?      (.get configuration "insertMissingWhitespace")
   :remove-consecutive-blank-lines? (.get configuration "removeConsecutiveBlankLines")})


(deftype ClojureDocumentRangeFormattingEditProvider [m]
  Object
  (provideDocumentRangeFormattingEdits [_ document range options token]
    (let [{:keys [configuration catch*]} m
          
          text    (.getText document range)

          pretty  (try
                    (cljfmt/reformat-string text configuration)
                    (catch js/Error e
                      (catch* e)
                      nil))]

      (when pretty
        #js [(vscode/TextEdit.replace range pretty)]))))


(defn activate [^js context]
  (let [scheme        #js {:language "clojure" :scheme "file"}
        output        (vscode/window.createOutputChannel "cljfmt")
        configuration (vscode/workspace.getConfiguration "cljfmt")
        provider      (ClojureDocumentRangeFormattingEditProvider. {:configuration (parse-configuration configuration)
                                                                    :catch*        (fn [e]
                                                                                     (.appendLine output (.-message e)))})]

    (.push context.subscriptions (vscode/Disposable.from output))

    (.push context.subscriptions (vscode/languages.registerDocumentRangeFormattingEditProvider scheme provider))))