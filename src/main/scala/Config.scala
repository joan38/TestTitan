case class Config(dmdPath: String = "dmd",
                  dataBase: String = "titan",
                  dataBasePath: String = "target/database",
                  queryStrategy: String = "graphQuery",
                  indexingMode: String = "titan")