(ns calva.fmt.util-test
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.util :as sut]))


#_(deftest log
    (is (= (with-out-str (sut/log {:text ""} :text))
           {:text ""})))


(def all-text "(def a 1)


(defn foo [x] (let [bar 1]

bar))")


(deftest add-head-and-tail
  (is (= {:head "" :tail all-text
          :all-text all-text
          :idx 0}
         (sut/add-head-and-tail {:all-text all-text :idx 0})))
  (is (= {:head all-text :tail ""
          :all-text all-text
          :idx (count all-text)}
         (sut/add-head-and-tail {:all-text all-text :idx (count all-text)})))
  (is (= {:head "(def a 1)\n\n\n(defn foo "
          :tail "[x] (let [bar 1]\n\nbar))"
          :all-text all-text
          :idx 22}
         (sut/add-head-and-tail {:all-text all-text :idx 22})))
  (is (= {:head all-text :tail ""
          :all-text all-text
          :idx (inc (count all-text))}
         (sut/add-head-and-tail {:all-text all-text :idx (inc (count all-text))}))))


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


(deftest current-line-empty?
  (is (= true (sut/current-line-empty? {:current-line "       "})))
  (is (= false (sut/current-line-empty? {:current-line "  foo  "}))))

(deftest indent-before-range
  (is (= 10
         (sut/indent-before-range {:all-text all-text :range [22 25]}))))


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


(deftest escape-regexp
  (is (= "\\.\\*"
         (sut/escape-regexp ".*"))))