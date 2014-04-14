Wikpedia Parser and Search Engine
--------------------
--------------------

Description
-------------------

************************************Files Used***********************************

Parser.java--->It is the main file to run which parses the document and call other appropriate classes 
to finally create a index file at given location.

Search.java---> is the file to search a given string inside the index folder

com.ire.basic
	Contributor.java---> It is the class defined for storing Contributor object fetched from the pages
	Page.java---> It is the class defined for storing Page object fetched from the xml parsing
	Revision.java---> It is the class defined for storing Contributor object fetched from the xml parsing

com.ire.engine
	Stemmer.java---> Class containing implementation of Porter Stemming
	Tokenizer.java---> Class containing methods to casefold,stem,tokenize and apply appropriate 
			   functions to the data
com.ire.handler
	SJHandler.java--->Handler for SaxParser for parsing the whole xml document

**********************************Index Info************************************

index will be stored in the 26 separate file a.txt b.txt c.txt likewise


***********************************Searching************************************

Binary Search is done over required files.

Field Queries and other queries are handled as mentioned

t:indiana actor indian
actor actress
etc

Ranking is done using tf-idf
weigtage is given like (0.5*title)/totaltitlecount+(0.3*info)/totalinfocount+(0.2*body)/totalbodycount+(0.05*cat)/totalcatcount+(0.05*link)/totallinkcount;

idf=log(total docs/no.of.docs containing term)

tf-idf is then scaled to [0,99999] which forms the root ranking for index 


Local PageRank Approximation and Implementation
 
System Requirements
-------------------

Operating System
Windows/Ubuntu 

Other S/w:
------------------
IDE for java (preferable eclipse)
Java: The repo can run on any java but having an uniformity on java1.7 will save effort in handling compatibility issues. (preferable java-oracle-1.7)
Installation:

Setup repository
------------------
Create a folder Search-Wiki in your eclipse workspace. From that folder execute the following command

    $ git clone https://github.com/sjuyal/Search-Wiki.git

Open eclipse and follow the following path 
File > Import > Existing Projects into Workspace and import the project in 'Search-Wiki' folder
This will import the project in your Eclipse IDE
