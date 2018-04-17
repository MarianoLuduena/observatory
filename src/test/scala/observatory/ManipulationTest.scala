package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait ManipulationTest extends FunSuite with Checkers {

  ignore("Average with RDDs") {
    val tempsByYear = (1975 to 1980).map { year =>
      val temperaturesRDD = Extraction.locateTemperaturesRDD(
        year,
        stationsFile = "/stations.csv",
        temperaturesFile = s"/$year.csv"
      )
      val avgTemperaturesRDD = Extraction.locationYearlyAverageRecordsRDD(temperaturesRDD)
      temperaturesRDD.unpersist(blocking = false)
      avgTemperaturesRDD
    }

    val fn = Manipulation.averageRDD(tempsByYear)
    println(s"(0, 0) = ${fn(GridLocation(0, 0))}")
  }

}