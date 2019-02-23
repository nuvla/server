(ns sixsq.nuvla.server.resources.spec.service-test
  (:require
    [clojure.test :refer [deftest]]
    [sixsq.nuvla.server.resources.service :as service-resource]
    [sixsq.nuvla.server.resources.spec.service :as service]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]))


(def valid-acl {:owner {:principal "ADMIN"
                        :type      "ROLE"}
                :rules [{:principal "jane"
                         :type      "USER"
                         :right     "VIEW"}]})


(deftest check-job
  (let [timestamp "1964-08-25T10:00:00.0Z"
        service {:id            (str service-resource/resource-type "/uuid")
                 :resource-type service-resource/resource-type
                 :created       timestamp
                 :updated       timestamp
                 :acl           valid-acl

                 :type          "docker"
                 :endpoint      "https://docker.example.org/api"
                 :accessible    true}]

    (stu/is-valid ::service/schema service)

    ;; mandatory keywords
    (doseq [k #{:id :resource-type :created :updated :acl :type :accessible}]
      (stu/is-invalid ::service/schema (dissoc service k)))

    ;;optional keywords
    (doseq [k #{:endpoint}]
      (stu/is-valid ::service/schema (dissoc service k)))))
