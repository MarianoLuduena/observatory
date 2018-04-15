package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import scala.collection.concurrent.TrieMap

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
}
