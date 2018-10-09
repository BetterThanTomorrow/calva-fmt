(ns calva.fmt.util-test
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.util :as sut]))


#_(deftest log
    (is (= (with-out-str (sut/log {:text ""} :text))
           {:text ""})))


(def all-text "(def a 1)


(defn foo [x] (let [bar 1]

bar))")

(deftest current-line
  (is (= "(def a 1)" (sut/current-line all-text 0)))
  (is (= "(def a 1)" (sut/current-line all-text 4)))
  (is (= "(def a 1)" (sut/current-line all-text 9)))
  (is (= "" (sut/current-line all-text 10)))
  (is (= "" (sut/current-line all-text 11)))
  (is (= "(defn foo [x] (let [bar 1]" (sut/current-line all-text 12)))
  (is (= "(defn foo [x] (let [bar 1]" (sut/current-line all-text 27)))
  (is (= "(defn foo [x] (let [bar 1]" (sut/current-line all-text 38)))
  (is (= "" (sut/current-line all-text 39)))
  (is (= "bar))" (sut/current-line all-text (count all-text)))))


(deftest indent-before-range
  (is (= 10
         (sut/indent-before-range {:all-text all-text :range [22 25]}))))


(deftest localize-index
  (is (= 0 (:local-idx (sut/localize-index {:range [12 15] :idx 12}))))
  (is (= 2 (:local-idx (sut/localize-index {:range [12 15] :idx 14}))))
  (is (= (- 444 12) (:local-idx (sut/localize-index {:range [12 15] :idx 444})))))


(deftest enclosing-range
  (is (= [22 25] ;"[x]"
         (:range (sut/enclosing-range {:all-text all-text :idx 23}))))
  (is (= [12 45] ;"enclosing form"
         (:range (sut/enclosing-range {:all-text all-text :idx 21}))))
  (is (= [0 9] ; after top level form
         (:range (sut/enclosing-range {:all-text all-text :idx 9}))))
  (is (= [0 9] ; before top level form
         (:range (sut/enclosing-range {:all-text all-text :idx 0}))))
  (is (= [26 44] ; before top level form
         (:range (sut/enclosing-range {:all-text all-text :idx 39}))))
  (is (= [10 10] ; void (between top level forms)
         (:range (sut/enclosing-range {:all-text all-text :idx 10})))))


(deftest re-pos-one
  (is (= 6
         (sut/re-pos-first "\\s*x\\s*t$" "foo te x t")))
  (is (= 6
         (sut/re-pos-first "\\s*x\\s*t$" "foo te x t")))
  (is (= 5
         (sut/re-pos-first "\\s*e\\s*xt\\s*$" "foo te xt")))
  (is (= 173
         (sut/re-pos-first "\"\\s*#\\s*\"\\)$" "(create-state \"\"
                                 \"###  \"
                                 \"  ###\"
                                 \" ### \"
                                 \"  #  \")"))))


(deftest inject-indent-symbol
  (is (= "(foo\n  FOO \n bar)"
         (:text (sut/inject-indent-symbol {:text "(foo\n  \n bar)"
                                           :indent-symbol "FOO"
                                           :local-idx 7})))))


(deftest escapeRegExp
  (is (= "\\.\\*"
         (sut/escapeRegExp ".*"))))


(deftest gen-indent-symbol
  (is (some? (:indent-symbol (sut/gen-indent-symbol {})))))

(deftest split
  (is (= [" " " "]
         (sut/split "  " 1)))
  (is (= ["(foo\n " " \n bar)"]
         (sut/split "(foo\n  \n bar)" 6))))

(deftest find-indent
  (is (= 0
         (sut/find-indent {:all-text "(defn foo [x]
  (let [bar 1]
    bar))
FOO"
                           :text "FOO"
                           :indent-symbol "FOO"
                           :range [45 45]})))
  (is (= 4
         (sut/find-indent {:all-text "(defn foo [x]\n  (let [bar 1]\n  FOO bar))"
                           :text "(let [bar 1] \n  FOO bar)"
                           :range [16 40]
                           :indent-symbol "FOO"}))))