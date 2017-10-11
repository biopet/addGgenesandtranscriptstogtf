package nl.biopet.tools.addgenesandtranscriptstogtf

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

object AddGenesAndTranscriptsToGtfTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      AddGenesAndTranscriptsToGtf.main(Array())
    }
  }
}
