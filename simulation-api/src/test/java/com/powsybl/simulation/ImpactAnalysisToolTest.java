package com.powsybl.simulation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.simulation.securityindexes.SecurityIndex;
import org.junit.Test;

import java.io.PrintStream;

public class ImpactAnalysisToolTest {
    @Test
    public void prettyPrintTest() {
        ImpactAnalysisTool impactAnalysisTool = new ImpactAnalysisTool();

        Multimap<String, SecurityIndex> securityIndexesPerContingency =  ArrayListMultimap.create();
        PrintStream out = new PrintStream(System.out);
        impactAnalysisTool.prettyPrint(securityIndexesPerContingency, out);
    }
}
