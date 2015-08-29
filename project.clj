(defproject twit-bot "0.1.0-SNAPSHOT"
  :description "Twitter bot example, for ClojureBA"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main twit-bot.core
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ "1.0.0"]
                 [twitter-api "0.7.8"]

                 ; logging :/
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [log4j/log4j "1.2.17"]])
