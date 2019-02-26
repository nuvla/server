(ns sixsq.nuvla.db.es.common.pagination
  (:require [clojure.tools.logging :as log]))

(def ^:const max-size 10000)

(defn es-paging-params
  "Returns [from size] based on the values 'first' and 'last'. If the value of
   'last' is zero, then a zero size is always returned. If the size exceeds the
   maximum, then an exception is thrown."
  [first last]
  (let [first (max 1 (or first 1))
        from (dec first)
        size (cond
               (nil? last) (- max-size from)
               (zero? last) 0
               (>= last first) (inc (- last first))
               :else 0)]
    (if (<= (+ from size) max-size)
      [from size]
      (throw (IllegalArgumentException.
               (str "size " size " too large; limit is " max-size))))))
