;; Copyright © 2014, JUXT LTD.

(ns bidi.ring
  (:require
   [bidi.bidi :as bidi :refer :all]))

(defprotocol Handle
  (handle-request [_ req match-context]
    "Handle a Ring request, but optionally utilize any context that was
    collected in the process of matching the handler."
    ))

(extend-protocol Handle
  clojure.lang.Fn
  (handle-request [f req _]
    (f req)))

(defn make-handler
  "Create a Ring handler from the route definition data
  structure. Matches a handler from the uri in the request, and invokes
  it with the request as a parameter."
  ([route handler-fn]
      (assert route "Cannot create a Ring handler with a nil Route(s) parameter")
      (fn [{:keys [uri path-info] :as request}]
        (let [path (or path-info uri)
              {:keys [handler route-params] :as match-context}
              (apply match-route route path (apply concat (seq request)))]
          (when handler
            (handle-request
             (handler-fn handler)
             (-> request
                 (update-in [:params] merge route-params)
                 (update-in [:route-params] merge route-params))
             (apply dissoc match-context :handler (keys request))
             )))))
   ([route] (make-handler route identity)))
