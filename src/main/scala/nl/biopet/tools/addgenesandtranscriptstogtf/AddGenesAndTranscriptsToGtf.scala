/*
 * Copyright (c) 2017 Sequencing Analysis Support Core - Leiden University Medical Center
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

import java.io.PrintWriter

import nl.biopet.utils.ngs.annotation.Feature
import nl.biopet.utils.tool.ToolCommand

import scala.collection.mutable.ListBuffer
import scala.io.Source

object AddGenesAndTranscriptsToGtf extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val reader = Source.fromFile(cmdArgs.input)

    val header = new ListBuffer[String]()

    val genes = reader
      .getLines()
      .filter { line =>
        if (line.startsWith("#")) header += line
        !line.startsWith("#")
      }
      .map(Feature.fromLine)
      .toTraversable
      .groupBy(_.attributes.get("gene_id"))

    val writer = new PrintWriter(cmdArgs.output)
    header.foreach(writer.println)

    for ((geneN, features) <- genes) {
      geneN match {
        case Some(geneName) =>
          val (geneStart, geneEnd) =
            features.foldLeft(
              (features.head.minPosition, features.head.maxPosition)) {
              case (a, b) =>
                (if (a._1 < b.minPosition) a._1 else b.minPosition,
                 if (a._2 > b.maxPosition) a._2 else b.maxPosition)
            }
          val gene = Feature(features.head.contig,
                             features.head.source,
                             "gene",
                             geneStart,
                             geneEnd,
                             None,
                             features.head.strand,
                             None,
                             Map("gene_id" -> geneName))
          writer.println(gene.asGtfLine)
          val transcriptFeatures =
            features.groupBy(_.attributes.get("transcript_id"))

          for ((transcriptN, transFeatures) <- transcriptFeatures) {
            transcriptN match {
              case Some(transcriptName) =>
                val (transStart, transEnd) = transFeatures.foldLeft(
                  (transFeatures.head.minPosition,
                   transFeatures.head.maxPosition)) {
                  case (a, b) =>
                    (if (a._1 < b.minPosition) a._1 else b.minPosition,
                     if (a._2 > b.maxPosition) a._2 else b.maxPosition)
                }
                val transcript = Feature(
                  features.head.contig,
                  features.head.source,
                  "transcript",
                  geneStart,
                  geneEnd,
                  None,
                  features.head.strand,
                  None,
                  Map("gene_id" -> geneName, "transcript_id" -> transcriptName)
                )
                writer.println(transcript.asGtfLine)

              case _ => transFeatures.foreach(f => writer.println(f.asGtfLine))
            }
            transFeatures.foreach(f => writer.println(f.asGtfLine))
          }
        case _ => features.foreach(f => writer.println(f.asGtfLine))
      }
    }

    writer.close()

    logger.info("Done")
  }

  def descriptionText: String =
    s"""
      |
      |This tool repairs a broken GTF file that does not contain all genes and transcripts.
      |A proper GTF file lists genes, transcripts and exons. However some tools only add exons to the GTF file.
      |$toolName adds genes and transcripts to a GTF file, based on the exons available in an input GTF file.
      |
    """.stripMargin

  def manualText: String =
    s"""
       |$toolName determines the genes and transcripts from the listed exons in the input GTF.
       |Only an input and an output GTF need to be passed to the command line.
     """.stripMargin

  def exampleText: String =
    s"""To create a GTF file with genes and transcripts from a GTF file with missing genes and transcripts:
       |${example("-I", "input.gtf", "-o", "output.gtf")}
     """.stripMargin
}
