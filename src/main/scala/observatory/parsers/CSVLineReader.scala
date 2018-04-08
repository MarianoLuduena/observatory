package observatory.parsers

class CSVLineReader(line: String, sep: String = ",", trim: Boolean = true) {

  private val iter: Iterator[String] = line.split(sep).iterator

  def readStr: String = if (trim) iter.next().trim else iter.next()

  def readDouble: Double = readStr.toDouble

  def readInt: Int = readStr.toInt

}
