import com.tinkerpop.gremlin.groovy.GremlinGroovyPipeline
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine
import com.tinkerpop.gremlin.java.GremlinPipeline
import scala.xml.Node
import com.tinkerpop.blueprints.{Graph, Vertex}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.tinkerpop.gremlin.scala._


object GraphUtils {
  val Log = LoggerFactory.getLogger(getClass)
  val Engine = new GremlinGroovyScriptEngine()

  def searchDmdVertex(graph: Graph, `type`: String, id: String, queryStrategy: String) = {
    val start = System.currentTimeMillis()

    val result = queryStrategy match {
      case "graphQuery" => graph.query().has("dmdid", id).has("type", `type`).vertices().toStream
      case "javaPipes1" => new GremlinPipeline(graph.getVertices("dmdid", id)).has("type", `type`).cast(classOf[Vertex]).iterator().toStream
      case "javaPipes2" => new GremlinPipeline(graph.getVertices).has("dmdid", id).has("type", `type`).cast(classOf[Vertex]).iterator().toStream
      case "javaPipes3" => new GremlinPipeline(graph).V().has("dmdid", id).has("type", `type`).cast(classOf[Vertex]).iterator().toStream
      case "scalaPipes" => graph.V.has("dmdid", id).has("type", `type`).toStream()
      case "groovyQuery" =>
        Engine.put("graph", graph)
        Engine.put("id", id)
        Engine.put("type", `type`)
        Engine.eval("""graph.V.has("dmdid", id).has("type", type)""")
          .asInstanceOf[GremlinGroovyPipeline[Vertex, Vertex]].iterator().toStream
    }

    Log.debug((System.currentTimeMillis() - start).toString)
    result
  }

  def setVertexId(xml: Node, vertex: Vertex, graph: Graph) =
    vertex.setProperty("dmdid", xml.text)

  def setVertexProperty(xml: Node, vertex: Vertex) =
    vertex.setProperty(toCamelCase(xml.label), xml.text)

  def toCamelCase(s: String) =
    "_[a-z]".r.replaceAllIn(s.toLowerCase, _.toString().toUpperCase).replaceAll("_", "")
}
