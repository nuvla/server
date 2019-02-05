(ns com.sixsq.slipstream.ssclj.resources.connector-template
  (:require
    [clojure.tools.logging :as log]
    [com.sixsq.slipstream.auth.acl :as a]
    [com.sixsq.slipstream.ssclj.resources.common.crud :as crud]
    [com.sixsq.slipstream.ssclj.resources.common.schema :as c]
    [com.sixsq.slipstream.ssclj.resources.common.utils :as u]
    [com.sixsq.slipstream.ssclj.resources.spec.connector-template]
    [com.sixsq.slipstream.util.response :as r]))

(def ^:const resource-tag :connectorTemplates)

(def ^:const resource-name "ConnectorTemplate")

(def ^:const resource-url (u/de-camelcase resource-name))

(def ^:const collection-name "ConnectorTemplateCollection")

(def ^:const resource-uri (str c/slipstream-schema-uri resource-name))

(def ^:const collection-uri (str c/slipstream-schema-uri collection-name))

(def user-can-view {:principal "USER"
                    :type      "ROLE"
                    :right     "VIEW"})

(def resource-acl {:owner {:principal "ADMIN"
                           :type      "ROLE"}
                   :rules [{:principal "ADMIN"
                            :type      "ROLE"
                            :right     "VIEW"}
                           user-can-view]})

(def collection-acl {:owner {:principal "ADMIN"
                             :type      "ROLE"}
                     :rules [{:principal "ADMIN"
                              :type      "ROLE"
                              :right     "VIEW"}
                             user-can-view]})

;;
;; Resource defaults
;;

(def connector-instance-name-default
  {:instanceName "Provide valid connector instance name."})

(def connector-mandatory-reference-attrs-defaults
  {:orchestratorImageid ""
   :quotaVm             "20"
   :maxIaasWorkers      5})

(def connector-reference-attrs-defaults
  {:endpoint                ""
   :objectStoreEndpoint     ""
   :nativeContextualization "linux-only"
   :orchestratorSSHUsername ""
   :orchestratorSSHPassword ""
   :securityGroups          "slipstream_managed"
   :updateClientURL         ""
   })

;;
;; atom to keep track of the loaded ConnectorTemplate resources
;;
(def templates (atom {}))
(def name->kw (atom {}))

(defn collection-wrapper-fn
  "Specialized version of this function that removes the adding
   of operations to the collection and entries.  These are already
   part of the stored resources."
  [resource-name collection-acl collection-uri collection-key]
  (fn [_ entries]
    (let [skeleton {:acl         collection-acl
                    :resourceURI collection-uri
                    :id          (u/de-camelcase resource-name)}]
      (assoc skeleton collection-key entries))))

(defn complete-resource
  "Completes the given document with server-managed information:
   resourceURI, timestamps, operations, and ACL."
  [{:keys [cloudServiceType] :as resource}]
  (when cloudServiceType
    (let [id (str resource-url "/" cloudServiceType)]
      (-> resource
          (merge {:id          id
                  :resourceURI resource-uri
                  :acl         resource-acl})
          (merge connector-mandatory-reference-attrs-defaults)
          (merge connector-instance-name-default)
          u/update-timestamps))))

(defn register
  "Registers a given ConnectorTemplate resource with the server.
   The resource document (resource) must be valid.
   The key will be used to create the id of
   the resource as 'connector-template/key'."
  [resource & [name-kw-map]]
  (when-let [full-resource (complete-resource resource)]
    (let [id (:id full-resource)]
      (swap! templates assoc id full-resource)
      (log/info "loaded ConnectorTemplate" id)
      (when name-kw-map
        (swap! name->kw assoc id name-kw-map)
        (log/info "added name->kw mapping from ConnectorTemplate" id)))))


;;
;; multimethods for validation
;;

(defmulti validate-subtype
          "Validates the given resource against the specific
           ConnectorTemplate subtype schema."
          :cloudServiceType)

(defmethod validate-subtype :default
  [resource]
  (throw (ex-info (str "unknown ConnectorTemplate type: " (:cloudServiceType resource)) resource)))

(defmethod crud/validate
  resource-uri
  [resource]
  (validate-subtype resource))

;;
;; CRUD operations
;;

(defmethod crud/add resource-name
  [request]
  (throw (r/ex-bad-method request)))

(defmethod crud/retrieve resource-name
  [{{uuid :uuid} :params :as request}]
  (try
    (let [id (str resource-url "/" uuid)]
      (-> (get @templates id)
          (a/can-view? request)
          (r/json-response)))
    (catch Exception e
      (or (ex-data e) (throw e)))))

;; must override the default implementation so that the
;; data can be pulled from the atom rather than the database
(defmethod crud/retrieve-by-id resource-url
  [id]
  (try
    (get @templates id)
    (catch Exception e
      (or (ex-data e) (throw e)))))

(defmethod crud/edit resource-name
  [request]
  (throw (r/ex-bad-method request)))

(defmethod crud/delete resource-name
  [request]
  (throw (r/ex-bad-method request)))

(defmethod crud/query resource-name
  [request]
  (a/can-view? {:acl collection-acl} request)
  (let [wrapper-fn (collection-wrapper-fn resource-name collection-acl collection-uri resource-tag)
        ;; FIXME: At least the paging options should be supported.
        options (select-keys request [:identity :query-params :cimi-params :user-name :user-roles])
        [count-before-pagination entries] ((juxt count vals) @templates)
        wrapped-entries (wrapper-fn request entries)
        entries-and-count (assoc wrapped-entries :count count-before-pagination)]
    (r/json-response entries-and-count)))
