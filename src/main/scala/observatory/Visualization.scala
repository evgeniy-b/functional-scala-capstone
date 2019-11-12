package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val weighted = temperatures.par.map {
      case (l, temperature) =>
        val distance = location.distanceTo(l)
        if (distance <= 1) return temperature
        (1 / math.pow(distance, 2), temperature)
    }
    weighted.map { case (w, t) => t * w }.sum / weighted.map(_._1).sum
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    val (lowers, highers) = points.toSeq.sortBy(_._1).partition(_._1 < value)
    val (lTemp, lColor) = if (lowers.isEmpty) return highers.head._2 else lowers.last
    val (hTemp, hColor) = if (highers.isEmpty) return lowers.last._2 else highers.head
    val rgb = Seq((lColor.red, hColor.red), (lColor.green, hColor.green), (lColor.blue, hColor.blue)).map {
      case (lc, hc) => (lc * (hTemp - value) + hc * (value - lTemp)) / (hTemp - lTemp)
    }.map(_.round.toInt)
    Color(rgb(0), rgb(1), rgb(2))
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val locations = for {
      lat <- 90 to -89 by -1
      lon <- -180 to 179
    } yield Location(lat, lon)
    val pixels = locations.par.map(predictTemperature(temperatures, _)).map(interpolateColor(colors, _).toPixel())
    Image(360, 180, pixels.toArray)
  }

}

