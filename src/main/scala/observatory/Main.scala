package observatory

object Main extends App {
  val data = Extraction.locateTemperatures(2015, "/stations.csv", "/1975.csv")
  val rows = Extraction.locationYearlyAverageRecords(data)
  println(rows.head)
  println("Total: " + rows.size)
//  Visualization.visualize(rows, temperatureColors).output("image.png")
  Interaction.tile(rows, temperatureColors, Tile(0,0,0)).output("image.png")
}
