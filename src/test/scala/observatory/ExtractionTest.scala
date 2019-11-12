package observatory

import java.time.LocalDate

import org.scalactic.TolerantNumerics
import org.scalatest.FunSuite

trait ExtractionTest extends FunSuite {
  implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.0001)
  lazy private val data = Extraction.locateTemperatures(2015, "/stations.csv", "/2015.csv")

  test("test data is loaded as expected") {
    def find(date: LocalDate, location: Location): Temperature = data.find(i => i._1 == date && i._2 == location).get._3

    assert(data.size === 3)
    assert(find(LocalDate.of(2015, 8, 11), Location(37.35, -78.433)) === 27.3)
    assert(find(LocalDate.of(2015, 12, 6), Location(37.358, -78.438)) === 0.0)
    assert(find(LocalDate.of(2015, 1, 29), Location(37.358, -78.438)) === 2.0)
  }

  test("test data is aggregated as expected") {
    val aggregate = Extraction.locationYearlyAverageRecords(data).toMap
    assert(aggregate.size === 2)
    assert(aggregate(Location(37.35, -78.433)) === 27.3)
    assert(aggregate(Location(37.358, -78.438)) === 1.0)
  }
}
