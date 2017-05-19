package org.henri.gatlingparser;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App
{
    enum GatlingSimulationRecordFormat {
        //https://groups.google.com/forum/#!topic/gatling/mbvN5CBDK4w
        //[scenario][userId][recordType][groupHierarchy][name][first/last byte sent timestamp][first/last byte received timestamp][status][extraInfo]
        v1 (2, 4, 5, 8, 9),
        // From io.gatling.core.stats.writer
        //fast"${RequestRecordHeader.value}][scenario][userId][{serializeGroups(groupHierarchy)}][name][startTimestamp][endTimestamp][status][{serializeMessage(message)}${serializeExtraInfo(extraInfo)}$
        v2 (0, 4, 5, 6, 7);

        int typeIndex, nameIndex, requestStartIndex, responseEndIndex, statusIndex;

        GatlingSimulationRecordFormat(int typeIndex,int nameIndex,int requestStartIndex, int  responseEndIndex,int statusIndex) {
          this.typeIndex         = typeIndex;
          this.nameIndex         = nameIndex;
          this.requestStartIndex = requestStartIndex;
          this.responseEndIndex  = responseEndIndex;
          this.statusIndex       = statusIndex;
        }

    }

    static class Request {
        String name;
        long startOfRequestSending;
        long endOfResponseReceiving;

        public Request(String name, long start, long end) {
            this.name = name;
            this.startOfRequestSending = start;
            this.endOfResponseReceiving = end;
        }


        public String toString() {
            return name + ": " + endOfResponseReceiving + " - " + startOfRequestSending + " = " + (endOfResponseReceiving - startOfRequestSending);
        }
    }
    
    static class Stat {
        String name;
        long execution, min, max, mean, stdDeviation, percentile95, percentile99;
        
        @Override
        public String toString() {
            return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", name, execution, min, max, mean, stdDeviation, percentile95, percentile99);
        }
    }
    
    public static void main(String... args) throws Exception
    {
        String filename = "undefined";
        GatlingSimulationRecordFormat  recordFormat = GatlingSimulationRecordFormat.v1;

        switch (args.length) {
            case 1 : filename = args[0]; break;
            case 2 : filename = args[0]; recordFormat = GatlingSimulationRecordFormat.valueOf(args[1]); break;
            default :
                     System.out.println("Usage: App simulation.log [v1|v2]");
                     System.exit(1);
                     return;
        }
     
        Map<String, List<Request>> requests = new HashMap<>();
        
        try(CSVReader reader = new CSVReader(new FileReader(filename), '\t')) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (!"REQUEST".equals(nextLine[recordFormat.typeIndex])) {
                    continue;
                }
                // Skip failed requests
                if (!"OK".equals(nextLine[recordFormat.statusIndex])) {
                    continue;
                }
                String name = nextLine[recordFormat.nameIndex];
                long start = Long.parseLong(nextLine[recordFormat.requestStartIndex]);
                long end = Long.parseLong(nextLine[recordFormat.responseEndIndex]);
                List<Request> list = requests.get(name);
                if (list == null) {
                    list = new ArrayList<>();
                    requests.put(name, list);
                }
                Request request = new Request(name, start, end);
//                System.out.println(request);
                list.add(request);
            }
        }

        Map<String, Stat> stats = calculateStats(requests);
        requests = null; // no need to keep it
        
        String outputFilename = deduceOutputFilename(filename);
        
        try(BufferedWriter out = Files.newBufferedWriter(Paths.get(outputFilename))) {
            out.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "name", "execution", "min", "max", "mean", "stdDeviation", "percentile95", "percentile99"));
            out.newLine();
            try {
                stats.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .forEachOrdered(stat -> {
                        try {
                            out.write(stat.toString());
                            out.newLine();
                        }
                        catch(IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            }
            catch(UncheckedIOException e) {
                throw e.getCause();
            }
        }
        
    }

    static String deduceOutputFilename(String filename) {
        String path = FilenameUtils.getFullPathNoEndSeparator(filename);
        String baseName = FilenameUtils.getBaseName(filename);
        return FilenameUtils.concat(path, baseName + "-result.txt");
    }

    static Map<String, Stat> calculateStats(Map<String, List<Request>> requests) {
        Map<String, Stat> stats = new HashMap<>();
        Min min = new Min();
        Max max = new Max();
        Mean mean = new Mean();
        Percentile percentile = new Percentile();
        StandardDeviation stdDev = new StandardDeviation(false);
        for( Map.Entry<String, List<Request>> entry : requests.entrySet()) {
            List<Request> request = entry.getValue();
            double[] times = new double[request.size()];
            for(int i = 0; i < request.size(); i++) {
                times[i] = request.get(i).endOfResponseReceiving - request.get(i).startOfRequestSending;
            }
            Stat stat = new Stat();
            stat.name = entry.getKey();
            stat.execution = request.size();
            stat.min = (long) min.evaluate(times);
            stat.max = (long) max.evaluate(times);
            stat.mean = (long) mean.evaluate(times);
            percentile.setData(times);
            percentile.setQuantile(95);
            stat.percentile95 = (long) percentile.evaluate();
            percentile.setQuantile(99);
            stat.percentile99 = (long) percentile.evaluate();
            stats.put(entry.getKey(), stat);
            stat.stdDeviation = (long) stdDev.evaluate(times, stat.mean);
        }
        return stats;
    }
}
