package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.helpers.InterpolationHelper

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 extends InterpolationHelper {

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    val (x, y) = (point.x, point.y)
    unitSquareInterpolation(x, y, d00, d01, d10, d11)
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {

    val width = 256
    val height = 256
    val alpha = 127
    val newZoom = tile.zoom + 8
    val n = width  // 2^8 = 256

    val pixels = (0 until width * height).par.map { pos =>
      val (x, y, d00, d01, d10, d11) = {
        val newX = n * tile.x + pos % width
        val newY = n * tile.y + pos / width
        val location = Tile(newX, newY, newZoom).toLocation
        (
          location.lon - location.lon.floor,
          location.lat - location.lat.floor,
          grid(GridLocation(location.lat.floor.toInt, location.lon.floor.toInt)),
          grid(GridLocation(location.lat.ceil.toInt, location.lon.floor.toInt)),
          grid(GridLocation(location.lat.floor.toInt, location.lon.ceil.toInt)),
          grid(GridLocation(location.lat.ceil.toInt, location.lon.ceil.toInt))
        )
      }

      val interpolatedTemperature = bilinearInterpolation(CellPoint(x, y), d00, d01, d10, d11)
      Visualization.interpolateColor(colors, interpolatedTemperature).toPixel(alpha)
    }

    Image(width, height, pixels.toArray)
  }

}
