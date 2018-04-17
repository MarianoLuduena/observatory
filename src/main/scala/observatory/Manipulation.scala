package observatory

import observatory.helpers.SparkSpecHelper
import org.apache.spark.rdd.RDD

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
    averageRDD(temperaturess.map(iter => spark.sparkContext.parallelize(iter.toSeq)))
  }

  /**
    * @param temperatures A sequence of known temperatures over the years
    * @return An RDD of the average temperature for each location
    */
  def averageRDD(temperatures: Iterable[RDD[(Location, Temperature)]]): GridLocation => Temperature = {
    makeGrid(
      collect(
        temperatures.tail
          .foldLeft(temperatures.head) { (unioned, rdd) => unioned.union(rdd) }
          .mapValues(t => (t, 1))
          .reduceByKey((a, b) => (a._1 + b._1, a._2 + b._2))
          .mapValues(x => x._1 / x._2)
      )
    )
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

