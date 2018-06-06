(ns calva.fmt.core
  (:require
   ["vscode" :as vscode]
   [calva.fmt.range-formatting-provider :as range-formatting]))

(defn activate [^js context]
  (let [scheme        #js {:language "clojure" :scheme "file"}
        provider      (range-formatting/RangeEditProvider.)]
    (.push context.subscriptions (vscode/languages.registerDocumentRangeFormattingEditProvider scheme provider))))