Jaggery ${carbon.product.version}
-----------------

${buildNumber}

Welcome to the Jaggery ${carbon.product.version} release

Jaggery is a framework to write webapps and HTTP-focused web services
for all aspects of the application: front-end, communication,
Server-side logic and persistence in pure Javascript. One of the
intents of this framework is to reduce the gap between writing web
apps and web services.

This Framework uses Mozilla Rhino to process Javascript at the
server and also contains a powerful caching layer with the support of
Rhino compiled scripts; so its as fast as the JVM. As few key features,
Jaggery has native JSON support and also E4X support for XML manipulation.



Release Notes - Jaggery - ALPHA-3

** Bugs, Improvements and New Features can be found in GitHub with below url
https://github.com/wso2/jaggery/issues?milestone=2&page=1&sort=created&state=closed


------------

Hardware Requirements
-------------------
1. Minimum memory - 256MB
2. Processor      - Pentium 800MHz or equivalent at minimum

Software Requirements
-------------------
1. Java SE Development Kit - 1.6 (1.6.0_21 onwards)
2. Apache Ant - An Apache Ant version is required. Ant 1.7.0 version is recommended.
3. The Management Console requires full Javascript enablement of the Web browser
   NOTE:
     On Windows Server 2003, it is not allowed to go below the medium security
     level in Internet Explorer 6.x.

Known Issues
------------

All known issues have been recorded at https://wso2.org/jira/browse/JAGGERY

Carbon Binary Distribution Directory Structure
--------------------------------------------


    JAGGERY_HOME
        ├── apps
        │   ├── freshometer
        │   ├── ROOT
        │   ├── sample
        │   ├── shout
        │   └── taskmaster
        ├── bin
        │   ├── jaggery.bat
        │   ├── jaggery.sh
        │   ├── server.bat
        │   └── server.sh
        ├── carbon
        │   ├── bin
        │   ├── dbscripts
        │   ├── lib
        │   ├── repository
        │   ├── tmp
        │   └── wso2carbon.pid
        ├── etc
        │   └── modulemetafiles
        ├── INSTALL.txt
        ├── LICENSE.txt
        ├── modules
        │   └── modules.xml
        ├── README.txt
        └── release-notes.html

    - bin
      Contains the scripts needed to start the Jaggery server (server.sh) and Jaggery Shell (jaggeryshell.sh)

    - carbon
      Known as CARBON_HOME, which is the home directory of WSO2 carbon server, The carbon server act as the
      Jaggery container.

    - apps
      The directory that contains the jaggery applications

    - modules
      The modules directory, which contain jaggery modules configuration.

    - etc
      Contains configuration files

    - LICENSE.txt
      Apache License 2.0 under which Jaggery is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 Carbon

    - release-notes.html
      Release information for Jaggery

Support
-------

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Carbon, visit the WSO2 Oxygen Tank (http://wso2.org)

Crypto Notice
-------------

This distribution includes cryptographic software.  The country in
which you currently reside may have restrictions on the import,
possession, use, and/or re-export to another country, of
encryption software.  Before using any encryption software, please
check your country's laws, regulations and policies concerning the
import, possession, or use, and re-export of encryption software, to
see if this is permitted.  See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms.  The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included cryptographic
software:

Apacge Rampart   : http://ws.apache.org/rampart/
Apache WSS4J     : http://ws.apache.org/wss4j/
Apache Santuario : http://santuario.apache.org/
Bouncycastle     : http://www.bouncycastle.org/

---------------------------------------------------------------------------
(c) Copyright 2011-2013 WSO2 Inc.
