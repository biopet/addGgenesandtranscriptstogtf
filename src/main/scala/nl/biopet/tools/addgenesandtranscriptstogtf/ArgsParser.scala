package nl.biopet.tools.addgenesandtranscriptstogtf

import java.io.File

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('I', "input") required () valueName "<file>" action {
    (x, c) =>
      c.copy(input = x)
  } text "Input gtf file. Mandatory"
  opt[File]('o', "output") required () valueName "<file>" action {
    (x, c) =>
      c.copy(output = x)
  } text "Output gtf file. Mandatory"
}
