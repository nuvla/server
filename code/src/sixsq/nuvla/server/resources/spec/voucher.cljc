(ns sixsq.nuvla.server.resources.spec.voucher
  (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.common :as common]
    [sixsq.nuvla.server.resources.spec.core :as core]
    [sixsq.nuvla.server.resources.spec.user :as user]
    [sixsq.nuvla.server.util.spec :as su]
    [spec-tools.core :as st]))


(s/def ::owner (-> (st/spec ::user/id)
                   (assoc :name "owner"
                          :json-schema/type "resource-id"
                          :json-schema/description "reference to owner user id"

                          :json-schema/section "meta"
                          :json-schema/group "body"
                          :json-schema/order 18)))

(s/def ::user (-> (st/spec ::user/id)
                  (assoc :name "user"
                         :json-schema/type "resource-id"
                         :json-schema/description "reference to associated user id"

                         :json-schema/section "meta"
                         :json-schema/group "body"
                         :json-schema/order 19)))

(s/def ::amount
  (-> (st/spec (s/and number? #(> % 0)))
      (assoc :name "amount"
             :json-schema/type "double"

             :json-schema/description "amount of the voucher"
             :json-schema/group "body"
             :json-schema/order 20)))


(s/def ::currency
  (-> (st/spec #{"EUR", "CHF", "USD"})
      (assoc :name "currency"
             :json-schema/type "string"

             :json-schema/description "currency for the voucher amount"
             :json-schema/group "body"
             :json-schema/order 21)))


(s/def ::expiry
  (-> (st/spec ::core/timestamp)
      (assoc :name "expiry"
             :json-schema/type "date-time"

             :json-schema/description "when the voucher expires"
             :json-schema/group "body"
             :json-schema/order 22)))


(s/def ::state
  (-> (st/spec #{"NEW", "ACTIVATED", "DISTRIBUTED",
                 "EXPIRED", "REDEEMED"})
      (assoc :name "state"
             :json-schema/type "string"

             :json-schema/description "usage state of the voucher"
             :json-schema/group "body"
             :json-schema/order 23

             :json-schema/value-scope {:values  ["NEW", "DISTRIBUTED", "ACTIVATED",
                                                 "EXPIRED", "REDEEMED"]
                                       :default "NEW"})))


(s/def ::service-info-url
  (-> (st/spec ::core/url)
      (assoc :name "service-info-url"
             :json-schema/type "string"

             :json-schema/description "URL for the service provider who issued the voucher"
             :json-schema/group "body"
             :json-schema/order 24)))


(s/def ::code
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "code"
             :json-schema/type "string"

             :json-schema/description "voucher code"
             :json-schema/group "body"
             :json-schema/order 25)))


(s/def ::activated
  (-> (st/spec ::core/timestamp)
      (assoc :name "activated"
             :json-schema/type "date-time"

             :json-schema/description "when the voucher was activated"
             :json-schema/group "body"
             :json-schema/order 26)))


(s/def ::redeemed
  (-> (st/spec ::core/timestamp)
      (assoc :name "redeemed"
             :json-schema/type "date-time"

             :json-schema/description "when the voucher was redeemed"
             :json-schema/group "body"
             :json-schema/order 27)))


(s/def ::batch
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "batch"
             :json-schema/type "string"

             :json-schema/description "reference for the batch this voucher belongs to"
             :json-schema/group "body"
             :json-schema/order 29)))


(s/def ::wave
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "wave"
             :json-schema/type "string"

             :json-schema/description "optional reference for the wave when the voucher was issued"
             :json-schema/group "body"
             :json-schema/order 30)))


(s/def ::supplier
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "supplier"
             :json-schema/type "string"

             :json-schema/description "name identifier for the voucher supplier"
             :json-schema/group "body"
             :json-schema/order 31)))


(s/def ::platform
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "platform"
             :json-schema/type "string"

             :json-schema/description "name identifier for the platform where the voucher is to be used"
             :json-schema/group "body"
             :json-schema/order 32)))


(s/def ::distributor
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "distributor"
             :json-schema/type "string"

             :json-schema/description "name identifier for the voucher distributor"
             :json-schema/group "body"
             :json-schema/order 33)))


(s/def ::discipline
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "discipline"
             :json-schema/type "string"

             :json-schema/description "name of the voucher discipline"
             :json-schema/group "body"
             :json-schema/order 34)))


(s/def ::institute
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "institute"
             :json-schema/type "string"

             :json-schema/description "name of the institute using the voucher"
             :json-schema/group "body"
             :json-schema/order 35)))


(s/def ::researcher-name
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "researcher-name"
             :json-schema/type "string"

             :json-schema/description "name of the researcher using the voucher"
             :json-schema/group "body"
             :json-schema/order 36)))


(s/def ::researcher-email
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "researcher-email"
             :json-schema/type "string"

             :json-schema/description "email of the researcher using the voucher"
             :json-schema/group "body"
             :json-schema/order 37)))


(s/def ::country
  (-> (st/spec #{
                 "AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM",
                 "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ",
                 "BM", "BT", "BO", "BQ", "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF",
                 "BI", "CV", "KH", "CM", "CA", "KY", "CF", "TD", "CL", "CN", "CX", "CC",
                 "CO", "KM", "CG", "CD", "CK", "CR", "CI", "HR", "CU", "CW", "CY", "CZ",
                 "DK", "DJ", "DM", "DO", "EC", "EG", "SV", "GQ", "ER", "EE", "SZ", "ET",
                 "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE",
                 "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY",
                 "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE",
                 "IM", "IL", "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR",
                 "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MO",
                 "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX",
                 "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP",
                 "NL", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MK", "MP", "NO", "OM",
                 "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR",
                 "QA", "RE", "RO", "RU", "RW", "BL", "SH", "KN", "LC", "MF", "PM", "VC",
                 "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG", "SX", "SK", "SI",
                 "SB", "SO", "ZA", "GS", "SS", "ES", "LK", "SD", "SR", "SJ", "SE", "CH",
                 "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR",
                 "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY", "UZ", "VU",
                 "VE", "VN", "VG", "VI", "WF", "EH", "YE", "ZM", "ZW"
                 })
      (assoc :name "country"
             :json-schema/type "string"

             :json-schema/description "2-letter country code"
             :json-schema/group "body"
             :json-schema/order 38)))


(s/def ::country-name
  (-> (st/spec ::core/nonblank-string)
    (assoc :name "country name"
           :json-schema/type "string"

           :json-schema/description "country name"
           :json-schema/group "body"
           :json-schema/order 39)))

;;
;; -------
;;

(s/def ::schema
  (su/only-keys-maps common/common-attrs
                     {:req-un [::owner
                               ::amount
                               ::currency
                               ::code
                               ::state
                               ::supplier]
                      :opt-un [::expiry
                               ::discipline
                               ::platform
                               ::institute
                               ::researcher-email
                               ::researcher-name
                               ::country
                               ::country-name
                               ::distributor
                               ::activated
                               ::service-info-url
                               ::user
                               ::redeemed
                               ::wave
                               ::batch]}))