import scala.xml.{Node, XML}
import com.tinkerpop.gremlin.scala._
import java.io.File
import org.slf4j.LoggerFactory
import GraphUtils._
import com.tinkerpop.blueprints.Graph


object VtmLoader {
  private val Log = LoggerFactory.getLogger(getClass)

  def apply(path: File, graph: Graph) {
    val vtmsXml = XML loadFile path

    Log debug "Loading VTMs"
    vtmsXml \ "VTM" foreach { vtmXml =>
      val vertex = graph.addV()
      Log trace s"Add vertex VTM ${(vtmXml \ "VTMID").text}"

      vertex.setProperty("type", "VTM")
      (vtmXml \ "VTMID").headOption map (setVertexId(_, vertex, graph))

      val setDmdProperty = setVertexProperty(_: Node, vertex)
      (vtmXml \ "INVALID").headOption map setDmdProperty
      (vtmXml \ "NM").headOption map setDmdProperty
      (vtmXml \ "ABBREVNM").headOption map setDmdProperty
      (vtmXml \ "VTMIDPREV").headOption map setDmdProperty
      (vtmXml \ "VTMIDDT").headOption map setDmdProperty
    }
  }
}
