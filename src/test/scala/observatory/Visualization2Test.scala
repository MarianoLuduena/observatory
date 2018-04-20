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

  private def processingTime(start: Long, finish: Long): String = s"${(finish - start).toDouble / 1000 / 60} minutes"

  ignore("Deviation tiles generation") {
    // Compute normals
    val normals = {
      val t0 = System.currentTimeMillis()
      val avgTemperatures = (1975 until 1990).map(averageTemperatures)
      val avg = Manipulation.average(avgTemperatures)
      val t1 = System.currentTimeMillis()
      println(s"Normals processing time: ${processingTime(t0, t1)}")
      avg
    }

    // Compute deviations
    (1990 until 2016).foreach { year =>
      println(s"Processing deviations for year $year")

      val t0 = System.currentTimeMillis()
      val grid = Manipulation.deviation(averageTemperatures(year), normals)

      val t1 = System.currentTimeMillis()
      println(s"Grid processing time for year $year: ${processingTime(t0, t1)}")

      def generateImage(year: Year, tile: Tile, data: Iterable[(Location, Temperature)]): Unit = {
        val img = Visualization2.visualizeGrid(grid, colourScale, tile).scaleTo(256, 256)  // Hack to reuse generateTiles logic
        val dirPath = s"./target/deviations/$year/${tile.zoom}"
        val directory = new java.io.File(dirPath)
        if (!directory.exists()) directory.mkdirs()
        val path = img.output(dirPath + s"/${tile.x}-${tile.y}.png")
        println(path)
      }

      val t2 = System.currentTimeMillis()
      // One at a time so as to avoid memory overflow
      Interaction.generateTiles(Iterable((year, Iterable())), generateImage)
      val t3 = System.currentTimeMillis()
      println(s"Generation of all tiles for year $year time: ${processingTime(t2, t3)}")
    }
  }
}
