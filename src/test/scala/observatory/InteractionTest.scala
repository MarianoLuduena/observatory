package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait InteractionTest extends FunSuite with Checkers {

  private val temperaturesColourScale = Iterable[(observatory.Temperature, Color)](
    (60.0, Color(255, 255, 255)),
    (32.0, Color(255, 0, 0)),
    (12.0, Color(255, 255, 0)),
    (0.0, Color(0, 255, 255)),
    (-15.0, Color(0, 0, 255)),
    (-27.0, Color(255, 0, 255)),
    (-50.0, Color(33, 0, 107)),
    (-60.0, Color(0, 0, 0))
  )

  ignore("One tile") {
    val img = {
      val avgTemp = Extraction.locationYearlyAverageRecordsRDD(
        Extraction.locateTemperaturesRDD(
          year = 1975,
          stationsFile = "/stations.csv",
          temperaturesFile = "/1975.csv"
        )
      ).collect()
      Interaction.tile(avgTemp, temperaturesColourScale, Tile(0, 0, 0))
    }

    val path = img.output("/tmp/test.png")
    println(path)
  }

  ignore("Generate tiles for 1975") {
    def generateImage(year: Year, tile: Tile, data: Iterable[(Location, Temperature)]): Unit = {
      val img = Interaction.tile(data, temperaturesColourScale, tile)
      val dirPath = s"./target/temperatures/$year/${tile.zoom}"
      val directory = new java.io.File(dirPath)
      if (!directory.exists()) directory.mkdirs()
      val path = img.output(dirPath + s"/${tile.x}-${tile.y}.png")
      println(path)
    }

    val start = System.currentTimeMillis()

    val year = 1975
    val data = Extraction.locationYearlyAverageRecordsRDD(
      Extraction.locateTemperaturesRDD(
        year = year,
        stationsFile = "/stations.csv",
        temperaturesFile = s"/$year.csv"
      )
    ).collect()

    Interaction.generateTiles[Iterable[(Location, Temperature)]](Iterable((year, data)), generateImage)

    val finish = System.currentTimeMillis()
    println(s"\nTotal processing time: ${(finish - start) / 60000.0} minutes")
  }
}
