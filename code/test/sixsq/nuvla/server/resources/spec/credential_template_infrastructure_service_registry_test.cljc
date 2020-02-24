(ns sixsq.nuvla.server.resources.spec.credential-template-infrastructure-service-registry-test
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test :refer [deftest is]]
    [sixsq.nuvla.server.resources.credential :as p]
    [sixsq.nuvla.server.resources.credential-template :as ct]
    [sixsq.nuvla.server.resources.credential-template-infrastructure-service-registry :as service]
    [sixsq.nuvla.server.resources.spec.credential-template-infrastructure-service-registry
     :as service-spec]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]))


(def valid-acl service/resource-acl)


(deftest test-credential-template-service-schema-check
  (let [timestamp "1972-10-08T10:00:00.00Z"
        tpl       {:id            (str ct/resource-type "/uuid")
                   :resource-type p/resource-type
                   :created       timestamp
                   :updated       timestamp
                   :acl           valid-acl
                   :subtype       service/credential-subtype
                   :method        service/method
                   :parent        "infrastructure-service/service-1"
                   :username      "foo"
                   :password      "bar"}]

    (is (s/valid? ::service-spec/schema tpl))

    ;; mandatory keys
    (doseq [k (-> tpl (dissoc :parent :username :password) keys set)]
      (stu/is-invalid ::service-spec/schema (dissoc tpl k)))

    ;; optional keys
    (stu/is-valid ::service-spec/schema (dissoc tpl :username))

    (doseq [k #{:parent :username :password}]
      (stu/is-valid ::service-spec/schema (dissoc tpl k)))))
