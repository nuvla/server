(ns sixsq.nuvla.server.resources.spec.user-template-self-registration-test
  (:require
    [clojure.test :refer [deftest is]]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]
    [sixsq.nuvla.server.resources.spec.user-template-self-registration :as ut-auto]
    [sixsq.nuvla.server.resources.user-template :as st]))

(def valid-acl {:owner {:principal "ADMIN"
                        :type      "ROLE"}
                :rules [{:type      "ROLE",
                         :principal "ADMIN",
                         :right     "ALL"}]})

(deftest check-user-template-self-registration-schema
  (let [timestamp "1964-08-25T10:00:00.0Z"
        tpl {:id             (str st/resource-type "/internal")
             :resource-type  st/resource-type
             :name           "my-template"
             :description    "my template"
             :group          "my group"
             :tags           #{"1", "2"}
             :created        timestamp
             :updated        timestamp
             :acl            valid-acl

             :method         "self-registration"
             :instance       "self-registration"

             :username       "user"
             :password       "plaintext-password"
             :passwordRepeat "plaintext-password"
             :emailAddress   "someone@example.org"}

        create-tpl {:name          "my-create"
                    :description   "my create description"
                    :tags          #{"3", "4"}
                    :resource-type "user-template-create"
                    :template      (dissoc tpl :id)}]

    ;; check the registration schema (without href)
    (stu/is-valid ::ut-auto/schema tpl)

    (doseq [attr #{:id :resource-type :created :updated :acl
                   :method :username :password :passwordRepeat :emailAddress}]
      (stu/is-invalid ::ut-auto/schema (dissoc tpl attr)))

    (doseq [attr #{:name :description :tags}]
      (stu/is-valid ::ut-auto/schema (dissoc tpl attr)))

    ;; check the create template schema (with href)
    (stu/is-valid ::ut-auto/schema-create create-tpl)
    (stu/is-valid ::ut-auto/schema-create (assoc-in create-tpl [:template :href] "user-template/abc"))
    (stu/is-invalid ::ut-auto/schema-create (assoc-in create-tpl [:template :href] "bad-reference/abc"))

    (doseq [attr #{:resource-type :template}]
      (stu/is-invalid ::ut-auto/schema-create (dissoc create-tpl attr)))

    (doseq [attr #{:name :description :tags}]
      (stu/is-valid ::ut-auto/schema-create (dissoc create-tpl attr)))))