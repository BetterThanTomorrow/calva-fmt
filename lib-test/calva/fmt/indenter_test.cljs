(ns calva.fmt.indenter-test
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [calva.fmt.indenter :as sut]))

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