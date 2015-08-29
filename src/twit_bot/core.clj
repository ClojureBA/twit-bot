(ns twit-bot.core
  (:require [clojure.data.json :as json]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [twitter.api.restful :refer [statuses-mentions-timeline]]
            [twitter.callbacks.handlers :as handlers]
            [twitter.callbacks.protocols :refer [map->SyncStreamingCallback]]
            [twitter.oauth :refer [make-oauth-creds]]))

(def creds-keys [:app-consumer-key
                 :app-consumer-secret
                 :user-access-token
                 :user-access-token-secret])

(defn read-creds-from-env []
  (->> creds-keys
       (map (juxt identity #(env %)))
       (into {})))

(def my-creds
  (let [creds (read-creds-from-env)]
    (apply make-oauth-creds (map creds creds-keys))))

(defn parse-bodypart [response baos]
  (-> baos
      (.toString "UTF-8")
      (json/read-json)))

(defn process-tweets [tweets]
  (log/info "Processing" (count tweets) "tweets")
  (doseq [tweet tweets]
    (let [text (:text tweet)
          user (get-in tweet [:user :screen_name])]
      (log/info "Tweet: " user text))))

(defn fetch-tweets [creds]
  (:body (statuses-mentions-timeline :oauth-creds creds)))

(defn start-bot [control-ch frequency]
  (log/info "Starting bot...")
  (async/go-loop []
    (let [[data ch] (async/alts! [control-ch (async/timeout frequency)])]
      (log/info "tick...")
      (cond

        (= control-ch ch)
        (if (= data :stop)
          (log/info "Ok, no more tweets")
          (do
            (log/info "Don't know what to do with" data ". Try again while I keep tweeting...")
            (recur)))

        ; tick, let's fetch tweets!
        :else
        (do
          (process-tweets (fetch-tweets my-creds))
          (recur))))))

(comment
  (def control-ch (async/chan))
  (def running-bot (start-bot control-ch 5000))

  (async/put! control-ch :stop)
  (async/put! control-ch :pause)

  ((:cancel (meta running-bot)))
  ((:cancelled? (meta running-bot))))

(defn -main [& args]
  (start-bot))
