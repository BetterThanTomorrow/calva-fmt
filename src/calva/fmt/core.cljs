(ns calva.fmt.core
  (:require
   ["vscode" :as vscode]
   [calva.fmt.range-formatting-provider :as range-formatting]
   [calva.fmt.ontype-formatting-provider :as ontype-formatting]
   [calva.fmt.language-config :as language-config]))

(defn activate [^js context]
  ;;Set the language configuration for vscode when using this extension
  (vscode/languages.setLanguageConfiguration "clojure", (language-config/ClojureLanguageConfiguration.))

  (.push (.-subscriptions context) (vscode/commands.registerCommand "calva.fmt.toggleAutoAdjustIndent" ontype-formatting/toggleAutoAdjustIndentCommand))
  (.push (.-subscriptions context) (vscode/languages.registerDocumentRangeFormattingEditProvider "clojure" (range-formatting/RangeEditProvider.)))
  (.push (.-subscriptions context) (vscode/languages.registerDocumentRangeFormattingEditProvider "clojure" (ontype-formatting/OnTypeEditProvider.) "\n"))

  (ontype-formatting/init))