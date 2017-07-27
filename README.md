[![Build Status](https://travis-ci.org/benemon/he-rss-poll.svg?branch=master)](https://travis-ci.org/benemon/he-rss-poll)

# Vert.x Highways England RSS Polling Example

This is a small project which will by default poll the [Highways England RSS feed](http://m.highways.gov.uk/feeds/rss/UnplannedEvents.xml) for unplanned events on England's roads.


### Configuration
* Install JDG 7.1 / Infinispan 8.4.x

* Remove REST endpoint from `standalone.xml` if running both on the same machine, as it causes port clashes between Vert.x and JDG / Infinispan.

* Set the following options for your default cache implementation. It doesn't matter if it's local, replicated, or distributed:
~~~
 <local-cache name="default">
    <indexing index="ALL">
		    <property name="default.directory_provider">ram</property>
		</indexing>
	  <expiration lifespan="600000" max-idle="300000" />
 </local-cache>
~~~


* Set the environment variable `HE_RSS_URL_LIST` with the following value:
  `http://m.highways.gov.uk/feeds/rss/UnplannedEvents/South%20East.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/South%20West.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/Eastern.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/West%20Midlands.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/East%20Midlands.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/North%20West.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/North%20East.xml`

These entries are used to build the configured list of polled endpoints. By default, none are active when the application first starts up.

### Running
* Run JDG

* Run application with `mvn clean vertx:run`

* Open your browser and point it at http://localhost:8080.

* Configuration Tab: Based on the content of `HE_RSS_URL_LIST`, some carefully orchestrated shenanigans will have turned that list into something more friendly here.

* Live Data Tab: Shows the information retrieved from the currently active endpoints. Inactive endpoints will show locked Cards on the Live Data dashbaord. Active endpoints will show either `0`, or another figure, with the status icon updating depending on how rubbish your travel options are looking!

* Hot Spots shows the 'Top 10' worst roads in the country over a given 5 minute window.

### TBD
  
  * Allow user to define polling frequency for each individual PollingVerticle instance.
  
  * Untangle the spaghetti

 ~~* Allow user to select which sources are polled dynamically~~
  
~~* Improve UI.~~
  
~~* Poll individual regions to demonstrate scalability~~

~~* Store events in JBoss Data Grid~~

~~* Create REST API to access data stored in JDG~~

~~* Create UI to return results from REST API.~~
