package org.henri.gatlingparser;

import static org.henri.gatlingparser.App.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.henri.gatlingparser.App.Request;
import org.henri.gatlingparser.App.Stat;
import org.junit.Test;

public class AppTest 
{

    @Test
    public void testDeduceOutputFilename() throws Exception
    {
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
    public void testCalculateStats() throws Exception
    {
        List<Request> requests = new ArrayList<>(100);
        for (int i = 1; i <= 100; i++) {
            Request r = new Request(0, i);
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
