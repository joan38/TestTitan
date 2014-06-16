case class Config(dmdPath: String = "resources",
                  dataBase: String = "titan",
                  dataBasePath: String = "target/database",
                  queryStrategy: String = "graphQuery",
                  indexingMode: String = "titan")