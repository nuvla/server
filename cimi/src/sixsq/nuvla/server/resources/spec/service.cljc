(ns sixsq.nuvla.server.resources.spec.service
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.common :as cimi-common]
    [sixsq.nuvla.server.resources.spec.core :as cimi-core]
    [sixsq.nuvla.server.util.spec :as su]
    [spec-tools.core :as st]))


(s/def ::type
  (-> (st/spec ::cimi-core/kebab-identifier)
      (assoc :name "type"
             :json-schema/name "type"
             :json-schema/type "string"
             :json-schema/providerMandatory true
             :json-schema/consumerMandatory true
             :json-schema/mutable true
             :json-schema/consumerWritable true

             :json-schema/displayName "service type"
             :json-schema/description "kebab-case identifier for the service type"
             :json-schema/help "kebab-case identifier for the service type"
             :json-schema/group "body"
             :json-schema/order 20
             :json-schema/hidden false
             :json-schema/sensitive false)))


(s/def ::endpoint
  (-> (st/spec ::cimi-core/nonblank-string)
      (assoc :name "endpoint"
             :json-schema/name "endpoint"
             :json-schema/type "string"
             :json-schema/providerMandatory true
             :json-schema/consumerMandatory true
             :json-schema/mutable true
             :json-schema/consumerWritable true

             :json-schema/displayName "endpoint"
             :json-schema/description "public API endpoint for the service"
             :json-schema/help "public endpoint from where the service API is accessible"
             :json-schema/group "body"
             :json-schema/order 21
             :json-schema/hidden false
             :json-schema/sensitive false)))


(s/def ::accessible
  (-> (st/spec boolean?)
      (assoc :name "accessible"
             :json-schema/name "accessible"
             :json-schema/type "string"
             :json-schema/providerMandatory true
             :json-schema/consumerMandatory false
             :json-schema/mutable true
             :json-schema/consumerWritable true

             :json-schema/displayName "accessible"
             :json-schema/description "flag to indicate if the service is accessible"
             :json-schema/help "flag to indicate if the service is accessible"
             :json-schema/group "body"
             :json-schema/order 22
             :json-schema/hidden false
             :json-schema/sensitive false

             :json-schema/value-scope {:default true})))


(s/def ::schema
  (su/only-keys-maps cimi-common/common-attrs
                     {:req-un [::type
                               ::endpoint
                               ::accessible]}))
