(ns sixsq.nuvla.server.resources.nuvlabox-record-0-lifecycle-test
  (:require
    [clojure.data.json :as json]
    [clojure.string :as str]
    [clojure.test :refer [deftest is use-fixtures]]
    [clojure.tools.logging :as log]
    [peridot.core :refer [content-type header request session]]
    [sixsq.nuvla.server.app.params :as p]
    [sixsq.nuvla.server.middleware.authn-info :refer [authn-info-header]]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.lifecycle-test-utils :as ltu]
    [sixsq.nuvla.server.resources.nuvlabox-record :as nb]))


(use-fixtures :each ltu/with-test-server-fixture)


(def base-uri (str p/service-context nb/resource-type))


(def timestamp "1964-08-25T10:00:00Z")


(def user "jane")


(defn random-mac-address []
  (let [random-str (apply str (remove #((set "-") %) (u/random-uuid)))]
    (->> random-str
         (partition 2)
         (take 6)
         (map #(apply str %))
         (interpose ":")
         (apply str))))


(def valid-nuvlabox {:created          timestamp
                     :updated          timestamp
                     :acl              {:owners   ["group/nuvla-admin" "user/alpha"]
                                        :view-acl ["user/jane"]}

                     :version          0

                     :mac-address      "aa:bb:cc:dd:ee:ff"
                     :owner            {:href "user/test"}
                     :organization     "ACME"
                     :os-version       "OS version"
                     :hw-revision-code "a020d3"
                     :login-username   "aLoginName"
                     :login-password   "aLoginPassword"

                     :form-factor      "Nuvlabox"
                     :vm-cidr          "10.0.0.0/24"
                     :lan-cidr         "10.0.1.0/24"})


(deftest lifecycle
  (let [session (-> (ltu/ring-app)
                    session
                    (content-type "application/json"))
        session-admin (header session authn-info-header "user/super group/nuvla-admin group/nuvla-user group/nuvla-anon")

        session-jane (header session authn-info-header "user/jane group/nuvla-user group/nuvla-anon")
        session-alpha (header session authn-info-header "user/alpha group/nuvla-user group/nuvla-anon")
        session-anon (header session authn-info-header "unknown group/nuvla-anon")]

    ;; admin and deployer collection query should succeed but be empty (no  records created yet)
    (-> session-admin
        (request base-uri)
        (ltu/body->edn)
        (ltu/is-status 200)
        (ltu/is-count zero?)
        (ltu/is-operation-present "add")
        (ltu/is-operation-absent "delete")
        (ltu/is-operation-absent "edit"))

    ;; normal collection query should succeed
    (-> session-jane
        (request base-uri)
        (ltu/body->edn)
        (ltu/is-status 200))

    ;; anonymous collection query should not succeed
    (-> session-anon
        (request base-uri)
        (ltu/body->edn)
        (ltu/is-status 403))

    ;; Admin creation.
    (-> session-admin
        (request base-uri
                 :request-method :post
                 :body (json/write-str (assoc valid-nuvlabox :mac-address (random-mac-address))))
        (ltu/body->edn)
        (ltu/is-status 201))

    (-> session-admin
        (request base-uri)
        (ltu/body->edn)
        (ltu/is-status 200)
        (ltu/is-count 1))

    ;; creating a nuvlabox as a normal user should succeed; both nano and regular
    (-> session-admin
        (request base-uri
                 :request-method :post
                 :body (json/write-str valid-nuvlabox))
        (ltu/body->edn)
        (ltu/is-status 201))

    ;; creating a nuvlabox as a normal user should succeed
    (let [entry (assoc valid-nuvlabox :mac-address "02:02:02:02:02:02")]
      (-> session-admin
          (request base-uri
                   :request-method :post
                   :body (json/write-str entry))
          (ltu/body->edn)
          (ltu/is-status 201)))

    (-> session-admin
        (request base-uri
                 :request-method :post
                 :body (json/write-str (assoc valid-nuvlabox :mac-address "00:00:00:00:00:00")))
        (ltu/body->edn)
        (ltu/is-status 201))

    (-> session-admin
        (request base-uri)
        (ltu/body->edn)
        (ltu/is-status 200)
        (ltu/is-count 4))

    ;; because the identifier is a UUID now, there are no constraints on duplicating the MAC address
    (-> session-admin
        (request base-uri
                 :request-method :post
                 :body (json/write-str (assoc valid-nuvlabox :mac-address "01:02:03:04:05:06")))
        (ltu/body->edn)
        (ltu/is-status 201))

    (-> session-admin
        (request base-uri
                 :request-method :post
                 :body (json/write-str (assoc valid-nuvlabox :mac-address "01:02:03:04:05:06")))
        (ltu/body->edn)
        (ltu/is-status 201))

    ;; create & actions
    (let [resp-admin (-> session-admin
                         (request base-uri
                                  :request-method :post
                                  :body (json/write-str (assoc valid-nuvlabox :mac-address "01:bb:cc:dd:ee:ff")))
                         (ltu/body->edn)
                         (ltu/is-status 201))


          id-nuvlabox (get-in resp-admin [:response :body :resource-id])
          location-admin (str p/service-context (-> resp-admin ltu/location))
          uri-nuvlabox (str p/service-context id-nuvlabox)]

      ;; id is a UUID now, not the MAC address
      (is (not= (str nb/resource-type "/01bbccddeeff") id-nuvlabox))

      (is (= location-admin uri-nuvlabox))

      ;; user should be able to see the resource and recover activation URL
      (let [activate-op (-> session-jane
                            (request uri-nuvlabox)
                            (ltu/body->edn)
                            (ltu/is-status 200)
                            (ltu/is-operation-absent "delete")
                            (ltu/is-operation-absent "edit")
                            (ltu/is-operation-present "activate")
                            (ltu/get-op "activate"))

            activate-url (str p/service-context activate-op)

            ;; anonymous should be able to activate the NuvlaBox and get back an api key and secret to access Nuvla
            credential-id (-> session-anon
                              (request activate-url
                                       :request-method :post)
                              (ltu/body->edn)
                              (ltu/is-status 200)
                              (ltu/is-key-value (comp not str/blank?) :secret-key true)
                              (get-in [:response :body :api-key]))]

        (let [credential-url (str p/service-context credential-id)
              credential-nuvlabox (-> session-admin
                                      (request credential-url)
                                      (ltu/body->edn)
                                      (ltu/is-status 200)
                                      :response
                                      :body)
              nuvlabox-record (-> session-admin
                                  (request uri-nuvlabox)
                                  (ltu/body->edn)
                                  (ltu/is-status 200)
                                  :response
                                  :body)]
          ;; check generated credentials acl and claims.
          (is (= (-> credential-nuvlabox :claims :identity) id-nuvlabox))
          (is (= (-> credential-nuvlabox :claims :roles set) #{id-nuvlabox
                                                               "group/nuvla-user"
                                                               "group/nuvla-anon"}))

          ;; acl of created credential is same as nuvlabox-record acl
          (is (= (:acl credential-nuvlabox) (:acl nuvlabox-record)))))

      ;; user should be able to see the resource
      (-> session-jane
          (request uri-nuvlabox)
          (ltu/body->edn)
          (ltu/is-status 200))

      ;; anonymous should not be able to see the resource
      (-> session-anon
          (request uri-nuvlabox)
          (ltu/body->edn)
          (ltu/is-status 403))

      ;; admin should be able to edit the resource
      (-> session-admin
          (request uri-nuvlabox
                   :request-method :put
                   :body (json/write-str (assoc valid-nuvlabox :comment "just a comment")))
          (ltu/body->edn)
          (ltu/is-status 200))


      ;; verify that the delete behaves correctly

      ;; anonymous users cannot see or delete the nuvlabox-record
      (-> session-anon
          (request uri-nuvlabox
                   :request-method :delete)
          (ltu/body->edn)
          (ltu/is-status 403))

      ;; check that a job to delete nuvlabox resources has been created
      (let [job-id (-> session-alpha
                       (request uri-nuvlabox
                                :request-method :delete)
                       (ltu/body->edn)
                       (ltu/is-status 202)
                       (ltu/location))

            job-url (str p/service-context job-id)

            job (-> session-alpha
                    (request job-url)
                    (ltu/body->edn)
                    (ltu/is-status 200)
                    :response
                    :body)]

        (is (= "delete_nuvlabox" (:action job)))
        (is (= id-nuvlabox (-> job :target-resource :href))))

      ;; check that the state and ACL have been updated on nuvlabox-record
      (let [record (-> session-alpha
                       (request uri-nuvlabox)
                       (ltu/body->edn)
                       (ltu/is-status 200)
                       :response
                       :body)

            acl (:acl record)]

        (is (= "DECOMMISSIONING" (:state record)))
        (is (= ["group/nuvla-admin"] (-> record :acl :owners)))
        (is ((-> acl :view-acl set) "user/alpha"))
        (is ((-> acl :view-data set) "user/alpha"))
        (is ((-> acl :view-meta set) "user/alpha"))

        (let [id-status (:nuvlabox-status record)

              uri-status (str p/service-context id-status)

              status (-> session-alpha
                         (request uri-status)
                         (ltu/body->edn)
                         (ltu/is-status 200)
                         :response
                         :body)

              acl (:acl status)]

          (is (= ["group/nuvla-admin"] (-> record :acl :owners)))
          (is ((-> acl :view-acl set) "user/alpha"))
          (is ((-> acl :view-data set) "user/alpha"))
          (is ((-> acl :view-meta set) "user/alpha")))))))


(deftest bad-methods
  (let [resource-uri (str p/service-context (u/new-resource-id nb/resource-type))]
    (ltu/verify-405-status [[base-uri :options]
                            [base-uri :delete]
                            [resource-uri :options]
                            [resource-uri :post]])))
