(ns sixsq.nuvla.server.util.log
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log]
    [ring.util.response :as ring-resp]
    [sixsq.nuvla.server.util.response :as r]))


(defn log-and-throw
  "Logs the given message and returns an error response with the given status
   code and message."
  [status msg]
  (log/error status "-" msg)
  (throw (r/ex-response msg status)))


(defn log-and-throw-400
  "Logs the given message as a warning and then throws an exception with a 400
   response."
  [msg]
  (let [response (-> {:status 400 :message msg}
                     r/json-response
                     (ring-resp/status 400))]
    (log/warn msg)
    (throw (ex-info msg response))))


(defn log-error-and-throw-with-redirect
  "Logs the given message and returns an error response. The error response
   will contain the status code and message if the redirect-url is not
   provided. If the redirect-url is provided, then an error response with a
   redirect to the given URL will be provided. The error message is appended as
   the 'error' query parameter."
  [status msg redirect-url]
  (log/error status "-" msg)
  (if redirect-url
    (throw (r/ex-redirect msg nil redirect-url))
    (throw (r/ex-response msg status))))
