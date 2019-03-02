(ns sixsq.nuvla.server.resources.spec.credential-template-service-aws
    (:require
      [clojure.spec.alpha :as s]
      [sixsq.nuvla.server.resources.spec.acl :as cimi-acl]
      [sixsq.nuvla.server.resources.spec.common :as cimi-common]
      [sixsq.nuvla.server.resources.spec.common-namespaces :as common-ns]
      [sixsq.nuvla.server.resources.spec.credential-template-infrastructure-service :as ct-infra-service]
      [sixsq.nuvla.server.resources.spec.credential-template :as ct]
      [sixsq.nuvla.server.util.spec :as su]
      [spec-tools.core :as st]))



(s/def ::amazonec2-access-key
  (-> (st/spec string?)                                     ;; ::cimi-core/nonblank-string
      (assoc :name "amazonec2-access-key"
             :json-schema/name "amazonec2-access-key"
             :json-schema/namespace common-ns/slipstream-namespace
             :json-schema/uri common-ns/slipstream-uri
             :json-schema/type "string"
             :json-schema/providerMandatory true
             :json-schema/consumerMandatory true
             :json-schema/mutable true
             :json-schema/consumerWritable true

             :json-schema/displayName "amazonec2-access-key"
             :json-schema/description "AWS API key"
             :json-schema/help "AWS API key"
             :json-schema/group "body"
             :json-schema/order 20
             :json-schema/hidden false
             :json-schema/sensitive false)))


(s/def ::amazonec2-secret-key
  (-> (st/spec string?)                                     ;; ::cimi-core/nonblank-string
      (assoc :name "amazonec2-secret-key"
             :json-schema/name "amazonec2-secret-key"
             :json-schema/namespace common-ns/slipstream-namespace
             :json-schema/uri common-ns/slipstream-uri
             :json-schema/type "string"
             :json-schema/providerMandatory true
             :json-schema/consumerMandatory true
             :json-schema/mutable true
             :json-schema/consumerWritable true

             :json-schema/displayName "amazonec2-secret-key"
             :json-schema/description "AWS secret API key"
             :json-schema/help "AWS secret API key"
             :json-schema/group "body"
             :json-schema/order 21
             :json-schema/hidden false
             :json-schema/sensitive true)))


(def credential-template-keys-spec
  {:req-un [::amazonec2-access-key
            ::amazonec2-secret-key]})

(def credential-template-create-keys-spec
  {:req-un [::amazonec2-access-key
            ::amazonec2-secret-key]})

;; Defines the contents of the api-key CredentialTemplate resource itself.
(s/def ::schema
  (su/only-keys-maps ct/resource-keys-spec
                     ct-infra-service/credential-template-service-keys-spec
                     credential-template-keys-spec))

;; Defines the contents of the api-key template used in a create resource.
(s/def ::template
  (su/only-keys-maps ct/template-keys-spec
                     ct-infra-service/credential-template-service-create-keys-spec
                     credential-template-create-keys-spec))

(s/def ::schema-create
  (su/only-keys-maps ct/create-keys-spec
                     {:req-un [::template]}))