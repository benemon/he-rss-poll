[![Build Status](https://travis-ci.org/benemon/he-rss-poll.svg?branch=master)](https://travis-ci.org/benemon/he-rss-poll)

# Vert.x Highways England RSS Polling Example

This is a small project which will by default poll the [Highways England RSS feed](http://m.highways.gov.uk/feeds/rss/UnplannedEvents.xml) for unplanned events and does literally nothing with the information for now.

To make it poll multiple sources for the information, set the environment variable `HE_RSS_URL_LIST` with the following value:

`http://m.highways.gov.uk/feeds/rss/UnplannedEvents/South%20East.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/South%20West.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/Eastern.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/West%20Midlands.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/East%20Midlands.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/North%20West.xml, http://m.highways.gov.uk/feeds/rss/UnplannedEvents/North%20East.xml`


### Configuration
* Install JDG 7.1 / Infinispan 8.4.x

* Remove REST endpoint from `standalone.xml`


### TBD
  * Improve UI.

  * Allow user to select which sources are polled dynamically
  
~~* Poll individual regions to demonstrate scalability~~

~~* Store events in JBoss Data Grid~~

~~* Create REST API to access data stored in JDG~~

~~* Create UI to return results from REST API.~~
