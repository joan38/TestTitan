case class Config(dmdPath: String = "dmd",
                  dataBase: String = "titan",
                  dataBasePath: String = "target/database",
                  queryingMode: String = "graph",
                  indexingMode: String = "titan")