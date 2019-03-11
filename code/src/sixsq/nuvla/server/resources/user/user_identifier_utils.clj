(ns sixsq.nuvla.server.resources.user.user-identifier-utils
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [sixsq.nuvla.db.filter.parser :as parser]
    [sixsq.nuvla.db.impl :as db]
    [sixsq.nuvla.server.resources.common.crud :as crud]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.user-identifier :as user-identifier]))


(def ^:private active-user-filter "(state='ACTIVE')")


(defn generate-identifier
  ([authn-method external-login]
   (generate-identifier authn-method external-login nil))
  ([authn-method external-login instance]
   (str (or instance (name authn-method)) ":" external-login)))


(defn add-user-identifier!
  [username authn-method external-login instance]
  (let [user-id (str "user/" username)
        identifier (generate-identifier authn-method external-login instance)]
    (log/debugf "Creating a user-identifier resource for user %s with identifier %s" username identifier)
    (crud/add
      {:identity     {:current "internal"
                      :authentications
                               {"internal" {:roles #{"ADMIN"}, :identity "internal"}}}
       :sixsq.slipstream.authn/claims
                     {:username "internal", :roles "ADMIN"}
       :params       {:resource-name "user-identifier"}
       :route-params {:resource-name "user-identifier"}
       :user-roles   #{"USER"}
       :body         {:identifier identifier
                      :user       {:href user-id}}})))


(defn- to-am-kw
  [authn-method]
  (keyword (str (name authn-method) "login")))


(defn find-user-identifier
  "Returns the externalIdentifier resource associated with the given
   identifier. Returns nil if not found. Function never throws an exception."
  ([identifier]
   (try
     (crud/retrieve-by-id (str "user-identifier/" (u/md5 identifier)))
     (catch Exception _
       nil)))

  ([authn-method external-login & [instance]]
   (find-user-identifier (generate-identifier authn-method external-login instance))))


(defn user-identity-exists?
  [authn-method external-login & [instance]]
  (->> (generate-identifier authn-method external-login instance)
       find-user-identifier
       boolean))


(defn sanitize-login-name
  "Replace characters not satisfying [a-zA-Z0-9_] with underscore."
  [s]
  (when s (str/replace s #"[^a-zA-Z0-9_-]" "_")))


(defn create-cimi-filter
  [filter]
  {:filter (parser/parse-cimi-filter filter)})


(defn find-username-by-identifier
  [authn-method instance external-login]
  (let [user-identifier-in-use (find-user-identifier authn-method external-login instance)

        filter-str-fallback (format "%s='%s' and %s" (name (to-am-kw authn-method)) external-login active-user-filter)
        filter-fallback (create-cimi-filter filter-str-fallback)
        query-users-fallback (fn [f] (try
                                       (second (db/query "user" {:cimi-params f
                                                                 :user-roles  ["ADMIN"]}))
                                       (catch Exception _ [])))
        matched-users-fallback (query-users-fallback filter-fallback)

        ignore-user-when-deleted (fn [user-id]
                                   (when-not (= "DELETED" (-> (crud/retrieve-by-id user-id)
                                                              :state))
                                     user-id))
        get-user (fn [users] (:username (first users)))
        throw-ex (fn [users] (throw (Exception. (str "There should be only one result for "
                                                     external-login ". But found " (count users)))))
        username (cond user-identifier-in-use (some-> user-identifier-in-use
                                                      :user
                                                      :href
                                                      ignore-user-when-deleted
                                                      (str/split #"/" 2)
                                                      second)
                       (= (count matched-users-fallback) 1) (get-user matched-users-fallback)
                       (> (count matched-users-fallback) 1) (throw-ex matched-users-fallback))]

    username))


(defn find-identities-by-user
  [user-id]
  (let [filter (create-cimi-filter (format "user/href='%s'" user-id))]
    (try
      (second (db/query user-identifier/resource-type {:cimi-params filter
                                                       :user-roles  ["ADMIN"]}))
      (catch Exception _
        []))))