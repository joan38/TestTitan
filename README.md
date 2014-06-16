TestTitan
=========

Purpose
-------
This project is intended to test querying Titan, Gremlin and Gremlin-Scala.
I created this project after having performance issues and though it would be easier to troubleshoot.

Usage
-----
This is a SBT project so run:  
`sbt clean "run -i [indexing mode] -q [querying mode]"`

`[indexing mode]` can be:

- none
- titan
- es (for Elastic Search indexing)
- lucene
 
`[querying mode]` can be:

- graphQuery for `graph.query().has("dmdid", id).has("type", type).vertices().toStream`
- javaPipes1 for `new GremlinPipeline(graph.getVertices("dmdid", id)).has("type", type).cast(classOf[Vertex]).iterator().toStream`
- javaPipes2 for `new GremlinPipeline(graph.getVertices).has("dmdid", id).has("type", type).cast(classOf[Vertex]).iterator().toStream`
- scalaPipes for `graph.V.has("dmdid", id).has("type", type).toStream()`

Results
-------
Here is what I measured on my machine (the times are the execution time given by SBT):

- `sbt clean "run -i titan -q graphQuery"` : ~157s
- `sbt clean "run -i titan -q javaPipes1"` : ~156s
- `sbt clean "run -i titan -q javaPipes2"` : too much (not using indexing)
- `sbt clean "run -i titan -q scalaPipes"` : too much (not using indexing)
- `sbt clean "run -i es -q graphQuery"` : too much (not using indexing)
- `sbt clean "run -i lucene -q graphQuery"` : too much (not using indexing)
