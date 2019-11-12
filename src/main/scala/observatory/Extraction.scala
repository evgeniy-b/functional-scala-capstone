package observatory

import java.time.LocalDate
import java.nio.file.Paths
import java.sql.Date
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql._
import org.apache.spark.sql.types._

/**
  * 1st milestone: data extraction
  */
object Extraction {
  import org.apache.spark.sql.SparkSession
  import org.apache.spark.sql.functions._

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Weather observations")
      .config("spark.master", "local")
      .getOrCreate()

  import spark.implicits._

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    readData(year, stationsFile, temperaturesFile).collect().map(row =>
      (row.getAs[Date]("date").toLocalDate, Location(row.getAs[Double]("lat"), row.getAs[Double]("lon")), row.getAs[Temperature]("temperature"))
    )
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    records.par.groupBy(_._2).mapValues(items => items.map(_._3).sum / items.size).toSeq.seq
  }


  private def readData(year: Year, stationsFile: String, temperaturesFile: String) = {
    val stations = readStations(stationsFile).where($"lat".isNotNull && $"lon".isNotNull)
    val temperatures = readTemperatures(year, temperaturesFile)
    stations
      .join(temperatures, stations("stn") <=> temperatures("stn") && stations("wban") <=> temperatures("wban"))
      .select(temperatures("date"), stations("lat"), stations("lon"), temperatures("temperature"))
  }

  private def readStations(file: String) = {
    val schema = StructType(Seq(
      StructField("stn", StringType, true),
      StructField("wban", StringType, true),
      StructField("lat", DoubleType, true),
      StructField("lon", DoubleType, true)
    ))
    spark.read.format("com.databricks.spark.csv").option("delimiter", ",").schema(schema).load(fsPath(file))
  }

  private def readTemperatures(year: Int, file: String) = {
    val schema = StructType(Seq(
      StructField("stn", StringType, true),
      StructField("wban", StringType, true),
      StructField("date", DateType, false),
      StructField("temperature", DoubleType, false)
    ))

    val rdd = spark.sparkContext.textFile(fsPath(file))
    val data = rdd.map(_.split(",")).map { row =>
      val stn = if (row(0).isEmpty) null else row(0)
      val wban = if (row(1).isEmpty) null else row(1)
      val date = Date.valueOf(LocalDate.of(year, row(2).toInt, row(3).toInt))
      val temperature = fromFarenheit(row(4).toDouble)
      Row.fromSeq(Seq(stn, wban, date, temperature))
    }

    spark.createDataFrame(data, schema)
  }

  private def fsPath(resource: String): String = Paths.get(getClass.getResource(resource).toURI).toString
}
