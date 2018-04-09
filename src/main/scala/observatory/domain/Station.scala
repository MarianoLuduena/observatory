package observatory.domain

import observatory.Location
import observatory.parsers.CSVLineReader

import scala.util.Try

object Station {

  /**
    * CSV Format: "STN Identifier, WBAN Identifier, Latitude, Longitude"
    *
    * @param line String representing a line from the CSV input file
    * @return Try[Station]
    */
  def parse(line: String): Try[Station] = Try {
    val lr = new CSVLineReader(line)
    Station(
      stn = lr.readStr,
      wban = lr.readStr,
      location = Location(lr.readDouble, lr.readDouble)
    )
  }
}

case class Station(stn: String, wban: String, location: Location) {

  val uid: String = stn + "|" + wban

  val toPair: (String, Station) = (uid, this)
}
