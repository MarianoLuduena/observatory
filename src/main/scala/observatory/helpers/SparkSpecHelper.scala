package observatory.helpers

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

trait SparkSpecHelper {

  protected def sparkConf: SparkConf =
    new SparkConf()
      .setAppName("observatory")
      .setMaster("local[2]")
      .set("spark.driver.memory", "1024m")
      .set("spark.executor.memory", "768m")
      .set("spark.logConf", "false")
      .set("spark.sql.shuffle.partitions", "20")

  protected def sparkSessionBuilder: SparkSession.Builder =
    SparkSession
      .builder()
      .config(sparkConf)

  protected def spark: SparkSession = sparkSessionBuilder.getOrCreate()

  protected def read(file: String): RDD[String] = spark.read.textFile(file).rdd

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
