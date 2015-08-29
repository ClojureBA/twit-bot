# twit-bot

A Twitter bot that ...

## Setup

- Create twitter app: apps.twitter.com (needs mobile phone in account)
- Set Read/Write permissions
- Take note of consumer key & secret
- Authorize app in bot account, for example using twurl:

    gem install twurl
    twurl authorize --consumer-key "consumer-key" \
      --consumer-secret "consumer-secret"

- Grab tokens from ~/.twurlrc and save them in `.lein-env` (you can use `.lein-env.sample` file as an example)

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
