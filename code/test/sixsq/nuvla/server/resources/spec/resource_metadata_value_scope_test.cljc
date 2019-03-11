(ns sixsq.nuvla.server.resources.spec.resource-metadata-value-scope-test
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test :refer [are deftest is]]
    [sixsq.nuvla.server.resources.spec.resource-metadata-value-scope :as spec]
    [sixsq.nuvla.server.resources.spec.resource-metadata-value-scope-enumeration-test :as enumeration]
    [sixsq.nuvla.server.resources.spec.resource-metadata-value-scope-item-test :as item]
    [sixsq.nuvla.server.resources.spec.resource-metadata-value-scope-range-test :as range]
    [sixsq.nuvla.server.resources.spec.resource-metadata-value-scope-single-value-test :as single-value]
    [sixsq.nuvla.server.resources.spec.resource-metadata-value-scope-unit-test :as unit]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]))


(def valid {:alpha enumeration/valid
            :beta  range/valid
            :gamma single-value/valid
            :delta unit/valid
            :zeta  item/valid})


(deftest check-value-scope

  (stu/is-valid ::spec/vscope valid)

  (stu/is-invalid ::spec/vscope {:badAttribute 1}))