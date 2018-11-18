(ns calva.fmt.playground
  (:require [cljfmt.core :as cljfmt]
            [zprint.core :refer [zprint-str]]
            [calva.fmt.util :as util]))


(comment
  (div
   (foo
    ; foo
    [:div]
    ;;bar
    [:div])))

(comment
  (foo
  ;; foo
   [:div]
  ;;bar
   [:div]))

(comment
  (def str "(defn \n\n)")

  (cljfmt/reformat-string str {:remove-surrounding-whitespace? false
                               :remove-trailing-whitespace? false
                               :remove-consecutive-blank-lines? false})

  (cljfmt/reformat-string "(div\n (foo\n  ;; foo\n  [:div]\n  ;;bar\n  [:div]))"
                          {:remove-surrounding-whitespace? false
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
