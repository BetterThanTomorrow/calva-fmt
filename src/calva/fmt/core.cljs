(ns calva.fmt.core
  (:require
   ["vscode" :as vscode]
   [calva.fmt.range-formatting-provider :as range-formatting]))

(defn activate [^js context]
  (let [provider (range-formatting/RangeEditProvider.)]
    (.push context.subscriptions (vscode/languages.registerDocumentRangeFormattingEditProvider "clojure" provider))))