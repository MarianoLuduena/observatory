package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait ManipulationTest extends FunSuite with Checkers {

  test("Deviation") {
    val normals = Manipulation.average(
      Iterable(
        Iterable((Location(0, 0), 1.0), (Location(-1, 0), 1.0), (Location(0, 1), 1.0), (Location(1, 1), 1.0))
      )
    )

    val ts = Iterable((Location(0, 0), 2.0), (Location(-1, 0), 0.0), (Location(0, 1), 1.0), (Location(1, 1), -1.0))
    val dv = Manipulation.deviation(ts, normals)
    println(dv(GridLocation(0, 0)))
  }

  ignore("Five year average") {
    val averagesByYear = (1975 until 1980).map { year =>
      Extraction.locationYearlyAverageRecordsRDD(
        Extraction.locateTemperaturesRDD(
          year = year,
          stationsFile = "/stations.csv",  // FIXME: This must be done more efficiently
          temperaturesFile = s"/$year.csv"
        )
      ).collect.toIterable
    }

    val f = Manipulation.average(averagesByYear)
    println(f(GridLocation(0, 0)))
  }

  ignore("Temperature prediction - Location vs. GridLocation") {
    val avgTemp = Extraction.locationYearlyAverageRecordsRDD(
      Extraction.locateTemperaturesRDD(
        year = 1975,
        stationsFile = "/stations.csv",
        temperaturesFile = "/1975.csv"
      )
    ).collect

    val t1 = Visualization.predictTemperature(avgTemp, Location(90, -180))  // top-left corner
    val f = Manipulation.makeGrid(avgTemp)
    val t2 = f(GridLocation(90, -180))
    assertResult(true)(scala.math.abs(t2 - t1) < 0.001)
  }
}