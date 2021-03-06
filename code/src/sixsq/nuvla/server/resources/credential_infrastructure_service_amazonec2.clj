(ns sixsq.nuvla.server.resources.credential-infrastructure-service-amazonec2
  "
Provides `docker-machine` credentials for AWS EC2. The attribute names
correspond exactly to those required by `docker-machine`.
"
  (:require
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.credential :as p]
    [sixsq.nuvla.server.resources.credential-template-infrastructure-service-amazonec2 :as tpl]
    [sixsq.nuvla.server.resources.resource-metadata :as md]
    [sixsq.nuvla.server.resources.spec.credential-infrastructure-service-amazonec2 :as service]
    [sixsq.nuvla.server.util.metadata :as gen-md]))

;;
;; convert template to credential
;;

(defmethod p/tpl->credential tpl/credential-subtype
  [{:keys [subtype method amazonec2-access-key amazonec2-secret-key acl]} _request]
  (let [resource (cond-> {:resource-type        p/resource-type
                          :subtype              subtype
                          :method               method
                          :amazonec2-access-key amazonec2-access-key
                          :amazonec2-secret-key amazonec2-secret-key}
                         acl (assoc :acl acl))]
    [nil resource]))


;;
;; multimethods for validation
;;

(def validate-fn (u/create-spec-validation-fn ::service/schema))


(defmethod p/validate-subtype tpl/credential-subtype
  [resource]
  (validate-fn resource))


(def create-validate-fn (u/create-spec-validation-fn ::service/schema-create))


(defmethod p/create-validate-subtype tpl/credential-subtype
  [resource]
  (create-validate-fn resource))


;;
;; initialization
;;

(def resource-metadata (gen-md/generate-metadata ::ns ::p/ns ::service/schema))


(defn initialize
  []
  (md/register resource-metadata))
