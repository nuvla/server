(ns sixsq.nuvla.server.resources.spec.credential-template-infrastructure-service-amazonec2
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.credential-template :as ct]
    [sixsq.nuvla.server.util.spec :as su]
    [spec-tools.core :as st]))


(s/def ::amazonec2-access-key
  (-> (st/spec string?)                                     ;; ::cimi-core/nonblank-string
      (assoc :name "amazonec2-access-key"
             :json-schema/type "string"
             :json-schema/description "AWS API key"
             :json-schema/order 20)))


(s/def ::amazonec2-secret-key
  (-> (st/spec string?)                                     ;; ::cimi-core/nonblank-string
      (assoc :name "amazonec2-secret-key"
             :json-schema/type "string"
             :json-schema/description "AWS secret API key"
             :json-schema/order 21
             :json-schema/sensitive true)))


(def credential-template-keys-spec
  {:req-un [::amazonec2-access-key
            ::amazonec2-secret-key]})


(def credential-template-create-keys-spec credential-template-keys-spec)


;; Defines the contents of the amazonec2 CredentialTemplate resource itself.
(s/def ::schema
  (su/only-keys-maps ct/resource-keys-spec
                     credential-template-keys-spec))


;; Defines the contents of the amazonec2 cred template used in a create resource.
(s/def ::template
  (-> (st/spec (su/only-keys-maps ct/template-keys-spec
                                  credential-template-create-keys-spec))
      (assoc :name "template"
             :json-schema/type "map")))


(s/def ::schema-create
  (su/only-keys-maps ct/create-keys-spec
                     {:req-un [::template]}))
