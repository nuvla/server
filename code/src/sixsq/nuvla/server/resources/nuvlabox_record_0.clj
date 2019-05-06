(ns sixsq.nuvla.server.resources.nuvlabox-record-0
  "
The nuvlabox-record (version 0) contains attributes to describe and configure
a NuvlaBox.
"
  (:require
    [sixsq.nuvla.server.resources.common.std-crud :as std-crud]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.nuvlabox-record :as nb-record]
    [sixsq.nuvla.server.resources.resource-metadata :as md]
    [sixsq.nuvla.server.resources.spec.nuvlabox-record-0 :as nb-record-0]
    [sixsq.nuvla.server.util.metadata :as gen-md]))


(def schema-version 0)


;;
;; multimethod for validation
;;

(def validate-fn (u/create-spec-validation-fn ::nb-record-0/schema))


(defmethod nb-record/validate-subtype schema-version
  [resource]
  (validate-fn resource))


;;
;; initialization
;;

(defn initialize
  []
  (std-crud/initialize nb-record/resource-type ::nb-record-0/schema)
  (md/register (gen-md/generate-metadata ::ns ::nb-record/ns ::nb-record-0/schema schema-version)))
