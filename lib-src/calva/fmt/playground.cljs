(ns calva.fmt.playground
  (:require [cljfmt.core :as cljfmt]
            [zprint.core :refer [zprint-str]]
            [calva.fmt.util :as util]))


(comment
  (foo
   ;;
   bar
   baz))

(comment
  (def str "(defn \n\n)")

  (cljfmt/reformat-string str {:remove-surrounding-whitespace? false
                               :remove-trailing-whitespace? false
                               :remove-consecutive-blank-lines? false})

  (cljfmt/reformat-string "(foo
       ;;
   bar
   baz)"
                          {:remove-surrounding-whitespace? false
                           :remove-trailing-whitespace? false
                           :remove-consecutive-blank-lines? false})

  (cljfmt/reformat-string
   "(foo
  
)"
   {:remove-surrounding-whitespace? false
    :remove-trailing-whitespace? false
    :indentation? false})

  (cljfmt/reformat-string "(ns ui-app.re-frame.db)

(def default-db #::{:page :home})")

  (cljfmt/reformat-string
   "(defn bar\n    [x]\n\n    baz)")

  (zprint-str "(defn bar\n    [x]\n\n    baz)"
              {:style :community
               :parse-string-all? true
               :fn-force-nl #{:arg1-body}})

  (cljfmt/reformat-string
   "(defn bar\n    [x]\n\n    baz)")

  "(defn bar\n    [x]\n  \n    baz)"

  (cljfmt/reformat-string
   ";; foo
(defn foo [x]
  (* x x))
  0")

  (div
  ;; foo
   [:div]
  ;; bar
   [:div]))
