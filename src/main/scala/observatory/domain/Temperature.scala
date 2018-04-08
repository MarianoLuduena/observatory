package observatory.domain

import java.time.LocalDate

import observatory.Year
import observatory.parsers.CSVLineReader

import scala.util.Try

object Temperature {

  /**
    * CSV Format: "STN Identifier, WBAN Identifier, Month, Day, Temperature (in °F)"
    *
    * @param line String representing a line from the CSV input file
    * @return Try[Temperature]
    */
  def parse(line: String): Try[Temperature] = Try {
    val lr = new CSVLineReader(line)
    Temperature(
      stn = lr.readStr,
      wban = lr.readStr,
      month = lr.readInt,
      day = lr.readInt,
      tempF = lr.readDouble
    )
  }

  val nullTemperatureValue = BigDecimal(9999.9)
}

case class Temperature(stn: String, wban: String, month: Int, day: Int, tempF: observatory.Temperature) {

  val stationUid: String = stn + wban

  def date(year: Year): LocalDate = LocalDate.of(year, month, day)

  val tempC: observatory.Temperature = (tempF - 32) * 5 / 9

  val isTemperatureNull: Boolean = BigDecimal(tempF) == Temperature.nullTemperatureValue
}
