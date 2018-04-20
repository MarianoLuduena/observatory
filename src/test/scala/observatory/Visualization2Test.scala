package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait Visualization2Test extends FunSuite with Checkers {

  private val colourScale = Iterable(
    (7.0, Color(0, 0, 0)),
    (4.0, Color(255, 0, 0)),
    (2.0, Color(255, 255, 0)),
    (0.0, Color(255, 255, 255)),
    (-2.0, Color(0, 255, 255)),
    (-7.0, Color(0, 0, 255))
  )

  private def averageTemperatures(year: Year): Iterable[(Location, Temperature)] =
    Extraction.locationYearlyAverageRecordsRDD(
      Extraction.locateTemperaturesRDD(
        year = year,
        stationsFile = "/stations.csv",
        temperaturesFile = s"/$year.csv"
      )
    ).collect.toIterable

  ignore("Deviation tiles generation") {
    // Compute normals
    val normals = {
      val avgTemperatures = (1975 until 1990).map(averageTemperatures)
      Manipulation.average(avgTemperatures)
    }

    // Compute deviations
    (1990 until 2016).foreach { year =>
      val grid = Manipulation.deviation(averageTemperatures(year), normals)

      def generateImage(year: Year, tile: Tile, data: Iterable[(Location, Temperature)]): Unit = {
        val img = Visualization2.visualizeGrid(grid, colourScale, tile)  // Hack to reuse generateTiles logic
        val dirPath = s"./target/temperatures/$year/${tile.zoom}"
        val directory = new java.io.File(dirPath)
        if (!directory.exists()) directory.mkdirs()
        val path = img.output(dirPath + s"/${tile.x}-${tile.y}.png")
        println(path)
      }

      // One at a time so as to avoid memory overflow
      Interaction.generateTiles(Iterable((year, Iterable())), generateImage)
    }
  }
}
