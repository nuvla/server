(ns sixsq.nuvla.server.resources.spec.customer
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.common :as common]
    [sixsq.nuvla.server.resources.spec.core :as core]
    [sixsq.nuvla.server.util.spec :as su]
    [spec-tools.core :as st]))


;; FIXME: Make a general macro for identifiers with a fixed prefix.
(def user-id-regex #"^user/[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}$")


(s/def ::parent
  (-> (st/spec (s/and string? #(re-matches user-id-regex %)))
      (assoc :name "parent"
             :json-schema/type "string")))


(s/def ::customer-id
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "customer-id"
             :json-schema/type "string"
             :json-schema/description "external customer id reference"

             :json-schema/order 20)))


(s/def ::schema
  (su/only-keys-maps common/common-attrs
                     {:req-un [::parent
                               ::customer-id]}))
