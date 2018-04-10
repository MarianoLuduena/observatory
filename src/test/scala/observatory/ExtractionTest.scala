package observatory

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

trait ExtractionTest extends FunSuite {

  ignore("Extraction with 2015 Dataset") {
    val seq = Extraction.locateTemperatures(
      year = 2015,
      stationsFile = "/stations.csv",  // file obtained as resource
      temperaturesFile = "./src/main/resources/2015.csv"  // file as absolute/relative path
    )
    assertResult(4091191, "Sequence size does not match")(seq.size)

    val avgTempByLocation = Extraction.locationYearlyAverageRecords(seq).take(100)
    avgTempByLocation.foreach(println)
  }

  test("Data Extraction examples") {
    val locatedTemps = Extraction.locateTemperatures(
      year = 2015,
      stationsFile = "/w1_stations.csv",
      temperaturesFile = "/w1_temperatures.csv"
    ).toArray
    assertResult(3)(locatedTemps.length)

    val avgTemps = Extraction.locationYearlyAverageRecords(locatedTemps).toArray
    assertResult(2)(avgTemps.length)
  }
}