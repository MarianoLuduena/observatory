package observatory

import com.sksamuel.scrimage.Image
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future, duration}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  private val logger = org.apache.log4j.Logger.getLogger(getClass.getSimpleName)

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = tile.toLocation

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {

    val width = 256
    val height = 256
    val alpha = 127
    val newZoom = tile.zoom + 8
    val n = width  // 2^8 = 256

    def posToLocation(width: Int, height: Int, pos: Int): Location = {
      val newX = n * tile.x + pos % width
      val newY = n * tile.y + pos / width
      tileLocation(Tile(newX, newY, newZoom))
    }

    Visualization.visualize(temperatures, colors, width, height, alpha, posToLocation)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit
  ): Unit = {

    implicit val ec: ExecutionContextExecutor = ExecutionContext.Implicits.global

    val futures =
      for {
        (year, data) <- yearlyData
        zoom <- 0 to 3
        qTiles = scala.math.pow(4, zoom).toInt
        tileNumber <- 0 until qTiles
        sideLength = scala.math.pow(2, zoom).toInt
        (x, y) = (tileNumber % sideLength, tileNumber / sideLength)
      } yield Future { generateImage(year, Tile(x, y, zoom), data) }(ec)

    val qProcessedTiles = scala.concurrent.Future.sequence(futures).map(_.size)
    val result = Await.result(qProcessedTiles, duration.Duration(2, duration.HOURS))

    logger.info(s"Number of processed tiles: $result")
  }

}
