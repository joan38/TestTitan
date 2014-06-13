import java.io.{PrintWriter, File}
import java.nio.charset.Charset
import java.nio.file.{Paths, Files}

import com.thinkaurelius.titan.example.GraphOfTheGodsFactory
import com.tinkerpop.gremlin.java.GremlinPipeline
import org.slf4j.LoggerFactory

import scala.util.Random
import scala.xml.XML

//import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph
import com.tinkerpop.blueprints.Vertex
import org.apache.commons.configuration.BaseConfiguration
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration._
import com.thinkaurelius.titan.core.TitanFactory
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.scala._


object Main extends App {
  private val Log = LoggerFactory.getLogger(getClass)
  val AppName = "Test Titan"
  val Version = "0.1"


  // Code to replace Drug names by random
  Seq("resources/f_ingredient2_3080514.xml",
    "resources/f_lookup2_3080514.xml",
    "resources/f_vmp2_3080514.xml",
    "resources/f_vtm2_3080514.xml") map { path =>
    (path, new String(Files.readAllBytes(Paths.get(path))))
  } map {
    case (path, content) =>
      val start = Math.abs(Random.nextInt(100))
      val end = start + Math.abs(Random.nextInt(15))
      (path, "<NM>.*</NM>".r.findAllIn(content) map (_.)  content.replaceAll("<NM>.*</NM>", s"<NM>${content.substring(start, end).replaceAll("""\"|<|>|=""", "")}</NM>"))
  } map {
    case (path, content) =>
      Files.delete(Paths.get(path))
      val out = new PrintWriter(path)
      out.println(content)
      out.close()
  }
  System.exit(0)

  new scopt.OptionParser[Config](AppName) {
    head(AppName, Version)
    opt[String]('m', "dmd") valueName "<path>" required() action {
      (x, c) => c.copy(dmdPath = x)
    } text "path to the DMD files"
    opt[String]('d', "db") valueName "<name>" action {
      (x, c) => c.copy(dataBase = x)
    } text "name of the DB"
    opt[String]('s', "dbPath") valueName "<path>" action {
      (x, c) => c.copy(dataBasePath = x)
    } text "path to the DB"
    opt[String]('i', "indexing") valueName "<mode>" action {
      (x, c) => c.copy(indexingMode = x)
    } text "Indexing mode"
    opt[String]('i', "querying") valueName "<mode>" action {
      (x, c) => c.copy(queryingMode = x)
    } text "Query mode"
  } parse(args, Config()) map { config =>
    Log debug "Starting"

    // Setup the DB
    val graph = createTitanGraph(config.dataBasePath, config.indexingMode)

    try {
      // Import everything in the graph DB
        ImportDmd(config.dmdPath, graph)
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
    Thread.sleep(1000)

    graph
  }
}
