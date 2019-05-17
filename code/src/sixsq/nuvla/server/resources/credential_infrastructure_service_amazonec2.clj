(ns sixsq.nuvla.server.resources.credential-infrastructure-service-amazonec2
  "
Sets the service compliant attribute names and values
for AWS
"
  (:require
    [sixsq.nuvla.server.resources.common.std-crud :as std-crud]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.credential :as p]
    [sixsq.nuvla.server.resources.credential-template-infrastructure-service-amazonec2 :as tpl]
    [sixsq.nuvla.server.resources.spec.credential-infrastructure-service-amazonec2 :as service]))

;;
;; convert template to credential
;;

(defmethod p/tpl->credential tpl/credential-type
  [{:keys [type method amazonec2-access-key amazonec2-secret-key parent acl]} request]
  (let [resource (cond-> {:resource-type        p/resource-type
                          :type                 type
                          :method               method
                          :amazonec2-access-key amazonec2-access-key
                          :amazonec2-secret-key amazonec2-secret-key
                          :parent               parent}
                         acl (assoc :acl acl))]
    [nil resource]))


;;
;; multimethods for validation
;;

(def validate-fn (u/create-spec-validation-fn ::service/schema))
(defmethod p/validate-subtype tpl/credential-type
  [resource]
  (validate-fn resource))

(def create-validate-fn (u/create-spec-validation-fn ::service/schema-create))
(defmethod p/create-validate-subtype tpl/credential-type
  [resource]
  (create-validate-fn resource))


;;
;; initialization
;;

(defn initialize
  []
  (std-crud/initialize p/resource-type ::service/schema))
