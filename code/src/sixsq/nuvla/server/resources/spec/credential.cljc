(ns sixsq.nuvla.server.resources.spec.credential
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.common :as c]
    [sixsq.nuvla.server.resources.spec.credential-template :as ct]
    [sixsq.nuvla.server.util.spec :as su]))


(s/def ::type ::ct/type)


(s/def ::method ::ct/method)


(def credential-keys-spec (su/merge-keys-specs [c/common-attrs
                                                {:req-un [::type
                                                          ::method]}]))