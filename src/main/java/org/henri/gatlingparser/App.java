package org.henri.gatlingparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Hello world!
 *
 */
public class App
{
    private static final int TYPE = 0;
    private static final int NAME = 3;
    private static final int START = 4;
    private static final int END = 5;
    
    private static class Request {
        public Request(long start, long end) {
            this.startOfRequestSending = start;
            this.endOfResponseReceiving = end;
        }
        
        long startOfRequestSending;
        long endOfResponseReceiving;
    }
    
    private static class Stat {
        String name;
        long execution, min, max, mean, stdDeviation, percentile95, percentile99;
        
        @Override
        public String toString() {
            return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", name, execution, min, max, mean, stdDeviation, percentile95, percentile99);
        }
    }
    
    public static void main(String... args) throws Exception
    {
        if(args.length != 1) {
            System.out.println("Usage: App simulation.log");
            System.exit(1);
            return;
        }
     
        Map<String, List<Request>> requests = new HashMap<>();
        
        String filename = args[0];
        
        CSVReader reader = new CSVReader(new FileReader(filename), '\t');
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if(!"ACTION".equals(nextLine[TYPE])) {
                continue;
            }
            String name = nextLine[NAME];
            if(!name.startsWith("Request")) {
                continue;
            }
            long start = Long.parseLong(nextLine[START]);
            long end = Long.parseLong(nextLine[END]);
            List<Request> list = requests.get(name);
            if(list == null) {
                list = new ArrayList<>();
                requests.put(name, list);
            }
            Request request = new Request(start, end);
            list.add(request);
        }
        reader.close();
        
        Map<String, Stat> stats = calculateStats(requests);
        requests = null; // no need to keep it
        
        File file = new File(filename);
        String path = FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath());
        String baseName = FilenameUtils.getBaseName(filename);
        
        BufferedWriter out = new BufferedWriter(new FileWriter(path + File.separatorChar + baseName
                + "-result.txt"));
        out.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "name", "execution", "min", "max", "mean", "stdDeviation", "percentile95", "percentile99"));
        out.newLine();
        for(Map.Entry<String, Stat> entry : stats.entrySet()) {
            Stat stat = entry.getValue();
            out.write(stat.toString());
            out.newLine();
        }
        out.close();
        
    }

    private static Map<String, Stat> calculateStats( Map<String, List<Request>> requests) {
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
