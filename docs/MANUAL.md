# Manual

## Introduction
This tool adds genes and transcripts to a GTF file, based on the exons available in an input GTF file.

## Example
To run this tool:
```bash
java -jar AddGenesAndTranscriptsToGtf-version.jar -I input.gtf -o output.gtf
```

To get help:
```bash
java -jar AddGenesAndTranscriptsToGtf-version.jar --help
General Biopet options


Options for AddGenesAndTranscriptsToGtf

Usage: AddGenesAndTranscriptsToGtf [options]

  -l, --log_level <value>  Level of log information printed. Possible levels: 'debug', 'info', 'warn', 'error'
  -h, --help               Print usage
  -v, --version            Print version
  -I, --input <file>       Input gtf file. Mandatory
  -o, --output <file>      Output gtf file. Mandatory
```

## Output
A GTF file with genes, transcripts and the original exons.
