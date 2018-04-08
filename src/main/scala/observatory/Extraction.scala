package observatory

import java.time.LocalDate

import observatory.domain.{Station, Temperature}
import observatory.helpers.SparkSpecHelper
import org.apache.spark.rdd.RDD

/**
  * 1st milestone: data extraction
  */
object Extraction extends SparkSpecHelper {

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    collect(locateTemperaturesRDD(year, stationsFile, temperaturesFile))
  }

  /**
    * This method should return the list of all the temperature records converted in degrees Celsius along with their
    * date and location (ignore data coming from stations that have no GPS coordinates). You should not round the
    * temperature values. The file paths are resource paths, so they must be absolute locations in your classpath (so
    * that you can read them with getResourceAsStream). For instance, the path for the resource file 1975.csv is
    * /1975.csv.
    *
    * @param year Year number
    * @param stationsFile Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return An RDD containing triplets (date, location, temperature)
    */
  private def locateTemperaturesRDD(year: Year, stationsFile: String, temperaturesFile: String):
    RDD[(LocalDate, Location, Temperature)] = {

    val stationsRDD =
      read(stationsFile)
        .flatMap { l => Station.parse(l).toOption }  // ignore the ones that failed parsing (e.g., those without location)
        .map(x => (x.uid, x))  // build pairRDD

    val temperaturesRDD =
      read(temperaturesFile)
        .flatMap { l => Temperature.parse(l).toOption }  // ignore the ones that failed parsing
        .filter(!_.isTemperatureNull)  // discard null temperatures
        .groupBy(_.stationUid)  // build pairRDD

    val joinedRDD = persist {
      stationsRDD.leftOuterJoin(temperaturesRDD).flatMap {
        case (_, (station, oTempSeq)) =>
          oTempSeq.getOrElse(Iterable()).map(t => (t.date(year), station.location, t.tempC))
      }
    }

    joinedRDD
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    collect(locationYearlyAverageRecordsRDD(spark.sparkContext.parallelize[(LocalDate, Location, Temperature)](records.toSeq)))
  }

  /**
    * This method should return the average temperature at each location, over a year.
    *
    * @param records An RDD containing triplets (date, location, temperature)
    * @return An RDD containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecordsRDD(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] = {
    persist {
      records.groupBy(_._2).mapValues { i =>
        val (sum, q) = i.foldLeft((0.0, 0))( (accum, e) => (accum._1 + e._3, accum._2 + 1))
        sum / q  // dividing by zero should never happen since there is at least one temperature measure for each station
      }
    }
  }

}
