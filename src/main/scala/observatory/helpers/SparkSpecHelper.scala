package observatory.helpers

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

trait SparkSpecHelper {

  @transient protected val sparkLogger: org.apache.log4j.Logger = {
    val l = org.apache.log4j.Logger.getLogger("org.apache.spark")
    l.setLevel(org.apache.log4j.Level.WARN)
    l
  }

  protected def sparkConf: SparkConf =
    new SparkConf()
      .setAppName("observatory")
      .setMaster("local[2]")
      .set("spark.driver.maxResultSize", "768m")
      .set("spark.driver.memory", "1g")
      .set("spark.driver.cores", "2")
      .set("spark.executor.memory", "768m")
      .set("spark.logConf", "false")
      .set("spark.sql.shuffle.partitions", "6")
      .set("spark.default.parallelism", "5")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryoserializer.buffer.max", "128m")

  protected def sparkSessionBuilder: SparkSession.Builder =
    SparkSession
      .builder()
      .config(sparkConf)

  protected lazy val spark: SparkSession = sparkSessionBuilder.getOrCreate()

  protected def read(file: String): RDD[String] = {
    val path = Option(getClass.getResource(file)).map(_.getFile).getOrElse(file)
    spark.read.textFile(path).rdd
  }

  protected def close(): Unit = spark.close()

  protected def defaultStorageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK

  protected def persist[T](rdd: RDD[T]): RDD[T] = rdd.persist(defaultStorageLevel)

  protected def blockingUnpersist: Boolean = false

  protected def collect[T](rdd: RDD[T], unpersist: Boolean = true): Array[T] = {
    val array = rdd.collect()
    if (unpersist) rdd.unpersist(blocking = blockingUnpersist)
    array
  }
}
