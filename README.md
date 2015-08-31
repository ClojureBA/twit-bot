# twit-bot

A Twitter bot that ...

## Setup

- Create twitter app: apps.twitter.com (needs mobile phone in account)
- Set Read/Write permissions
- Uncheck "Allow this application to be used to Sign in with Twitter" in the app settings
- Take note of consumer key & secret
- Authorize app in bot account, for example using twurl (ruby gem):

        gem install twurl
        twurl authorize --consumer-key "consumer-key" \
          --consumer-secret "consumer-secret"

- Grab tokens from ~/.twurlrc and save them in `.lein-env` (you can use `.lein-env.sample` file as an example)

## Build the bot!

The project provides just a skeleton for the bot: `process-tweets` will be called every time a new batch of tweets is received from the mentions timeline stream

From there, you can do anything! The sky is the limit! Just some ideas:

  - Assume the tweet will contain clojure code, so eval it and respond to the user with the output. If you like to walk on the safe side, you should take a look to [clojail](https://github.com/Raynes/clojail)
  - YARPSB (AKA: Yet Another Rock, Paper, Scissors Bot)
  - url shortener?
  - Even an echobot would be a good idea to play around with clojure!

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
