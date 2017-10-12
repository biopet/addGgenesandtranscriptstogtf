package nl.biopet.tools.addgenesandtranscriptstogtf

import java.io.PrintWriter

import nl.biopet.utils.ngs.annotation.Feature
import nl.biopet.utils.tool.ToolCommand

import scala.collection.mutable.ListBuffer
import scala.io.Source

object AddGenesAndTranscriptsToGtf extends ToolCommand {
  def main(args: Array[String]): Unit = {
    val parser = new ArgsParser(toolName)
    val cmdArgs =
      parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)

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

}
