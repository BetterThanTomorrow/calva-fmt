(ns vscode-cljfmt.core
  (:require
   ["vscode" :as vscode]

   [cljfmt.core :as cljfmt]))


(deftype ClojureDocumentRangeFormattingEditProvider []
  Object
  (provideDocumentRangeFormattingEdits [_ document range options token]
    (let [text (.getText document range)
          pretty (cljfmt/reformat-string text {:remove-consecutive-blank-lines? false})
          edit (vscode/TextEdit.replace range pretty)]
      #js [edit])))


(defn activate [^js context]
  (let [scheme   #js {:language "clojure"
                      :scheme   "file"}

        provider (ClojureDocumentRangeFormattingEditProvider.)

        disposable (vscode/languages.registerDocumentRangeFormattingEditProvider scheme provider)]

    (.push context.subscriptions disposable)))