(ns calva.fmt.indenter-test
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.indenter :as sut]))

(deftest gen-indent-symbol
  (is (some? (:indent-symbol (sut/gen-indent-symbol {})))))

(deftest split
  (is (= [" " " "]
         (sut/split "  " 1)))
  (is (= ["(foo\n " " \n bar)"]
         (sut/split "(foo\n  \n bar)" 6))))

(deftest inject-indent-symbol
  (is (= "(foo\n  FOO \n bar)"
         (:text (sut/inject-indent-symbol {:text "(foo\n  \n bar)"
                                           :indent-symbol "FOO"
                                           :local-idx 7})))))

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

(deftest indent-for-index
  (is (= 5
         (:indent (sut/indent-for-index {:all-text "(def a 1)
(defn a [b]
  (let [c 1]
    (foo

    bar)))"
                                         :idx 45}))))
  (is (= 0
         (:indent (sut/indent-for-index {:all-text "(defn a
  []
  do-some-stuff)

(def a :b)"
                                         :idx 30}))))
  (is (= 9
         (:indent (sut/indent-for-index {:all-text "(foo-bar a

)"
                                         :idx 11})))))