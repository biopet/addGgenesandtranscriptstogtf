/*
 * Copyright (c) 2017 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

  def toolCommand: AddGenesAndTranscriptsToGtf.type =
    AddGenesAndTranscriptsToGtf

  @Test
  def testMain(): Unit = {
    val inputFile = File.createTempFile("test.", ".gtf")
    inputFile.deleteOnExit()
    val gene1 =
      Feature("chrQ",
              "test",
              "gene",
              11,
              40,
              None,
              Some(true),
              None,
              Map("gene_id" -> "gene_1"))
    val transcript1 = Feature(
      "chrQ",
      "test",
      "transcript",
      11,
      40,
      None,
      Some(true),
      None,
      Map("gene_id" -> "gene_1", "transcript_id" -> "transcript_1_1"))
    val exon1 = Feature(
      "chrQ",
      "test",
      "exon",
      11,
      20,
      None,
      Some(true),
      None,
      Map("gene_id" -> "gene_1", "transcript_id" -> "transcript_1_1"))
    val exon2 = Feature(
      "chrQ",
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
