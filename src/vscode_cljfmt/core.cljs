(ns vscode-cljfmt.core
  (:require
   ["vscode" :as vscode]

   [cljfmt.core :as cljfmt]))


(deftype ClojureDocumentRangeFormattingEditProvider [output]
  Object
  (provideDocumentRangeFormattingEdits [_ document range options token]
    (let [text    (.getText document range)

          pretty  (try
                    (cljfmt/reformat-string text {:remove-consecutive-blank-lines? false})
                    (catch js/Error e
                      (.appendLine output (.-message e))
                      nil))]

      (when pretty
        #js [(vscode/TextEdit.replace range pretty)]))))


(defn activate [^js context]
  (let [scheme      #js {:language "clojure" :scheme "file"}
        output      (vscode/window.createOutputChannel "cljfmt")
        provider    (ClojureDocumentRangeFormattingEditProvider. output)]

    (.push context.subscriptions (vscode/Disposable.from output))

    (.push context.subscriptions (vscode/languages.registerDocumentRangeFormattingEditProvider scheme provider))))