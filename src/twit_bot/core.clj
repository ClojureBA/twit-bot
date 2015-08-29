(ns twit-bot.core
  (:require [clojure.data.json :as json]
            [environ.core :refer [env]]
            [twitter.api.restful :refer [statuses-mentions-timeline]]
            [twitter.callbacks.handlers :as handlers]
            [twitter.callbacks.protocols :refer [map->AsyncStreamingCallback]]
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
  (doseq [tweet tweets]
    (let [text (:text tweet)
          user (get-in tweet [:user :screen_name])]
      (println user text))))

(defn print-keys [tweets]
  (println (map keys tweets)))

; callback that just prints the text of the status
(def printer-callback
  (map->AsyncStreamingCallback {:on-bodypart (comp process-tweets parse-bodypart)
                                :on-failure (comp println handlers/response-return-everything)
                                :on-exception handlers/exception-print}))

(defn start-bot []
  (statuses-mentions-timeline
    :oauth-creds my-creds
    :callbacks printer-callback))

(comment
  (def running-bot (start-bot))

  ((:cancel (meta running-bot)))
  ((:cancelled? (meta running-bot))))

(defn -main [& args]
  (start-bot))
