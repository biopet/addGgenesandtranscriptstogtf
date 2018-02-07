package nl.biopet.tools.addgenesandtranscriptstogtf

import java.io.File

import nl.biopet.utils.test.tools.ToolTest
import nl.biopet.utils.io._
import nl.biopet.utils.ngs.annotation.Feature
import org.testng.annotations.Test

import scala.io.Source

class AddGenesAndTranscriptsToGtfTest extends ToolTest[Args] {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      AddGenesAndTranscriptsToGtf.main(Array())
    }
  }

  def toolCommand: AddGenesAndTranscriptsToGtf.type = AddGenesAndTranscriptsToGtf

  @Test
  def testMain(): Unit = {
    val inputFile = File.createTempFile("test.", ".gtf")
    inputFile.deleteOnExit()
    val gene1 =
      Feature("chrQ", "test", "gene", 11, 40, None, Some(true), None, Map("gene_id" -> "gene_1"))
    val transcript1 = Feature("chrQ",
      "test",
      "transcript",
      11,
      40,
      None,
      Some(true),
      None,
      Map("gene_id" -> "gene_1", "transcript_id" -> "transcript_1_1"))
    val exon1 = Feature("chrQ",
      "test",
      "exon",
      11,
      20,
      None,
      Some(true),
      None,
      Map("gene_id" -> "gene_1", "transcript_id" -> "transcript_1_1"))
    val exon2 = Feature("chrQ",
      "test",
      "exon",
      31,
      40,
      None,
      Some(true),
      None,
      Map("gene_id" -> "gene_1", "transcript_id" -> "transcript_1_1"))
    writeLinesToFile(inputFile, List("#test", exon1.asGtfLine, exon2.asGtfLine))

    val outputFile = File.createTempFile("test.", ".gtf")
    outputFile.deleteOnExit()

    AddGenesAndTranscriptsToGtf.main(
      Array("-I", inputFile.getAbsolutePath, "-o", outputFile.getAbsolutePath))

    val reader = Source.fromFile(outputFile)
    val lines = reader.getLines().toList
    reader.close()

    lines.head shouldBe "#test"
    val features = lines.filter(!_.startsWith("#")).map(Feature.fromLine)

    features shouldBe List(
      gene1,
      transcript1,
      exon1,
      exon2
    )
  }

}
