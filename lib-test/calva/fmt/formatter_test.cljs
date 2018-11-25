(ns calva.fmt.formatter-test
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.formatter :as sut]))

;; TODO: Fix this bug (gazonk should be indented twice)
#_(deftest format-text
    (is (= "  (foo\n   bar\n   baz)\n  gazonk"
           (:range-text (sut/format-text {:range-text "  (foo   \nbar\n      baz)\ngazonk"})))))


(deftest format-text-at-range
  (is (= "(foo)\n(defn bar\n  [x]\n  baz)"
         (:range-text (sut/format-text-at-range {:all-text "  (foo)\n(defn bar\n[x]\nbaz)" :range [2 26]})))))


(deftest format-text-at-range-old
  (is (= "(foo)\n  (defn bar\n    [x]\n    baz)"
         (:range-text (sut/format-text-at-range-old {:all-text "  (foo)\n(defn bar\n[x]\nbaz)" :range [2 26]})))))


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
         (:range (sut/format-text-at-idx {:all-text all-text :idx 11})))))


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


(def head-and-tail-text "(def a 1)


(defn foo [x] (let [bar 1]

bar))")


(deftest add-head-and-tail
  (is (= {:head "" :tail head-and-tail-text
          :all-text head-and-tail-text
          :idx 0}
         (sut/add-head-and-tail {:all-text head-and-tail-text :idx 0})))
  (is (= {:head head-and-tail-text :tail ""
          :all-text head-and-tail-text
          :idx (count head-and-tail-text)}
         (sut/add-head-and-tail {:all-text head-and-tail-text :idx (count head-and-tail-text)})))
  (is (= {:head "(def a 1)\n\n\n(defn foo "
          :tail "[x] (let [bar 1]\n\nbar))"
          :all-text head-and-tail-text
          :idx 22}
         (sut/add-head-and-tail {:all-text head-and-tail-text :idx 22})))
  (is (= {:head head-and-tail-text :tail ""
          :all-text head-and-tail-text
          :idx (inc (count head-and-tail-text))}
         (sut/add-head-and-tail {:all-text head-and-tail-text :idx (inc (count head-and-tail-text))}))))


(deftest normalize-indents
  (is (= "(foo)\n  (defn bar\n    [x]\n    baz)"
         (:range-text (sut/normalize-indents {:all-text "  (foo)\n(defn bar\n[x]\nbaz)"
                                              :range [2 26]
                                              :range-text "(foo)\n(defn bar\n  [x]\n  baz)"})))))


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


;; These fail, leading to a horrible behaviour when creating new lines top level
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
  (is (= "(bar \n\n )"
         (:range-text (sut/format-text-at-idx-on-type {:all-text "(bar \n\n)" :idx 7}))))
  (is (= "(bar \n \n )"
         (:range-text (sut/format-text-at-idx-on-type {:all-text "(bar \n \n)" :idx 8}))))
  (is (= "(bar \n \n )"
         (:range-text (sut/format-text-at-idx-on-type {:all-text "(bar \n\n)" :idx 6}))))
  (is (= "\"bar \n \n \""
         (:range-text (sut/format-text-at-idx-on-type {:all-text "\"bar \n \n \"" :idx 7}))))
  (is (= "\"bar \n \n \""
         (:range-text (sut/format-text-at-idx-on-type {:all-text "\"bar \n \n \"" :idx 7}))))
  (is (= "'([]\n    [])"
         (:range-text (sut/format-text-at-idx-on-type {:all-text "  '([]\n[])" :idx 7})))))


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


(deftest current-line-empty?
  (is (= true (sut/current-line-empty? {:current-line "       "})))
  (is (= false (sut/current-line-empty? {:current-line "  foo  "}))))


(deftest indent-before-range
  (is (= 10
         (sut/indent-before-range {:all-text "(def a 1)


(defn foo [x] (let [bar 1]

bar))" :range [22 25]})))
  (is (= 4
         (sut/indent-before-range {:all-text "  '([]
[])" :range [4 9]}))))


(def enclosing-range-text "(def a 1)


(defn foo [x] (let [bar 1]

bar))")


(deftest enclosing-range
  (is (= [22 25] ;"[x]"
         (:range (sut/enclosing-range {:all-text enclosing-range-text :idx 23}))))
  (is (= [12 45] ;"enclosing form"
         (:range (sut/enclosing-range {:all-text enclosing-range-text :idx 21}))))
  (is (= [0 9] ; after top level form
         (:range (sut/enclosing-range {:all-text enclosing-range-text :idx 9}))))
  (is (= [0 9] ; before top level form
         (:range (sut/enclosing-range {:all-text enclosing-range-text :idx 0}))))
  (is (= [26 44] ; before top level form
         (:range (sut/enclosing-range {:all-text enclosing-range-text :idx 39}))))
  (is (= [10 10] ; void (between top level forms)
         (:range (sut/enclosing-range {:all-text enclosing-range-text :idx 10}))))
  (is (= [5 5]
         (:range (sut/enclosing-range {:all-text "  []\n  \n[]" :idx 5}))))
  (is (= [1 7]
         (:range (sut/enclosing-range {:all-text " ([][])" :idx 4}))))
  (is (= [1 6]
         (:range (sut/enclosing-range {:all-text " (\"[\")" :idx 4}))))
  (is (= [1 12]
         (:range (sut/enclosing-range {:all-text " {:foo :bar}" :idx 2}))))
  (is (= [1 13]
         (:range (sut/enclosing-range {:all-text " #{:foo :bar}" :idx 8}))))
  (is (= [1 12]
         (:range (sut/enclosing-range {:all-text " '(:foo bar)" :idx 8})))))
