(ns calva.fmt.util-test
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.util :as sut]))


#_(deftest log
    (is (= (with-out-str (sut/log {:text ""} :text))
           {:text ""})))


(def all-text "(def a 1)


(defn foo [x] (let [bar 1]

bar))")


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