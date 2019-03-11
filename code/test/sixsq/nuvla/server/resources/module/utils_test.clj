(ns sixsq.nuvla.server.resources.module.utils-test
  (:require
    [clojure.test :refer [are deftest is]]
    [sixsq.nuvla.server.resources.module.utils :as t]))


(deftest split-resource
  (is (= [{:alpha 1, :beta 2} {:gamma 3}]
         (t/split-resource {:alpha   1
                            :beta    2
                            :content {:gamma 3}}))))


(deftest check-parent-path
  (are [parent-path path] (= parent-path (t/get-parent-path path))
                          nil nil
                          "" "alpha"
                          "alpha" "alpha/beta"
                          "alpha/beta" "alpha/beta/gamma"))


(deftest check-set-parent-path
  (are [expected arg] (= expected (:parent-path (t/set-parent-path arg)))
                      "ok" {:parent-path "bad-value"
                            :path        "ok/go"}
                      nil {}))