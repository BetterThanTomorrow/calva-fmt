(ns calva.fmt.language-config)

(defn ClojureLanguageConfiguration []
  (this-as this
           (set! (.-wordPattern this) #"[^\[\]\(\)\{\};\s\"\\]+")
           (set! (.-indentationRules this) #js {:increaseIndentPattern #"[\[\(\{]"
                                                :decreaseIndentPattern nil})))