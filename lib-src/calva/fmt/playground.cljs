(ns calva.fmt.playground
  (:require [cljfmt.core :as cljfmt]
            [zprint.core :refer [zprint-str]]
            [calva.fmt.util :as util]))


(def str "(defn \n\n)")

(cljfmt/reformat-string str {:remove-surrounding-whitespace? false
                             :remove-trailing-whitespace? false
                             :remove-consecutive-blank-lines? false})

(cljfmt/reformat-string
 "(defn bar\n    [x]\n\n    baz)")

(zprint-str "(defn bar\n    [x]\n\n    baz)"
            {:style :community
             :parse-string-all? true
             :fn-force-nl #{:arg1-body}})

(cljfmt/reformat-string
 "(defn bar\n    [x]\n\n    baz)")


"(defn bar\n    [x]\n  \n    baz)"

(div
 ;; foo
 [:div]
 ;; bar
 [:div])