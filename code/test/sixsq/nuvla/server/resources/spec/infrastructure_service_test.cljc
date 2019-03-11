(ns sixsq.nuvla.server.resources.spec.infrastructure-service-test
  (:require
    [clojure.test :refer [deftest]]
    [sixsq.nuvla.server.resources.infrastructure-service :as service-resource]
    [sixsq.nuvla.server.resources.spec.infrastructure-service :as infrastructure-service]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]))


(def valid-acl {:owner {:principal "ADMIN"
                        :type      "ROLE"}
                :rules [{:principal "jane"
                         :type      "USER"
                         :right     "VIEW"}]})


(deftest check-service
  (let [timestamp "1964-08-25T10:00:00.0Z"
        service {:id            (str service-resource/resource-type "/uuid")
                 :resource-type service-resource/resource-type
                 :created       timestamp
                 :updated       timestamp
                 :acl           valid-acl

                 :parent        "infrastructure-service-group/abcdef"

                 :method        "generic"
                 :type          "docker"
                 :endpoint      "https://docker.example.org/api"
                 :state         "STARTED"}]

    (stu/is-valid ::infrastructure-service/schema service)

    ;; mandatory keywords
    (doseq [k #{:id :resource-type :created :updated :acl :parent :method :type :state}]
      (stu/is-invalid ::infrastructure-service/schema (dissoc service k)))

    ;;optional keywords
    (doseq [k #{:endpoint}]
      (stu/is-valid ::infrastructure-service/schema (dissoc service k)))))