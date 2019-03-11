(ns sixsq.nuvla.server.resources.spec.resource-metadata
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.common :as cimi-common]
    [sixsq.nuvla.server.resources.spec.core :as cimi-core]
    [sixsq.nuvla.server.resources.spec.resource-metadata-action :as action]
    [sixsq.nuvla.server.resources.spec.resource-metadata-attribute :as attribute]
    [sixsq.nuvla.server.resources.spec.resource-metadata-capability :as capability]
    [sixsq.nuvla.server.resources.spec.resource-metadata-value-scope :as value-scope]
    [sixsq.nuvla.server.util.spec :as su]))


(s/def ::typeURI ::cimi-core/uri)


(s/def ::name ::cimi-core/resource-name)


(s/def ::resource-metadata
  (su/only-keys-maps cimi-common/common-attrs
                     {:req-un [::typeURI]
                      :opt-un [::attribute/attributes
                               ::value-scope/vscope
                               ::capability/capabilities
                               ::action/actions]}))