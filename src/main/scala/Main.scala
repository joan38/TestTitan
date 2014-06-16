import com.thinkaurelius.titan.core.TitanFactory
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration._
import com.tinkerpop.blueprints.Vertex
import org.apache.commons.configuration.BaseConfiguration
import org.slf4j.LoggerFactory


object Main extends App {
  private val Log = LoggerFactory.getLogger(getClass)
  val AppName = "Test Titan"
  val Version = "0.1"

  new scopt.OptionParser[Config](AppName) {
    head(AppName, Version)
    opt[String]('x', "xml") valueName "<path>" action {
      (x, c) => c.copy(dmdPath = x)
    } text "path to the DMD files"
    opt[String]('d', "dbPath") valueName "<path>" action {
      (x, c) => c.copy(dataBasePath = x)
    } text "path to the DB"
    opt[String]('i', "indexing") valueName "<mode>" action {
      (x, c) => c.copy(indexingMode = x)
    } text "Indexing mode"
    opt[String]('q', "querying") valueName "<mode>" action {
      (x, c) => c.copy(queryStrategy = x)
    } text "Query mode"
  } parse(args, Config()) map { config =>
    Log debug "Starting"

    // Setup the DB
    val graph = createTitanGraph(config.dataBasePath, config.indexingMode)

    try {
      // Import everything in the graph DB
        ImportData(config.dmdPath, graph, config.queryStrategy)
        graph.commit()
    } finally {
      Log debug "Shutting down the graph DB"
      graph.shutdown()
    }
  }

  def createTitanGraph(directory: String, indexingConfig: String) = {
    val esIndexName = "elasticsearch"
    val luceneIndexName = "lucene"

    val config = new BaseConfiguration
    val storage = config.subset(STORAGE_NAMESPACE)

    // Configuring local backend
    storage.setProperty(STORAGE_BACKEND_KEY, STORAGE_BACKEND_DEFAULT)
    storage.setProperty(STORAGE_DIRECTORY_KEY, directory)

    // Configuring elastic search index
    val esIndex = storage.subset(INDEX_NAMESPACE).subset(esIndexName)
    esIndex.setProperty(INDEX_BACKEND_KEY, "elasticsearch")
    esIndex.setProperty("local-mode", true)
    esIndex.setProperty("client-only", false)
    esIndex.setProperty(STORAGE_DIRECTORY_KEY, s"$directory/es")

    // Configuring lucene index
    val index = storage.subset(INDEX_NAMESPACE).subset(luceneIndexName)
    index.setProperty(INDEX_BACKEND_KEY, "lucene")
    index.setProperty(STORAGE_DIRECTORY_KEY, s"$directory/lucene")

    val graph = TitanFactory.open(config)

    indexingConfig match {
      case "none" =>
      case "titan" =>
        graph.makeKey("dmdid").dataType(classOf[String]).indexed(classOf[Vertex]).make()
        graph.makeKey("type").dataType(classOf[String]).indexed(classOf[Vertex]).make()
      case "es" =>
        graph.makeKey("dmdid").dataType(classOf[String]).indexed(esIndexName, classOf[Vertex]).make()
        graph.makeKey("type").dataType(classOf[String]).indexed(esIndexName, classOf[Vertex]).make()
      case "lucene" =>
        graph.makeKey("dmdid").dataType(classOf[String]).indexed(luceneIndexName, classOf[Vertex]).make()
        graph.makeKey("type").dataType(classOf[String]).indexed(luceneIndexName, classOf[Vertex]).make()
    }

    graph.commit()

    // To let ES finish to init
    Thread.sleep(5000)

    graph
  }
}
