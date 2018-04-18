package observatory

import com.sksamuel.scrimage.Pixel
import scala.collection.parallel.mutable.ParArray
import scala.math.{Pi, atan, pow, sinh}

/**
  * Introduced in Week 1. Represents a location on the globe.
  * @param lat Degrees of latitude, -90 ≤ lat ≤ 90
  * @param lon Degrees of longitude, -180 ≤ lon ≤ 180
  */
case class Location(lat: Double, lon: Double)

/**
  * Introduced in Week 3. Represents a tiled web map tile.
  * See https://en.wikipedia.org/wiki/Tiled_web_map
  * Based on http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
  * @param x X coordinate of the tile
  * @param y Y coordinate of the tile
  * @param zoom Zoom level, 0 ≤ zoom ≤ 19
  */
case class Tile(x: Int, y: Int, zoom: Int) {

  def toLocation: Location = {
    val n = pow(2, zoom)
    val longitude = x.toDouble / n * 360 - 180
    val latitude = atan(sinh(Pi - y.toDouble / n * 2 * Pi)) * 180 / Pi

    Location(lat = latitude, lon = longitude)
  }
}

/**
  * Introduced in Week 4. Represents a point on a grid composed of
  * circles of latitudes and lines of longitude.
  * @param lat Circle of latitude in degrees, -89 ≤ lat ≤ 90
  * @param lon Line of longitude in degrees, -180 ≤ lon ≤ 179
  */
case class GridLocation(lat: Int, lon: Int)

/**
  * Introduced in Week 5. Represents a point inside of a grid cell.
  * @param x X coordinate inside the cell, 0 ≤ x ≤ 1
  * @param y Y coordinate inside the cell, 0 ≤ y ≤ 1
  */
case class CellPoint(x: Double, y: Double)

/**
  * Introduced in Week 2. Represents an RGB color.
  * @param red Level of red, 0 ≤ red ≤ 255
  * @param green Level of green, 0 ≤ green ≤ 255
  * @param blue Level of blue, 0 ≤ blue ≤ 255
  */
case class Color(red: Int, green: Int, blue: Int) {

  def toPixel(alphaChannel: Int = 255): Pixel = Pixel(red, green, blue, alphaChannel)
}

object Grid {

  def apply(temperatures: Iterable[(Location, Temperature)]): Grid = {
    val grid: Grid = new Grid

    (0 until grid.width).par.foreach { x =>
      (0 until grid.height).par.foreach { y =>
        val location: Location = Location(lat = y - grid.height / 2, lon = x - grid.width / 2)
        grid.setValueAt(x, y, Visualization.predictTemperature(temperatures, location))
      }
    }

    grid
  }
}

sealed class Grid {

  val width: Int = 360
  val height: Int = 180

  private val data: ParArray[Temperature] = new ParArray[Temperature](width * height)

  def setValueAt(x: Int, y: Int, value: Temperature): Unit = data(x + y * width) = value

  /**
    * The latitude can be any integer between -89 and 90, and the longitude can be any integer between -180 and 179.
    * The top-left corner has coordinates (90, -180), and the bottom-right corner has coordinates (-89, 179).
    *
    * @param gl GridLocation
    * @return The Temperature at the given GridLocation
    */
  def getTemperature(gl: GridLocation): Temperature = {
    val x: Int = gl.lon + width / 2
    val y: Int = height / 2 - gl.lat

    data(x + y * width)
  }
}
