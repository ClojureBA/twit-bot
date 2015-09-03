(ns twit-bot.core
  (:require [clojure.data.json :as json]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [twitter.api.restful :refer [statuses-mentions-timeline search-tweets]]
            [twitter.callbacks.handlers :as handlers]
            [twitter.callbacks.protocols :refer [map->SyncSingleCallback]]
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

(defn print-tweets [tweets]
  (doseq [tweet tweets]
    (let [text (:text tweet)
          user (get-in tweet [:user :screen_name])]
      (log/info "Tweet: " user text))))

(defn process-tweets [{:keys [statuses search_metadata]}]
  (log/info "Processing" (count statuses) "tweets")
  (print-tweets statuses))

(defn fetch-tweets [creds]
  (statuses-mentions-timeline
    :oauth-creds creds
    :callbacks (map->SyncSingleCallback
                 {:on-success handlers/response-return-body
                  :on-failure (comp #(log/error %) handlers/get-twitter-error-message)
                  :on-exception handlers/exception-print})))

(defn find-tweets
  "esta es mi doc"
  [creds query since-id]
  (search-tweets :oauth-creds creds
                 :params {:q query :since_id since-id}
                 :callbacks (map->SyncSingleCallback
                             {:on-success handlers/response-return-body
                              :on-failure (comp #(log/error %)
                                                handlers/get-twitter-error-message)
                              :on-exception handlers/exception-print})))

(defn start-bot
  ([control-ch frequency] (start-bot control-ch frequency nil))
  ([control-ch frequency since]
  (log/info "Starting bot...")
  (async/go-loop [since since]
    (let [[data ch] (async/alts! [control-ch (async/timeout frequency)])]
      (cond

        (= control-ch ch)
        (if (= data :stop)
          (log/info "Ok, no more tweets")
          (do
            (log/info "Don't know what to do with" data ". Try again while I keep tweeting...")
            (recur since)))

        ; tick, let's fetch tweets!
        :else
        (do
          (log/info "tick...")
          (let [{:keys [statuses search_metadata]} (find-tweets my-creds "#cljba-bot" since)]
            (print-tweets statuses)
            (recur (:max_id search_metadata)))))))))

(comment
  ; with dropping (or sliding buffer), a message will be dropped on async/put! with full buffer
  (def control-ch (async/chan (async/dropping-buffer 1)))
  (def control-ch (async/chan (async/sliding-buffer 1)))
  (def control-ch (async/chan 1))

  (def running-bot (start-bot control-ch 8000))

  ; async/>!! might block if buffer is full and can't drop messages
  (async/>!! control-ch :pause)
  ; put! will not block
  (async/put! control-ch :stop)
  (async/put! control-ch :pause #(println "accepted" %))

  (find-tweets my-creds "#cljba-bot")

  (fetch-tweets my-creds)
  )

(defn -main [& args]
  (async/<!! (start-bot (async/chan) 5000)))
