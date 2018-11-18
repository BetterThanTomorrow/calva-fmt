(ns calva.fmt.formatter-test
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.formatter :as sut]))

;; TODO: Fix this bug (gazonk should be indented twice)
#_(deftest format-text
    (is (= "  (foo\n   bar\n   baz)\n  gazonk"
           (:range-text (sut/format-text {:range-text "  (foo   \nbar\n      baz)\ngazonk"})))))


(deftest normalize-indents
  (is (= "(foo)\n  (defn bar\n    [x]\n    baz)"
         (:range-text (sut/normalize-indents {:all-text "  (foo)\n(defn bar\n[x]\nbaz)"
                                              :range [2 26]
                                              :range-text "(foo)\n(defn bar\n  [x]\n  baz)"}))))
  (is (= "(foo\n;;\n  foo\n    ;;bar\n  bar)"
         (:range-text (sut/normalize-indents {:all-text " (foo\n;;\n      foo\n    ;;\n      bar)"
                                              :range [1 36]
                                              :range-text "(foo\n;;\n      foo\n    ;;\n      bar)"})))))


(deftest format-text-at-range
  (is (= "(foo)\n  (defn bar\n    [x]\n    baz)"
         (:range-text (sut/format-text-at-range {:all-text "  (foo)\n(defn bar\n[x]\nbaz)" :range [2 26]})))))


(def all-text "  (foo)
  (defn bar
         [x]

baz)")


(deftest format-text-at-idx
  (is (= "(defn bar
    [x]
  
    baz)"
         (:range-text (sut/format-text-at-idx {:all-text all-text :idx 11}))))
  (is (= 1
         (:new-index (sut/format-text-at-idx {:all-text all-text :idx 11}))))
  (is (= [10 38]
         (:range (sut/format-text-at-idx {:all-text all-text :idx 11}))))
  (is (= "\"bar \n \n \""
         (:range-text (sut/format-text-at-idx-on-type {:all-text "\"bar \n \n \"" :idx 7})))))


(deftest new-index
  (is (= 1
         (:new-index (sut/format-text-at-idx {:all-text all-text :idx 11}))))
  (is (= 13
         (:new-index (sut/format-text-at-idx {:all-text all-text :idx 28}))))
  (is (= 10
         (:new-index (sut/format-text-at-idx {:all-text all-text :idx 22}))))
  (is (= 12
         (:new-index (sut/format-text-at-idx {:all-text all-text :idx 27}))))
  (is (= 22
         (:new-index (sut/format-text-at-idx {:all-text all-text :idx 33}))))
  (is (= 5
         (:new-index (sut/format-text-at-idx {:all-text "(defn \n  \nfoo)" :idx 6}))))
  (is (= 11
         (:new-index (sut/format-text-at-idx {:all-text "(foo\n (bar)\n )" :idx 11})))))


(def first-top-level-text "
;; foo
(defn foo [x]
  (* x x))
 ")

(def mid-top-level-text ";; foo
(defn foo [x]
  (* x x))
 
(bar)")

(def last-top-level-text ";; foo
(defn foo [x]
  (* x x))
 ")


(deftest new-index-top-level
  (is (= 1
         (:new-index (sut/format-text-at-idx {:all-text first-top-level-text :idx 1}))))
  (is (= first-top-level-text
         (:range-text (sut/format-text-at-idx {:all-text first-top-level-text :idx 1}))))
  (is (= 32
         (:new-index (sut/format-text-at-idx {:all-text mid-top-level-text :idx 33}))))
  (is (= mid-top-level-text
         (:range-text (sut/format-text-at-idx {:all-text mid-top-level-text :idx 33}))))
  (is (= 32
         (:new-index (sut/format-text-at-idx {:all-text last-top-level-text :idx 32}))))
  (is (= last-top-level-text
         (:range-text (sut/format-text-at-idx {:all-text last-top-level-text :idx 32})))))


(deftest format-text-at-idx-on-type
  (comment ;; https://github.com/weavejester/cljfmt/issues/142
    (is (= "(bar \n \n )"
           (:range-text (sut/format-text-at-idx-on-type {:all-text "(bar \n\n)" :idx 7})))))
  (is (= "\"bar \n \n \""
         (:range-text (sut/format-text-at-idx-on-type {:all-text "\"bar \n \n \"" :idx 7})))))


(deftest new-index-on-type
  (is (= 6
         (:new-index (sut/format-text-at-idx-on-type {:all-text "(defn \n)" :idx 6}))))
  (is (= 6
         (:new-index (sut/format-text-at-idx-on-type {:all-text "(defn \n  )" :idx 6}))))
  (is (= 6
         (:new-index (sut/format-text-at-idx-on-type {:all-text "(defn \n  \n  )" :idx 6}))))
  (is (= 6
         (:new-index (sut/format-text-at-idx-on-type {:all-text "(defn \n\n  )" :idx 6}))))
  (is (= 11
         (:new-index (sut/format-text-at-idx-on-type {:all-text "(foo\n (bar)\n )" :idx 11})))))


(deftest index-for-tail-in-range
  (is (= 7
         (:new-index (sut/index-for-tail-in-range
                      {:range-text "foo te    x t"
                       :range-tail "   x t"}))))
  (is (= 169
         (:new-index (sut/index-for-tail-in-range
                      {:range-text "(create-state \"\"
                                \"###  \"
                                \"  ###\"
                                \" ### \"
                                \"  #  \")"
                       :range-tail "\"  #  \")"})))))


(deftest remove-indent-token-if-empty-current-line
  (is (= {:range-text "foo\n\nbar"
          :range [4 4]
          :current-line ""
          :new-index 4}
         (sut/remove-indent-token-if-empty-current-line {:range-text "foo\n0\nbar"
                                                         :range [4 5]
                                                         :new-index 4
                                                         :current-line ""})))
  (is (= {:range-text "foo\n0\nbar"
          :range [4 5]
          :current-line "0"
          :new-index 4}
         (sut/remove-indent-token-if-empty-current-line {:range-text "foo\n0\nbar"
                                                         :range [4 5]
                                                         :new-index 4
                                                         :current-line "0"}))))