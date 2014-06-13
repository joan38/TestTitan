import scala.xml.Node
import com.tinkerpop.blueprints.{TransactionalGraph, Graph, Vertex}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.tinkerpop.gremlin.scala._


object GraphUtils {

  //  val engine = new GremlinGroovyScriptEngine()

  def searchDmdVertex(graph: Graph, `type`: String, id: String) = {
    val start = System.currentTimeMillis()

    val result = graph.query().has("dmdid", id).has("type", `type`).vertices().toStream
    //graph.V.has("dmdid", id).has("type", `type`).toStream()

    LoggerFactory.getLogger(getClass).info((System.currentTimeMillis() - start).toString)
    result
  }

  //engine.put("graph", graph)
  //engine.put("id", id)
  //engine.put("type", `type`)
  //engine.eval("""graph.V.has("dmdid", id).has("type", type)""")
  //  .asInstanceOf[GremlinGroovyPipeline[Vertex, Vertex]].toList.toList

  //graph.V.has("dmdid", id).has("type", `type`).toStream()
  //new GremlinPipeline(graph.getVertices("type", `type`)).has("dmdid", id).cast(classOf[Vertex]).toList.toList


  def searchPhpVertex(graph: Graph, `type`: String, id: String) =
    graph.query().has("phpid", id).has("type", `type`).vertices().toStream

  def setVertexId(xml: Node, vertex: Vertex, graph: Graph) =
    vertex.setProperty("dmdid", xml.text)

  def setVertexProperty(xml: Node, vertex: Vertex) =
    vertex.setProperty(toCamelCase(xml.label), xml.text)

  def toCamelCase(s: String): String =
    "_[a-z]".r.replaceAllIn(s.toLowerCase, _.toString().toUpperCase).replaceAll("_", "")
}
