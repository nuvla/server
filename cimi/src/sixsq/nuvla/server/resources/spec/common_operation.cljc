(ns sixsq.nuvla.server.resources.spec.common-operation
  "Spec definitions for common operation types used in CIMI resources."
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.core :as cimi-core]
    [spec-tools.core :as st]))


(s/def ::href
  (-> (st/spec ::cimi-core/nonblank-string)
      (assoc :name "href"
             :json-schema/name "href"
             :json-schema/type "string"
             :json-schema/providerMandatory true
             :json-schema/consumerMandatory false
             :json-schema/mutable false
             :json-schema/consumerWritable false

             :json-schema/displayName "href"
             :json-schema/description "URI for operation"
             :json-schema/help "unique URI that identifies the operation"
             :json-schema/hidden false
             :json-schema/sensitive false)))


(s/def ::rel
  (-> (st/spec ::cimi-core/nonblank-string)
      (assoc :name "rel"
             :json-schema/name "rel"
             :json-schema/type "string"
             :json-schema/providerMandatory true
             :json-schema/consumerMandatory false
             :json-schema/mutable false
             :json-schema/consumerWritable false

             :json-schema/displayName "rel"
             :json-schema/description "URL for performing action"
             :json-schema/help "URL for performing action"
             :json-schema/hidden false
             :json-schema/sensitive false)))



