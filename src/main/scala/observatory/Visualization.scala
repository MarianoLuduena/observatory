package observatory

import com.sksamuel.scrimage.Image
import observatory.helpers.InterpolationHelper

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends InterpolationHelper {

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val samples = temperatures.par.map(t => (gcDistance(t._1, location), t._2))
    idw(samples)
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    val (lowerPoints, upperPoints) = points.toArray.sortBy(_._1).partition(_._1 < value)
    (lowerPoints.lastOption, upperPoints.headOption) match {
      case (_, Some(up)) if up._1 == value => up._2
      case (Some(lp), Some(up)) => interpolateColor(lp, up, value)
      case (Some(lp), _) => lp._2
      case (_, Some(up)) => up._2
      case _ => Color(0, 0, 0)  // black
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val width: Int = 360
    val height: Int = 180

    def posToLocation(width: Int, height: Int, pos: Int): Location = {
      val latitude = 90 - 180.0 / height * (pos / width)
      val longitude = -180 + 360.0 / width * (pos % width)
      Location(lat = latitude, lon = longitude)
    }

    visualize(temperatures, colors, width, height, 255, posToLocation)
  }

  /**
    * Generalization of the visualize method for an image of parametric size
    *
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param width Image's width
    * @param height Image's height
    * @param alpha Parameter that determines the transparency of each and every pixel in the image
    * @return A width×height image where each pixel shows the predicted temperature at its location
    */
  def visualize(
                 temperatures: Iterable[(Location, Temperature)],
                 colors: Iterable[(Temperature, Color)],
                 width: Int,
                 height: Int,
                 alpha: Int,
                 posToLocation: (Int, Int, Int) => Location
               ): Image = {

    val pixels = (0 until (width * height)).par.map { x =>
      val predictedTemperature = predictTemperature(temperatures, posToLocation(width, height, x))
      val interpolatedColour = interpolateColor(colors, predictedTemperature)
      interpolatedColour.toPixel(alpha)
    }

    Image(width, height, pixels.toArray)
  }

}

