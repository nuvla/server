(ns sixsq.nuvla.server.resources.spec.pricing-test
  (:require
    [clojure.edn :refer [read-string]]
    [clojure.test :refer [deftest]]
    [sixsq.nuvla.server.resources.spec.pricing :as pricing]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]))

(def catalogue (read-string "{:plans ({:plan-id \"price_HAtjtEZXFVvXHn\", :name \"Nuvla.io Gold Subscription\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 15000.0}, :order 4, :required-items [\"price_HAtFObLDcxip0v\" \"price_HAtKALfhpzCEsD\" \"price_HAtTihhaLyIitj\"]} {:plan-id \"price_HAtivB39G4yVPc\", :name \"Nuvla.io Silver Subscription\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 4000.0}, :order 3, :required-items [\"price_HAtEztj9oxWrw9\" \"price_HAtJOwIzsCFFuM\" \"price_HAtPWAKIU2uY1t\"], :optional-items [\"price_HAtTLsUtfavEdW\"]} {:plan-id \"price_HAtg2LqD3uyraw\", :name \"Nuvla.io Bronze Subscription\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 500.0}, :order 2, :required-items [\"price_HAtDSJkL0NUoY9\" \"price_HAtIc8im1E58Vp\"], :optional-items [\"price_HAtIc8im1E58Vp\" \"price_HAtNCniwpFj3p2/price_HAtTLsUtfavEdW\"]} {:plan-id \"price_HAt31QlT2LC58v\", :name \"Nuvla.io Basic Subscription\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 0.0}, :order 1, :required-items [\"price_HAtBrMVEJzzk8d\" \"price_HAtHr5DkRjcSgc\"], :optional-items [\"price_HAtNCniwpFj3p2/price_HAtTLsUtfavEdW\"]}), :plan-items ({:plan-id \"price_HAtTihhaLyIitj\", :name \"Nuvla Gold Support\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 0.0}} {:plan-id \"price_HAtTLsUtfavEdW\", :name \"Nuvla Gold Support\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 500.0}} {:plan-id \"price_HAtPWAKIU2uY1t\", :name \"Nuvla Silver Support\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 0.0}} {:plan-id \"price_HAtNCniwpFj3p2\", :name \"Nuvla Silver Support\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"licensed\", :billing-scheme \"per_unit\", :amount 150.0}} {:plan-id \"price_HAtKALfhpzCEsD\", :name \"Nuvla VPN client\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"tiered\", :aggregate-usage \"max\", :tiers-mode \"graduated\", :tiers ({:order 0, :amount 0.0, :up-to 10} {:order 1, :amount 5.0, :up-to nil})}} {:plan-id \"price_HAtJOwIzsCFFuM\", :name \"Nuvla VPN client\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"tiered\", :aggregate-usage \"max\", :tiers-mode \"graduated\", :tiers ({:order 0, :amount 0.0, :up-to 5} {:order 1, :amount 5.0, :up-to nil})}} {:plan-id \"price_HAtIc8im1E58Vp\", :name \"Nuvla VPN client\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"tiered\", :aggregate-usage \"max\", :tiers-mode \"graduated\", :tiers ({:order 0, :amount 0.0, :up-to 1} {:order 1, :amount 5.0, :up-to nil})}} {:plan-id \"price_HAtHr5DkRjcSgc\", :name \"Nuvla VPN client\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"per_unit\", :amount 5.0, :aggregate-usage \"max\"}} {:plan-id \"price_HAtFObLDcxip0v\", :name \"Nuvla NuvlaBox\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"tiered\", :aggregate-usage \"max\", :tiers-mode \"graduated\", :tiers ({:order 0, :amount 0.0, :up-to 500} {:order 1, :amount 24.0, :up-to nil})}} {:plan-id \"price_HAtEztj9oxWrw9\", :name \"Nuvla NuvlaBox\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"tiered\", :aggregate-usage \"max\", :tiers-mode \"graduated\", :tiers ({:order 0, :amount 0.0, :up-to 100} {:order 1, :amount 32.0, :up-to nil})}} {:plan-id \"price_HAtDSJkL0NUoY9\", :name \"Nuvla NuvlaBox\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"tiered\", :aggregate-usage \"max\", :tiers-mode \"graduated\", :tiers ({:order 0, :amount 0.0, :up-to 10} {:order 1, :amount 40.0, :up-to nil})}} {:plan-id \"price_HAtBrMVEJzzk8d\", :name \"Nuvla NuvlaBox\", :charge {:currency \"eur\", :interval \"month\", :usage-type \"metered\", :billing-scheme \"tiered\", :aggregate-usage \"max\", :tiers-mode \"graduated\", :tiers ({:order 0, :amount 0.0, :up-to 1} {:order 1, :amount 50.0, :up-to nil})}})}"))
(deftest check-catalogue
  (let [timestamp "1964-08-25T10:00:00.00Z"
        root      (merge {:id            "pricing/catalogue"
                          :resource-type "pricing"
                          :created       timestamp
                          :updated       timestamp
                          :acl           {:owners   ["group/nuvla-admin"]
                                          :view-acl ["group/nuvla-user"]}}
                         catalogue)]

    (stu/is-valid ::pricing/schema root)))

