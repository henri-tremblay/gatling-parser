package org.henri.gatlingparser;

import static org.henri.gatlingparser.App.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.henri.gatlingparser.App.Request;
import org.henri.gatlingparser.App.Stat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

public class AppTest {

    static Path sampleSimulation = Paths.get("src", "test", "data", "simulation.log");
    static Path sampleSimulationResult = Paths.get("src", "test", "data", "simulation-result.txt");

    @AfterClass
    public static void after() {
        File file = sampleSimulationResult.toFile();
        if(file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testSamplefile() throws Exception {
        App.main(sampleSimulation.toString());
        try(BufferedReader in = new BufferedReader(new FileReader(sampleSimulationResult.toFile()))) {
            assertEquals("name\texecution\tmin\tmax\tmean\tstdDeviation\tpercentile95\tpercentile99", in.readLine());
            assertEquals("Mark Redirect 1\t5\t75\t191\t132\t43\t191\t191", in.readLine());
            assertEquals("Mark\t5\t35\t82\t47\t17\t82\t82", in.readLine());
        }
    }

    @Test
    public void testDeduceOutputFilename() throws Exception {
        checkFile(deduceOutputFilename("/etc/var/simulation.log"), "/etc/var/simulation-result.txt");
        checkFile(deduceOutputFilename("C:\\Documents\\simulation.log"),
                "C:\\Documents\\simulation-result.txt");
        checkFile(deduceOutputFilename("simulation.log"), "simulation-result.txt");
        checkFile(deduceOutputFilename("../simulation.log"), "../simulation-result.txt");
        checkFile(deduceOutputFilename("var/simulation.log"), "var/simulation-result.txt");
    }
    
    private void checkFile(String actual, String expected) {
        assertEquals(FilenameUtils.normalize(expected), actual);
    }

    @Test
    public void testCalculateStats() throws Exception {
        List<Request> requests = new ArrayList<>(100);
        for (int i = 1; i <= 100; i++) {
            Request r = new Request("", 0, i);
            requests.add(r);
        }

        Map<String, Stat> stats = calculateStats(Collections.singletonMap("foo", requests));

        Stat stat = stats.get("foo");
        assertEquals("foo", stat.name);
        assertEquals(100, stat.execution);
        assertEquals(1, stat.min);
        assertEquals(100, stat.max);
        assertEquals(50, stat.mean);
        assertEquals(95, stat.percentile95);
        assertEquals(99, stat.percentile99);
        assertEquals(28, stat.stdDeviation);
    }
}
