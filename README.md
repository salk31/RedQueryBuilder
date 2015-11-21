RedQueryBuilder
===============

An interative demo and ZIP download are hosted on GAE here http://redquerybuilder.appspot.com/

RedQueryBuilder uses an adapted parser from http://www.h2database.com/html/main.html and shares the parse object tree.


Build
-----

* Install Maven http://maven.apache.org/run-maven/index.html
* mvn clean install
* The JavaScript friendly files will be in js/target/js-{version}

Eclipse JUnit
-------------
Known to be good: Eclipse 4.4.2, Google plugin 3.8.0, GWT 2.7.0

JSInterop tests need:
-Dgwt.args="-XjsInteropMode JS -nodevMode"


