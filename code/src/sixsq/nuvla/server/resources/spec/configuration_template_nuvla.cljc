(ns sixsq.nuvla.server.resources.spec.configuration-template-nuvla
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.configuration-template :as ps]
    [sixsq.nuvla.server.resources.spec.core :as core]
    [sixsq.nuvla.server.util.spec :as su]
    [spec-tools.core :as st]))


(s/def ::smtp-username
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "smtp-username"
             :json-schema/display-name "SMTP username"
             :json-schema/description "SMTP username for sending email from server"

             :json-schema/order 20)))


(s/def ::smtp-password
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "smtp-password"
             :json-schema/display-name "SMTP password"
             :json-schema/description "SMTP password for sending email from server"

             :json-schema/order 21
             :json-schema/sensitive true)))


(s/def ::smtp-host
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "smtp-host"
             :json-schema/display-name "SMTP host"
             :json-schema/description "SMTP host for sending email from server"

             :json-schema/order 22)))


(s/def ::smtp-port
  (-> (st/spec ::core/port)
      (assoc :name "smtp-port"
             :json-schema/display-name "SMTP port"
             :json-schema/description "SMTP port for sending email from server"

             :json-schema/order 23)))


(s/def ::smtp-ssl
  (-> (st/spec boolean?)
      (assoc :name "smtp-ssl"
             :json-schema/type "boolean"
             :json-schema/display-name "SMTP SSL?"
             :json-schema/description "use SSL when interacting with SMTP server?"

             :json-schema/order 24)))


(s/def ::smtp-debug
  (-> (st/spec boolean?)
      (assoc :name "smtp-debug"
             :json-schema/type "boolean"
             :json-schema/display-name "debug SMTP?"
             :json-schema/description "turn on debugging when interacting with SMTP server?"

             :json-schema/order 25)))


(s/def ::support-email
  (-> (st/spec ::core/email)
      (assoc :name "support-email"
             :json-schema/display-name "support email"
             :json-schema/description "email address for support"

             :json-schema/order 26)))


(s/def ::stripe-api-key
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "stripe-api-key"
             :json-schema/display-name "stripe api key"
             :json-schema/description "stripe private api-key to communicate with the api"

             :json-schema/order 27)))


(s/def ::stripe-client-id
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "stripe-client-id"
             :json-schema/display-name "stripe client id"
             :json-schema/description "stripe client-id to create connected accounts"

             :json-schema/order 28)))


(s/def ::conditions-url
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "conditions-url"
             :json-schema/display-name "Terms & conditions url"
             :json-schema/description "Terms & conditions url"

             :json-schema/order 29)))


(def configuration-template-keys-spec
  {:opt-un [::smtp-username
            ::smtp-password
            ::smtp-host
            ::smtp-port
            ::smtp-ssl
            ::smtp-debug
            ::support-email
            ::stripe-api-key
            ::stripe-client-id
            ::conditions-url]})


;; Defines the contents of the nuvla configuration-template resource itself.
(s/def ::schema
  (su/only-keys-maps ps/resource-keys-spec
                     configuration-template-keys-spec))


;; Defines the contents of the nuvla template key used in a create resource.
(s/def ::template
  (-> (st/spec (su/only-keys-maps ps/template-keys-spec
                                  configuration-template-keys-spec))
      (assoc :name "template"
             :json-schema/type "map")))


(s/def ::schema-create
  (su/only-keys-maps ps/create-keys-spec
                     {:opt-un [::template]}))
