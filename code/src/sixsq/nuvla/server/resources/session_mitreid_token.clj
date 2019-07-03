(ns sixsq.nuvla.server.resources.session-mitreid-token
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [sixsq.nuvla.auth.cookies :as cookies]
    [sixsq.nuvla.auth.external :as ex]
    [sixsq.nuvla.auth.internal :as auth-internal]
    [sixsq.nuvla.auth.utils.sign :as sign]
    [sixsq.nuvla.auth.utils.timestamp :as ts]
    [sixsq.nuvla.server.resources.common.std-crud :as std-crud]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.session :as p]
    [sixsq.nuvla.server.resources.session-oidc.utils :as oidc-utils]
    [sixsq.nuvla.server.resources.session.utils :as sutils]
    [sixsq.nuvla.server.resources.spec.session :as session]
    [sixsq.nuvla.server.resources.spec.session-template-mitreid-token :as st-mitreid-token]
    [sixsq.nuvla.server.middleware.authn-info :as authn-info]))


(def ^:const authn-method "mitreid-token")


;;
;; multimethods for validation
;;

(def validate-fn (u/create-spec-validation-fn ::session/session))


(defmethod p/validate-subtype authn-method
  [resource]
  (validate-fn resource))


(def create-validate-fn (u/create-spec-validation-fn ::st-mitreid-token/schema-create))


(defmethod p/create-validate-subtype authn-method
  [resource]
  (create-validate-fn resource))


;;
;; transform template into session resource
;;

(defmethod p/tpl->session authn-method
  [{:keys [token instance href redirectURI] :as resource} {:keys [headers] :as request}]
  (if token
    (let [{:keys [clientIPs]} (oidc-utils/config-mitreid-token-params redirectURI instance)
          {:keys [publicKey]} (oidc-utils/config-mitreid-params redirectURI instance)]
      (try
        (let [{:keys [sub] :as claims} (sign/unsign-cookie-info token publicKey)
              roles (concat (oidc-utils/extract-roles claims)
                            (oidc-utils/extract-groups claims)
                            (oidc-utils/extract-entitlements claims))]
          (log/debug "MITREid token authentication claims for" instance ":" (pr-str claims))
          (if sub
            (if-let [matched-user (ex/match-oidc-username :mitreid sub instance)]
              (let [session-info {:href href, :username matched-user, :redirectURI redirectURI}
                    ;; FIXME: Use correct values for username and user-id!
                    {:keys [id clientIP] :as session} (sutils/create-session "username" "user-id" session-info headers authn-method)
                    claims (cond-> (auth-internal/create-claims matched-user)
                                   id (assoc :session id)
                                   id (update :roles #(str id " " %))
                                   roles (update :roles #(str % " " (str/join " " roles))))
                    cookie (cookies/create-cookie claims)
                    expires (ts/rfc822->iso8601 (:expires cookie))
                    claims-roles (:roles claims)
                    session (cond-> (assoc session :expiry expires)
                                    claims-roles (assoc :roles claims-roles))]

                ;; only validate the client IP address, if the parameter is set
                (when clientIPs
                  (when-not ((set clientIPs) clientIP)
                    (oidc-utils/throw-invalid-address clientIP redirectURI)))

                (log/debug "MITREid cookie token claims for" (u/id->uuid href) ":" (pr-str claims))
                (let [cookies {authn-info/authn-cookie cookie}]
                  (if redirectURI
                    [{:status 303, :headers {"Location" redirectURI}, :cookies cookies} session]
                    [{:cookies cookies} session])))
              (oidc-utils/throw-inactive-user sub nil))
            (oidc-utils/throw-no-subject nil)))
        (catch Exception e
          (oidc-utils/throw-invalid-access-code (str e) nil))))
    (oidc-utils/throw-no-access-token nil)))


;;
;; initialization: no schema for this parent resource
;;

(defn initialize
  []
  (std-crud/initialize p/resource-type ::session/session))
