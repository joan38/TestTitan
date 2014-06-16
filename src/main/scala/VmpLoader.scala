import scala.xml.{Node, XML}
import com.tinkerpop.gremlin.scala._
import java.io.File
import GraphUtils._
import org.slf4j.LoggerFactory
import com.tinkerpop.blueprints.Graph


object VmpLoader {
  private val Log = LoggerFactory.getLogger(getClass)

  def apply(path: File, graph: Graph, queryStrategy: String) {
    def loadVmp(vmpXml: Node) {
      val vertex = graph.addV()
      Log trace s"Add vertex VMP ${(vmpXml \ "VPID").text}"

      vertex.setProperty("type", "VMP")
      (vmpXml \ "VPID").headOption map (setVertexId(_, vertex, graph))

      val setDmdProperty = setVertexProperty(_: Node, vertex)
      (vmpXml \ "VPIDDT").headOption map setDmdProperty
      (vmpXml \ "VPIDPREV").headOption map setDmdProperty
      (vmpXml \ "INVALID").headOption map setDmdProperty

      (vmpXml \ "NM").headOption map setDmdProperty orElse (throw new IllegalStateException("NM required"))
      (vmpXml \ "ABBREVNM").headOption map setDmdProperty
      (vmpXml \ "BASISCD").headOption map setDmdProperty orElse (throw new IllegalStateException("BASISCD required"))
      (vmpXml \ "NMDT").headOption map setDmdProperty
      (vmpXml \ "NMPREV").headOption map setDmdProperty
      (vmpXml \ "BASIS_PREVCD").headOption map setDmdProperty
      (vmpXml \ "NMCHANGECD").headOption map setDmdProperty

      (vmpXml \ "COMBPRODCD").headOption map setDmdProperty
      (vmpXml \ "PRES_STATCD").headOption map setDmdProperty orElse (throw new IllegalStateException("PRES_STATCD required"))
      (vmpXml \ "SUG_F").headOption map setDmdProperty
      (vmpXml \ "GLU_F").headOption map setDmdProperty
      (vmpXml \ "PRES_F").headOption map setDmdProperty
      (vmpXml \ "CFC_F").headOption map setDmdProperty
      (vmpXml \ "NON_AVAILCD").headOption map setDmdProperty
      (vmpXml \ "NON_AVAILDT").headOption map setDmdProperty
      (vmpXml \ "DF_INDCD").headOption map setDmdProperty
      (vmpXml \ "UDFS").headOption map setDmdProperty
      (vmpXml \ "UDFS_UOMCD").headOption map setDmdProperty
      (vmpXml \ "UNIT_DOSE_UOMCD").headOption map setDmdProperty

      (vmpXml \ "VTMID").headOption map { id =>
        graph.addE(searchDmdVertex(graph, "VTM", id.text, queryStrategy).head, vertex, "has")
      }
    }

    def loadVirtualProductIngredient(vpiXml: Node) {
      val vertex = graph.addV()
      Log trace s"Add vertex VPI ${vertex.getId}"
      vertex.setProperty("type", "VPI")

      val setDmdProperty = setVertexProperty(_: Node, vertex)
      (vpiXml \ "BASIS_STRNTCD").headOption map setDmdProperty
      (vpiXml \ "BS_SUBID").headOption map setDmdProperty
      (vpiXml \ "STRNT_NMRTR_VAL").headOption map setDmdProperty
      (vpiXml \ "STRNT_DNMTR_VAL").headOption map setDmdProperty

      (vpiXml \ "VPID").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "VMP", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("VPID required"))

      (vpiXml \ "ISID").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "ING", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("ISID required"))

      (vpiXml \ "STRNT_NMRTR_UOMCD").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "UNIT_OF_MEASURE", id.text, queryStrategy).head, "has")
      }

      (vpiXml \ "STRNT_DNMTR_UOMCD").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "UNIT_OF_MEASURE", id.text, queryStrategy).head, "has")
      }
    }

    def loadOntDrugForm(ontDrugFormXml: Node) {
      val vertex = graph.addV()
      Log trace s"Add vertex ONT ${vertex.getId}"
      vertex.setProperty("type", "ONT")

      (ontDrugFormXml \ "VPID").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "VMP", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("VPID required"))

      // TODO Might get ride of this vertex
      (ontDrugFormXml \ "FORMCD").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "ONT_FORM_ROUTE", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("FORMCD required"))
    }

    def loadDrugForm(drugFormXml: Node) {
      val vertex = graph.addV()
      Log trace s"Add vertex DFORM ${vertex.getId}"
      vertex.setProperty("type", "DFORM")

      (drugFormXml \ "VPID").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "VMP", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("VPID required"))

      // TODO Might get ride of this vertex
      (drugFormXml \ "FORMCD").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "FORM", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("FORMCD required"))
    }

    def loadDrugRoute(drugRouteXml: Node) {
      val vertex = graph.addV()
      Log trace s"Add vertex DROUTE ${vertex.getId}"
      vertex.setProperty("type", "DROUTE")

      (drugRouteXml \ "ROUTECD").headOption map (setVertexProperty(_, vertex)) orElse (throw new IllegalStateException("ROUTECD required"))

      (drugRouteXml \ "VPID").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "VMP", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("VPID required"))
    }

    def loadControlDrugInfo(controlDrugInfoXml: Node) {
      val vertex = graph.addV()
      Log trace s"Add vertex CONTROL_INFO ${vertex.getId}"
      vertex.setProperty("type", "CONTROL_INFO")

      val setDmdProperty = setVertexProperty(_: Node, vertex)
      (controlDrugInfoXml \ "CATCD").headOption map setDmdProperty orElse (throw new IllegalStateException("CATCD required"))
      (controlDrugInfoXml \ "CATDT").headOption map setDmdProperty
      (controlDrugInfoXml \ "CAT_PREVCD").headOption map setDmdProperty

      (controlDrugInfoXml \ "VPID").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "VMP", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("VPID required"))

      // TODO Might get ride of this vertex
      (controlDrugInfoXml \ "CATCD").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "CONTROL_DRUG_CATEGORY", id.text, queryStrategy).head, "has")
      } orElse (throw new IllegalStateException("CATCD required"))

      // TODO Might get ride of this vertex
      (controlDrugInfoXml \ "CAT_PREVCD").headOption map { id =>
        graph.addE(vertex, searchDmdVertex(graph, "CONTROL_DRUG_CATEGORY", id.text, queryStrategy).head, "previous")
      }
    }

    val vmpXml = XML loadFile path

    Log debug "Loading VMPs"
    vmpXml \ "VMPS" \ "VMP" foreach loadVmp

    Log debug "Loading Virtual Product Ingredient"
    vmpXml \ "VIRTUAL_PRODUCT_INGREDIENT" \ "VPI" foreach loadVirtualProductIngredient

    Log debug "Loading Ont Drug Form"
    vmpXml \ "ONT_DRUG_FORM" \ "ONT" foreach loadOntDrugForm

    Log debug "Loading Drug Form"
    vmpXml \ "DRUG_FORM" \ "DFORM" foreach loadDrugForm

    Log debug "Loading Drug Route"
    vmpXml \ "DRUG_ROUTE" \ "DROUTE" foreach loadDrugRoute

    Log debug "Loading Control Drug Info"
    vmpXml \ "CONTROL_DRUG_INFO" \ "CONTROL_INFO" foreach loadControlDrugInfo
  }
}
