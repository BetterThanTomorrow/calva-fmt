(ns calva.fmt.playground
  (:require [cljfmt.core :as cljfmt]
            [calva.fmt.util :as util]))

(println
 (cljfmt/reformat-string
  "(bar

)"
  {:remove-surrounding-whitespace? false
   :remove-trailing-whitespace? false
   :remove-consecutive-blank-lines? false}))