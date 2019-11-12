package observatory


import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait VisualizationTest extends FunSuite with Checkers {
  test("Antipodes returns true for antipodes locations") {
    val loc1 = new Location( 48.6, 7.8)
    val loc2 = new Location( -48.6, 7.8)
    assert(loc1.isAntipode(loc2) === true)
    assert(loc2.isAntipode(loc1) === true)
  }

  test("Distance increases with different latitudes") {
    val loc1 = new Location( 0, 7.8)
    val loc2 = new Location( 20, 7.8)
    val loc3 = new Location( 40, 7.8)
    assert(loc1.distanceTo(loc2) < loc1.distanceTo(loc3))
  }

  test("Distance revolves around the world") {
    // same hemisphere
    val locNorth1 = new Location( 80, 60)
    val locNorth2 = new Location( 88, -170)
    val locInNorth = new Location( 87, 60)
    assert(locNorth1.distanceTo(locInNorth) > locNorth2.distanceTo(locInNorth))

    // longitude loops at 180

    // same hemisphere
    val locEast = new Location( 2, -179)
    val locWest = new Location( 2, 175)
    val locinWest = new Location( 2, 178)
    assert(locWest.distanceTo(locinWest) > locEast.distanceTo(locinWest))
  }

  test("Location distance") {
    val berlin = Location(52.520008, 13.404954)
    val moscow = Location(55.751244, 37.618423)

    assert(berlin.distanceTo(moscow).toInt === 1610)
    assert(moscow.distanceTo(berlin).toInt === 1610)
  }


  test("Color interpolation 1") {
    val scale = List((0.0, Color(255,0,0)), (2.147483647E9, Color(0,0,255)))
    val value = 1.0737418235E9
    assert(Visualization.interpolateColor(scale, value) === Color(128,0,128))
  }

  test("Color interpolation 2") {
    val scale = List((-1.0, Color(255,0,0)), (0.0, Color(0,0,255)))
    val value = -0.75
    assert(Visualization.interpolateColor(scale, value) === Color(191,0,64))
  }
}

//
//visualize
//[Observed Error] GeneratorDrivenPropertyCheckFailedException was thrown during property evaluation.
//(VisualizationTest.scala:133)
//Falsified after 0 successful property evaluations.
//Location: (VisualizationTest.scala:133)
//Occurred when passed generated values (
//arg0 = -36.756320385817155,
//arg1 = -81.78275035289676
//)
//Label of failing property:
//Incorrect computed color at Location(-27.0,-180.0): Color(149,0,106). Expected to be closer to Color(0,0,255) than Color(255,0,0)
//[Lost Points] 5

//(VisualizationTest.scala:133)
//Falsified after 0 successful property evaluations.
//Location: (VisualizationTest.scala:133)
//Occurred when passed generated values (
//arg0 = 100.0,
//arg1 = 22.431927224761637
//)
//Label of failing property:
//Incorrect computed color at Location(-27.0,-180.0): Color(149,0,106). Expected to be closer to Color(0,0,255) than Color(255,0,0)
//[Lost Points] 5
