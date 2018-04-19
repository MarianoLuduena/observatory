package observatory.helpers

import observatory.{Color, Location}

import scala.collection.GenIterable
import scala.math._

trait InterpolationHelper {

  /**
    * Power parameter p.
    * Greater values of p assign greater influence to values closest to the interpolated point.
    * For N dimensions, power parameters p <= N cause the interpolated values to be dominated by points far away.
    *
    * @return Power parameter p value
    */
  protected def p: Double = 4

  /**
    * Inverse distance weighting
    *
    * @param samples List of values (distance, value -temperature-)
    * @return Interpolated value
    */
  protected def idw(samples: GenIterable[(Double, Double)]): Double = {
    val (num, denom, _) = samples.par.aggregate((0.0, 0.0, false))({ (ndl, s) =>
      if (ndl._3) ndl
      else if (s._1 < 1) (s._2, 1.0, true)
      else {
        val w = 1 / pow(s._1, p)
        (ndl._1 + s._2 * w, ndl._2 + w, ndl._3)
      }
    }, {
      case (t1@(_, _, l1), _) if l1 => t1
      case (_, t2@(_, _, l2)) if l2 => t2
      case ((n1, d1, _), (n2, d2, _)) => (n1 + n2, d1 + d2, false)
    })

    num / denom
  }

  /**
    * Method to determine whether one point is the antipodes of another one
    *
    * @param p1 Latitude and longitude of the first location
    * @param p2 Latitude and longitude of the second location
    * @return True if p2 is the antipodes of p1; false otherwise
    */
  protected def isAntipodes(p1: Location, p2: Location): Boolean =
    p1.lat == -p2.lat && (p1.lon == p2.lon + 180 || p1.lon == p2.lon - 180)

  /**
    * Earth's radius
    *
    * @return Earth's mean radius in Kilometers (Km)
    */
  protected def radius = 6371.0088

  /**
    * Great-circle distance
    *
    * @param p1 First location (point 1)
    * @param p2 Second location (point 2)
    * @return Distance between locations in Kilometers (Km)
    */
  protected def gcDistance(p1: Location, p2: Location): Double = {
    val centralAngle =
      if (p1 == p2) 0
      else if (isAntipodes(p1, p2)) Pi
      else {
        val p1Lat = p1.lat.toRadians
        val p2Lat = p2.lat.toRadians
        val deltaLon = abs(p1.lon.toRadians - p2.lon.toRadians)
        acos(sin(p1Lat) * sin(p2Lat) + cos(p1Lat) * cos(p2Lat) * cos(deltaLon))
      }

    radius * centralAngle
  }

  /**
    * Linear interpolation between two known points
    *
    * @param x0 X coordinate of first point
    * @param y0 Y coordinate of first point
    * @param x1 X coordinate of second point
    * @param y1 Y coordinate of second point
    * @param x Position at which I want to find the interpolated value
    * @return Interpolated value (Double) y
    */
  protected def lerp(x0: Double, y0: Int, x1: Double, y1: Int, x: Double): Double =
    (y0 * (x1 - x) + y1 * (x - x0)) / (x1 - x0)

  /**
    * Method to determine the interpolated colour given to known points with their associated colours
    *
    * @param lp Lower point (Temperature, Color)
    * @param up Upper point (Temperature, Color)
    * @param x Temperature for which the Color is desired
    * @return The interpolated Color for x Temperature
    */
  protected def interpolateColor(lp: (Double, Color), up: (Double, Color), x: Double): Color =
    Color(
      red = lerp(lp._1, lp._2.red, up._1, up._2.red, x).round.toInt,
      green = lerp(lp._1, lp._2.green, up._1, up._2.green, x).round.toInt,
      blue = lerp(lp._1, lp._2.blue, up._1, up._2.blue, x).round.toInt
    )

  /**
    * Special case of bilinear interpolation
    *
    * @param x Position on the X axis
    * @param y Position on the Y axis
    * @param d00 Known value at (0, 0)
    * @param d01 Known value at (0, 1)
    * @param d10 Known value at (1, 0)
    * @param d11 Known value at (1, 1)
    * @return
    */
  protected def unitSquareInterpolation(
                               x: Double,
                               y: Double,
                               d00: Double,
                               d01: Double,
                               d10: Double,
                               d11: Double
                             ): Double =
    d00 * (1 - x) * (1 - y) + d10 * x * (1 - y) + d01 * (1 - x) * y + d11 * x * y
}
