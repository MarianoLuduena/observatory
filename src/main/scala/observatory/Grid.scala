package observatory

object Grid {

  def apply(temperatures: Iterable[(Location, Temperature)]): Grid = {
    val grid: Grid = new Grid

    (0 until grid.width).par.foreach { x =>
      (0 until grid.height).par.foreach { y =>
        val location: Location = Location(lat = grid.height / 2 - y, lon = x - grid.width / 2)
        grid.setValueAt(x, y, Visualization.predictTemperature(temperatures, location))
      }
    }

    grid
  }
}

sealed class Grid {

  val width: Int = 360
  val height: Int = 180
  val size: Int = width * height

  private val data: Array[Temperature] = new Array[Temperature](size)

  def setValueAt(x: Int, y: Int, value: Temperature): Unit = data.update(x + y * width, value)  // data(x + y * width) = value

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

  def +=(that: Grid): Grid = {
    (0 until size).par.foreach(i => data.update(i, data(i) + that.data(i)))
    this
  }

  def scaleTo(factor: Double): Unit = (0 until size).par.foreach(i => data.update(i, data(i) * factor))
}
