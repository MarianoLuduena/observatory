package observatory

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait ManipulationTest extends FunSuite with Checkers {

  test("Deviation") {
    val normals = Manipulation.average(
      Iterable(
        Iterable((Location(0, 0), 1.0), (Location(-1, 0), 1.0), (Location(0, 1), 1.0), (Location(1, 1), 1.0))
      )
    )

    val ts = Iterable((Location(0, 0), 2.0), (Location(-1, 0), 0.0), (Location(0, 1), 1.0), (Location(1, 1), -1.0))
    val dv = Manipulation.deviation(ts, normals)
    println(dv(GridLocation(0, 0)))
  }

}