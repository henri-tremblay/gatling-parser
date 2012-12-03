gatling-parser
==============

Generates an Excel parseable report from Gatling simulation log file for all server calls and presenting the usual metrics.

Usage: App simulation.log

You can lauch it from the command line using parse.bat which calls mvn exec:java.

The result is a file called simulation-result.txt which contains one line per request in the usual Gatling details section. The columns are the same (name, execution, min, max, mean, stdDeviation, percentile95, percentile99) and separated by a tabulation. You can easily load it in Excel and play with it as easy. This report is really useful since Gatling doesn't provide yet a single view of the performance of every requests.

The last simulation.log format tested was version 1.2.3. Please, let me know in case of issue with a newer version.
