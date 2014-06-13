import scala.xml.{Node, XML}
import com.tinkerpop.gremlin.scala._
import org.slf4j.LoggerFactory
import java.io.File
import GraphUtils._
import com.tinkerpop.blueprints.Graph


object IngredientLoader {
  private val Log = LoggerFactory.getLogger(getClass)

  def apply(path: File, graph: Graph) {
    def loadIngredient(ingredientXml: Node) {
      val vertex = graph.addV()
      Log trace s"Add vertex ING ${(ingredientXml \ "ISID").text}"

      vertex.setProperty("type", "ING")
      (ingredientXml \ "ISID").headOption map (setVertexId(_, vertex, graph))

      val setDmdProperty = setVertexProperty(_: Node, vertex)
      (ingredientXml \ "ISIDDT").headOption map setDmdProperty
      (ingredientXml \ "ISIDPREV").headOption map setDmdProperty
      (ingredientXml \ "INVALID").headOption map setDmdProperty
      (ingredientXml \ "NM").headOption map setDmdProperty orElse (throw new IllegalStateException("NM required"))
    }

    val ingredientsXml = XML loadFile path

    Log debug "Loading Ingredients"
    ingredientsXml \ "ING" foreach loadIngredient
  }
}
