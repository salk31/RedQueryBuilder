RedQueryBuilder
===============

RedQueryBuilder fills the gap between a hand coded "quick search" and a full blown query language. A typical use is in an intranet web application where experienced users need a quick ad-hoc way to find particular data. This means they don't have to use report frameworks or ask a techie.

To use in your app you just need to drop in the JS, feed it the meta data for your database then handle the change events to get the SQL and argument values.

Features include:

* Pure JavaScript runtime.
* Can be fed simple meta data (JavaScript/JSON or GWT interfaces)
* Client side SQL parser so it can load existing SQL (within limits of the UI)
* Non-GWT integration support with event handlers and JavaScript object configuration. See source of the JDBC sample page.


RedQueryBuilder uses an adapted parser from http://www.h2database.com/html/main.html and shares the parse object tree.

An interative demo and ZIP download are hosted on GAE here http://redquerybuilder.appspot.com/


