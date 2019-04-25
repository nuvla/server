(ns sixsq.nuvla.auth.utils.acl-test
  (:refer-clojure :exclude [update])
  (:require
    [clojure.test :refer :all]
    [sixsq.nuvla.auth.utils.acl :as t]))


(deftest check-normalize
  (are [arg expected] (= expected (t/normalize-acl arg))

                      {:owners ["b" "a"]}
                      {:owners ["a" "b"]}

                      {:owners ["a" "b"]
                       :edit-acl ["a"]
                       :view-acl ["b"]}
                      {:owners ["a" "b"]}

                      {:owners ["a" "b"]
                       :edit-acl ["a" "c"]}
                      {:owners ["a" "b"]
                       :edit-acl ["c"]}

                      ))