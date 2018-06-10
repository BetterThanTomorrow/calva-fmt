(ns calva.fmt.core
  (:require
   ["vscode" :as vscode]
   [calva.fmt.range-formatting-provider :as range-formatting]
   [calva.fmt.ontype-formatting-provider :as ontype-formatting]
   [calva.fmt.language-config :as language-config]))

(defn activate [^js context]
  ;; Languae configuration
  (vscode/languages.setLanguageConfiguration "clojure", (language-config/ClojureLanguageConfiguration.))

  ;; Commands
  (.push (.-subscriptions context) (vscode/commands.registerCommand "calva.fmt.toggleAutoAdjustIndent" ontype-formatting/toggleAutoAdjustIndentCommand))

  ;; Providers
  (.push (.-subscriptions context) (vscode/languages.registerDocumentRangeFormattingEditProvider "clojure" (range-formatting/RangeEditProvider.)))
  (.push (.-subscriptions context) (vscode/languages.registerOnTypeFormattingEditProvider "clojure" (ontype-formatting/OnTypeEditProvider.) "\n"))

  ;; Inititalization
  (ontype-formatting/init))