import java.io.{IOException, File}
import javax.sql.rowset.spi.TransactionalWriter
import com.tinkerpop.blueprints.{TransactionalGraph, Graph}


object ImportData {

  def apply(pathToXmlDirectory: String, graph: TransactionalGraph, queryStrategy: String) = {
    val files = new File(pathToXmlDirectory).listFiles()

    // Lookups
    files.find(_.getName.matches( """.*lookup.*\.xml""")) map
      (LookupLoader(_, graph)) orElse
      (throw new IOException("Could not find lookup XML file"))

    // Ingredients
    files.find(_.getName.matches( """.*ingredient.*\.xml""")) map
      (IngredientLoader(_, graph)) orElse
      (throw new IOException("Could not find ingredient XML file"))

    // VTMs
    files.find(_.getName.matches( """.*vtm.*\.xml""")) map
      (VtmLoader(_, graph)) orElse
      (throw new IOException("Could not find VTM XML file"))
    graph.commit()

    // VMPs
    files.find(_.getName.matches( """.*vmp.*\.xml""")) map
      (VmpLoader(_, graph, queryStrategy: String)) orElse
      (throw new IOException("Could not find VMP XML file"))

    graph
  }
}
