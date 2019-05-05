(ns sixsq.nuvla.server.resources.spec.nuvlabox-state-test
  (:require
    [clojure.test :refer [are deftest]]
    [sixsq.nuvla.server.resources.nuvlabox-state :as nbs]
    [sixsq.nuvla.server.resources.spec.nuvlabox-state :as nuvlabox-state]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]))


(def valid-acl {:owners ["group/nuvla-admin"]})


(def timestamp "1964-08-25T10:00:00Z")


(def state {:id             (str nbs/resource-type "/uuid")
            :resource-type  nbs/resource-type
            :created        timestamp
            :updated        timestamp

            :acl            valid-acl

            :parent         "nuvlabox-resource/uuid"
            :state          "ONLINE"

            :next-heartbeat timestamp

            :resources      {:cpu   {:capacity 8
                                     :load     4.5}
                             :ram   {:capacity 4096
                                     :used     1000}
                             :disks [{:device   "root"
                                      :capacity 20000
                                      :used     10000}
                                     {:device   "datastore"
                                      :capacity 20000
                                      :used     10000}]}

            :peripherals    {:usb [{:busy        false
                                    :vendor-id   "vendor-id"
                                    :device-id   "device-id"
                                    :bus-id      "bus-id"
                                    :product-id  "product-id"
                                    :description "description"}]}

            :wifi-password  "some-secure-password"})


(deftest check-nuvlabox-state

  (stu/is-valid ::nuvlabox-state/schema state)
  (stu/is-invalid ::nuvlabox-state/schema (assoc state :bad-attr "BAD_ATTR"))

  ;; required
  (doseq [attr #{:id :resource-type :created :updated :acl
                 :parent :state}]
    (stu/is-invalid ::nuvlabox-state/schema (dissoc state attr)))

  ;; optional
  (doseq [attr #{:next-heartbeat :resources :peripherals :wifi-password}]
    (stu/is-valid ::nuvlabox-state/schema (dissoc state attr))))
