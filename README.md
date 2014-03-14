{jaggery.js} - The delicious Javascript framework
============================================

Jaggery is a framework to write webapps and HTTP-focused web services 
for all aspects of the application: front-end, communication, server-side
logic and persistence in pure Javascript. One of the intents of this
framework is to reduce the gap between writing web apps and web services.
Jaggery is open-source and released under Apache 2.0.

As a pure Javascript server-side scripting engine, Jaggery combines all
the strengths of Javascript with flexibility and freedom at both the
development and deployment stages. Most JavaScript webapp development
mechanisms restrict developers to a framework-specific structure or
patterns, which demand an additional learning curve. By contrast, with
Jaggery, any developers who are versed in Javascript have everything
they need to get going.

More information about jaggery can be found at 
[jaggeryjs.org](http://jaggeryjs.org).

###Building###

1. Clone the repo using `git clone --recurse-submodules https://github.com/wso2/jaggery.git`.
If you have already clone repo, then init jaggery-extensions submodule using `git submodule init && git submodule update`
2. `mvn clean install`


####Jaggery Community Server (with docs, samples and modules)####

1. Build Jaggery Core Server
2. Build [Jaggery Extensions](https://github.com/wso2/jaggery-extensions) project
3. Build Jaggery Community Server using `mvn clean install -P community`

