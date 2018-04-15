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
      val seq = Extraction.locateTemperaturesRDD(
        year = 2015,
        stationsFile = "/stations.csv",
        temperaturesFile = "/2015.csv"
      )
      Extraction.locationYearlyAverageRecordsRDD(seq).collect()
    }

    val path = Visualization.visualize(avgTempByLocation, temperaturesColourScale).output("/tmp/test.png")
    assert(path != null)
  }

  test("Predict temperature") {
    val temperatures = Iterable[(Location, Temperature)](
      (Location(-5.0, 0.0), 0.0),
      (Location(-1.0, 2.0), 10.0),
      (Location(0.0, 2.0), 10.0),
      (Location(1.0, 3.0), 15.0),
      (Location(5.0, -3.0), -5.0)
    )
    val t = Visualization.predictTemperature(temperatures, Location(0.0, 0.0))
    assertResult(true)(t > 0)
  }

  test("Basic color interpolation") {
    assertResult(Color(255, 255, 255))(Visualization.interpolateColor(temperaturesColourScale, 65.0))
    assertResult(Color(255, 255, 255))(Visualization.interpolateColor(temperaturesColourScale, 60.0))
    assertResult(Color(255, 0, 0))(Visualization.interpolateColor(temperaturesColourScale, 32.0))
    assertResult(Color(17, 0, 54))(Visualization.interpolateColor(temperaturesColourScale, -55.0))
    assertResult(Color(0, 0, 0))(Visualization.interpolateColor(temperaturesColourScale, -60.0))
    assertResult(Color(0, 0, 0))(Visualization.interpolateColor(temperaturesColourScale, -65.0))
  }
}
