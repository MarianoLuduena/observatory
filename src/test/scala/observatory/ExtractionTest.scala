package observatory

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

trait ExtractionTest extends FunSuite {

  ignore("Extraction with 2015 Dataset") {
    val seq = Extraction.locateTemperatures(
      year = 2015,
      stationsFile = "./src/main/resources/stations.csv",
      temperaturesFile = "./src/main/resources/2015.csv"
    )
    assertResult(4091191, "Sequence size does not match")(seq.size)

    val avgTempByLocation = Extraction.locationYearlyAverageRecords(seq).take(100)
    avgTempByLocation.foreach(println)
  }
}