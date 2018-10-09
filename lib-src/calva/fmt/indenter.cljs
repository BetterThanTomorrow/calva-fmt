(ns calva.fmt.indenter
  (:require [calva.fmt.util :as util]
            [calva.fmt.formatter :refer [format-text]]
            [calva.js-utils :refer [cljify]]
            ["paredit.js" :as paredit]))


(defn indent-for-index
  "Figures out how where a cursor placed at `idx` should be placed"
  [{:keys [all-text idx config] :as m}]
  {:pre [(string? all-text)
         (number? idx)
         (or (map? config) (nil? config))]}
  (try
    (assoc m :indent  (-> m
                          (assoc-in [:config :remove-surrounding-whitespace?] false)
                          (util/gen-indent-symbol)
                          (util/enclosing-range)
                          (#(assoc % :text (apply subs all-text (:range %))))
                          (util/localize-index)
                          (util/inject-indent-symbol)
                          (format-text)
                          (util/find-indent)))
    (catch js/Error e
      {:error e})))