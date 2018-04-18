package observatory

import observatory.helpers.SparkSpecHelper

/**
  * 4th milestone: value-added information
  */
object Manipulation extends SparkSpecHelper {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    Grid(temperatures).getTemperature
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val grid =
      temperaturess.map(t => (Grid.apply(t), 1)).reduce((t1, t2) => (t1._1 += t2._1, t1._2 + t2._2)) match {
        case (g, count) => g.scaleTo(1.0 / count); g
      }
    grid.getTemperature
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val grid = makeGrid(temperatures)
    gl => grid(gl) - normals(gl)
  }


}

