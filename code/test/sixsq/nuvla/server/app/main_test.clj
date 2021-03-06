(ns sixsq.nuvla.server.app.main-test
  (:require
    [clojure.test :refer [are deftest is]]
    [sixsq.nuvla.server.app.main :as t]))

(deftest check-parse-port
  (are [expected arg] (is (= expected (t/parse-port arg)))
                      nil nil
                      nil (System/getProperties)
                      nil "badport"
                      nil "-1"
                      nil "65536"
                      nil "1.3"
                      1 "1"
                      65535 "65535"))
