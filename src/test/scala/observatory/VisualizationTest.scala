package observatory


import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait VisualizationTest extends FunSuite with Checkers {

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

  ignore("2015 temperatures image to disk") {
    val avgTempByLocation = {
      val seq = Extraction.locateTemperatures(
        year = 2015,
        stationsFile = "/stations.csv",
        temperaturesFile = "/2015.csv"
      )
      Extraction.locationYearlyAverageRecords(seq)
    }

    val path = Visualization.visualize(avgTempByLocation, temperaturesColourScale).output("/tmp/test.png")
    assert(path != null)
  }
}
