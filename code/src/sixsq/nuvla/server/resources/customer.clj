(ns sixsq.nuvla.server.resources.customer
  "
Customer mapping to external banking system."
  (:require
    [clojure.string :as str]
    [sixsq.nuvla.auth.acl-resource :as a]
    [sixsq.nuvla.auth.utils :as auth]
    [sixsq.nuvla.server.resources.common.crud :as crud]
    [sixsq.nuvla.server.resources.common.std-crud :as std-crud]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.spec.customer :as customer]
    [sixsq.nuvla.server.resources.spec.customer-related :as customer-related]
    [sixsq.nuvla.server.resources.customer.utils :as utils]
    [sixsq.nuvla.server.resources.pricing.stripe :as stripe]
    [sixsq.nuvla.server.resources.resource-metadata :as md]
    [sixsq.nuvla.server.util.metadata :as gen-md]
    [sixsq.nuvla.server.resources.configuration-nuvla :as config-nuvla]
    [sixsq.nuvla.server.util.response :as r]
    [clojure.tools.logging :as log]))


(def ^:const resource-type (u/ns->type *ns*))


(def ^:const collection-type (u/ns->collection-type *ns*))


(def collection-acl {:query ["group/nuvla-user"]
                     :add   ["group/nuvla-user"]})

;;
;; validate customer
;;

(def validate-fn (u/create-spec-validation-fn ::customer/schema))


(def validate-customer-body (utils/throw-invalid-body-fn ::customer-related/customer))


(defmethod crud/validate resource-type
  [resource]
  resource)

;;
;; multimethod for ACLs
;;

(defmethod crud/add-acl resource-type
  [{:keys [parent] :as resource} request]
  (assoc resource :acl {:owners   ["group/nuvla-admin"]
                        :view-acl [parent]
                        :manage   [parent]}))

;;
;; "Implementations" of multimethod declared in crud namespace
;;

(defn user-id->resource-id
  [user-id]
  (->> user-id
       u/parse-id
       (str/join "-")
       (str resource-type "/")))

(defn request->resource-id
  [{{uuid :uuid} :params :as request}]
  (str resource-type "/" uuid))


;; resource identifier a UUID generated from the user-id
(defmethod crud/new-identifier resource-type
  [resource resource-name]
  (assoc resource :id (-> resource :parent user-id->resource-id)))


;;
;; CRUD operations
;;

(def add-impl (std-crud/add-fn resource-type collection-acl resource-type))


(defn add-customer
  [{customer :body :as request} user-id]
  (config-nuvla/throw-stripe-not-configured)
  (utils/throw-customer-exist (user-id->resource-id user-id))
  (validate-customer-body customer)
  (let [customer-id (utils/create-customer customer user-id)]
    (-> request
        (assoc :body {:parent      user-id
                      :customer-id customer-id})
        add-impl)))


(defmethod crud/add resource-type
  [request]
  (a/throw-cannot-add collection-acl request)
  (utils/throw-admin-can-not-be-customer request)
  (add-customer request (auth/current-user-id request)))


(def retrieve-impl (std-crud/retrieve-fn resource-type))


(defmethod crud/retrieve resource-type
  [request]
  (config-nuvla/throw-stripe-not-configured)
  (retrieve-impl request))


(def query-impl (std-crud/query-fn resource-type collection-acl collection-type))


(defmethod crud/query resource-type
  [request]
  (config-nuvla/throw-stripe-not-configured)
  (query-impl request))


(defmethod crud/set-operations resource-type
  [{:keys [id] :as resource} request]
  (let [can-manage?                   (a/can-manage? resource request)
        get-subscription-op           (u/action-map id utils/get-subscription-action)
        create-subscription-op        (u/action-map id utils/create-subscription-action)
        create-setup-intent-op        (u/action-map id utils/create-setup-intent-action)
        list-payment-methods-op       (u/action-map id utils/list-payment-methods-action)
        detach-payment-method-op      (u/action-map id utils/detach-payment-method-action)
        set-default-payment-method-op (u/action-map id utils/set-default-payment-method-action)
        upcoming-invoice-op           (u/action-map id utils/upcoming-invoice-action)
        list-invoices-op              (u/action-map id utils/list-invoices-action)]
    (cond-> (crud/set-standard-operations resource request)

            can-manage? (update :operations concat [get-subscription-op
                                                    create-subscription-op
                                                    create-setup-intent-op
                                                    list-payment-methods-op
                                                    set-default-payment-method-op
                                                    detach-payment-method-op
                                                    upcoming-invoice-op
                                                    list-invoices-op]))))


(defmethod crud/do-action [resource-type utils/get-subscription-action]
  [request]
  (config-nuvla/throw-stripe-not-configured)
  (try
    (some-> request
            (request->resource-id)
            (crud/retrieve-by-id-as-admin)
            (a/throw-cannot-manage request)
            :customer-id
            stripe/retrieve-customer
            utils/get-current-subscription
            utils/s-subscription->map
            r/json-response)
    (catch Exception e
      (or (ex-data e) (throw e)))))


(defmethod crud/do-action [resource-type utils/create-subscription-action]
  [{body :body :as request}]
  (config-nuvla/throw-stripe-not-configured)
  (try
    (-> request
        (request->resource-id)
        (crud/retrieve-by-id-as-admin)
        (a/throw-cannot-manage request)
        (utils/throw-plan-id-mandatory request)
        (utils/throw-subscription-already-exist request)
        :customer-id
        (utils/create-subscription body)
        r/json-response)
    (catch Exception e
      (or (ex-data e) (throw e)))))


(defmethod crud/do-action [resource-type utils/create-setup-intent-action]
  [request]
  (config-nuvla/throw-stripe-not-configured)
  (try
    (-> request
        (request->resource-id)
        (crud/retrieve-by-id-as-admin)
        (a/throw-cannot-manage request)
        :customer-id
        utils/create-setup-intent
        r/json-response)
    (catch Exception e
      (or (ex-data e) (throw e)))))


(defmethod crud/do-action [resource-type utils/list-payment-methods-action]
  [request]
  (config-nuvla/throw-stripe-not-configured)
  (try
    (-> request
        (request->resource-id)
        (crud/retrieve-by-id-as-admin)
        (a/throw-cannot-manage request)
        :customer-id
        stripe/retrieve-customer
        (utils/list-payment-methods)
        r/json-response)
    (catch Exception e
      (or (ex-data e) (throw e)))))


(defmethod crud/do-action [resource-type utils/detach-payment-method-action]
  [{{:keys [payment-method]} :body :as request}]
  (config-nuvla/throw-stripe-not-configured)
  (let [{:keys [id] :as resource} (-> request
                                      (request->resource-id)
                                      (crud/retrieve-by-id-as-admin)
                                      (a/throw-cannot-manage request))]
    (try
      (some-> payment-method
              stripe/retrieve-payment-method
              stripe/detach-payment-method)
      (r/map-response (format "%s successfully detached" payment-method) 200 id)
      (catch Exception e
        (or (ex-data e) (throw e))))))


(defmethod crud/do-action [resource-type utils/set-default-payment-method-action]
  [{{:keys [payment-method]} :body :as request}]
  (config-nuvla/throw-stripe-not-configured)
  (let [{:keys [id customer-id] :as resource} (-> request
                                                  (request->resource-id)
                                                  (crud/retrieve-by-id-as-admin)
                                                  (a/throw-cannot-manage request))]
    (try
      (-> customer-id
          stripe/retrieve-customer
          (stripe/update-customer {"invoice_settings" {"default_payment_method" payment-method}}))
      (r/map-response (format "%s successfully set as default" payment-method) 200 id)
      (catch Exception e
        (or (ex-data e) (throw e))))))


(defmethod crud/do-action [resource-type utils/upcoming-invoice-action]
  [request]
  (config-nuvla/throw-stripe-not-configured)
  (try
    (-> request
        (request->resource-id)
        (crud/retrieve-by-id-as-admin)
        (a/throw-cannot-manage request)
        :customer-id
        (utils/get-upcoming-invoice)
        r/json-response)
    (catch Exception e
      (or (ex-data e) (throw e)))))


(defmethod crud/do-action [resource-type utils/list-invoices-action]
  [request]
  (config-nuvla/throw-stripe-not-configured)
  (try
    (-> request
        (request->resource-id)
        (crud/retrieve-by-id-as-admin)
        (a/throw-cannot-manage request)
        :customer-id
        (utils/list-invoices)
        r/json-response)
    (catch Exception e
      (or (ex-data e) (throw e)))))


(def delete-impl (std-crud/delete-fn resource-type))

(defmethod crud/delete resource-type
  [request]
  (delete-impl request))


;;
;; initialization
;;

(def resource-metadata (gen-md/generate-metadata ::ns ::customer/schema))

(defn initialize
  []
  (std-crud/initialize resource-type ::customer/schema)
  (md/register resource-metadata))


;; ==== Move add to user operation create-customer
;; when stripe configured, user is signed-up but not customer. Create customer op available (payment-method optional)
;; when new user and stripe configured create user and customer. (payment-method optional)
;; delete user what about customer and subscription. Delete user also used in post-add exception