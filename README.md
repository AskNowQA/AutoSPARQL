## Introduction

AutoSPARQL TBSL is a graphical user interface, which allows to answer natural
language queries over RDF knowledge bases. It is based on algorithms
implemented in the DL-Learner Semantic Web machine learning framework.

## Requirements
* Java 7 or higher
* Maven 3 or higher
* Git

## Installation and Execution
1. clone the git repository
2. run `./compile` and then `./run` (Linux, Mac) or `compile.bat` and then `run.bat` (Windows)
* Manually: Do `mvn install -N` in the folders in that order: autosparql, commons, algorithm-tbsl. Then go into autosparql-tbsl and run `mvn jetty:run`.

Then, go into your browser and access `http://localhost:8080` and click on the link to the application.

## Error Reporting
If you encounter errors, please look at the issues if the problem is already reported.
If not, please create a single issue including the command line output.
If the error occurs during compilation, please use `./createcompillelog` instead of `./compile` to create the compile log.

## Adding your own Dataset
Using your own datasource instead of DBpedia or Oxford is nontrivial.
It needs several days of work in addition to the time needed to familiarize yourself with the code base.

You need to:

- fork AutoSPARQL TBSL
- add your own domain dependent lexicon as an LTAG grammar at algorithm-tbsl/src/main/resources/tbsl/lexicon (see http://pub.uni-bielefeld.de/publication/2002961 and http://pub.uni-bielefeld.de/publication/2278529 as well as the existing files in that folder)
- add your knowledge base:
 1. as a local model to algorithm-tbsl/src/main/resources/models/yourmodel (preferred for small knowledge bases as it is much faster and more reliable)
 2. as a SPARQL (version 1.0 is enough) endpoint along with a SOLR server instance
- create a singleton for your knowledge base (see package org.aksw.autosparql.tbsl.algorithm.knowledgebase)
- extend org.aksw.autosparql.tbsl.algorithm.learning.TBSL with TbslYourKnowledgeBase
- finally, in case the dataset isn't a private model, please do a pull request so that it can be integrated in the main project

## Modules and Packages
| Maven Module      | Package                             | Purpose       |
|-------------------|-------------------------------------|---------------|
| autosparql-parent |                  -                  | Parent Module |
| algorithm-tbsl    | org.aksw.autosparql.tbsl.algorithm  | Algorithm     |
| commons           | org.aksw.autosparql.commons         | Utilities     |
| autosparql-tbsl   | org.aksw.autosparql.tbsl.gui.vaadin | Web Interface |
