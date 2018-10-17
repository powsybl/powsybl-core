/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ReportTapChangers {

    public ReportTapChangers(CgmesModel cgmes, Consumer<String> output) {
        this.cgmes = cgmes;
        this.output = output;
        Path p = (Path) cgmes.getProperties().get("dataSource");
        String ps = "";
        if (p != null) {
            ps = p.toString().replaceAll("../data", "");
        }
        this.dataSource = ps;
        this.modelId = cgmes.modelId();
        this.txTapChangers = new HashMap<>();
        this.txEnds = new HashMap<>();
        this.endTx = new HashMap<>();

        ReportRow d = new ReportRow("TapChangerHeader");
        d.col("dataSource");
        d.col("modelId");
        d.col("txId");
        d.col("txName");
        d.col("numEnds (windings)");
        d.col("ratedU1");
        d.col("r1");
        d.col("x1");
        d.col("g1");
        d.col("b1");
        d.col("ratedU2");
        d.col("r2");
        d.col("x2");
        d.col("g2");
        d.col("b2");
        d.col("ratedU3");
        d.col("r3");
        d.col("x3");
        d.col("g3");
        d.col("b3");
        d.col("numTapChangers");
        d.col("sameSteps");
        d.col("numRatioTapChangersEnd1");
        d.col("numPhaseTapChangersEnd1");
        d.col("numRatioTapChangersEnd2");
        d.col("numPhaseTapChangersEnd2");
        d.col("numRatioTapChangersEnd3");
        d.col("numPhaseTapChangersEnd3");
        for (int k = 0; k < 6; k++) {
            d.col("tapChangerId");
            d.col("end");
            d.col(CgmesNames.LOW_STEP);
            d.col(CgmesNames.HIGH_STEP);
            d.col("neutralStep");
            d.col("step");
            d.col("atNeutral");
            d.col("regulating");
        }
        d.end(output);
    }

    private void addTapChanger(PropertyBag tc) {
        String txId = transformerId(tc);
        if (txId == null) {
            ReportRow d = new ReportRow("TapChangerError");
            d.col(dataSource);
            d.col(modelId);
            d.col(tcId(tc));
            d.col(txId);
            d.col("Missing Transformer");
            d.end(output);
            return;
        }
        List<PropertyBag> tcs = txTapChangers.computeIfAbsent(txId, t -> new ArrayList<>());
        tcs.add(tc);
    }

    private void addEnd(PropertyBag end) {
        String txId = end.getId("PowerTransformer");
        List<PropertyBag> ends = txEnds.computeIfAbsent(txId, t -> new ArrayList<>());
        ends.add(end);
        String endId = end.getId(CgmesNames.TRANSFORMER_END);
        endTx.put(endId, txId);
    }

    void report() {

        // For every transformer, add all its ends and tap changers
        cgmes.transformerEnds().forEach(this::addEnd);
        cgmes.ratioTapChangers().forEach(this::addTapChanger);
        cgmes.phaseTapChangers().forEach(this::addTapChanger);

        txEnds.keySet().forEach(txId -> {
            List<PropertyBag> ends = txEnds.get(txId);
            List<PropertyBag> tcs = txTapChangers.get(txId);

            ReportRow d = new ReportRow("TapChanger");
            d.col(dataSource);
            d.col(modelId);
            d.col(txId);
            d.col(txName(txId));
            d.col(ends.size());

            int k;
            for (k = 0; k < ends.size(); k++) {
                PropertyBag end = ends.get(k);
                d.col(end.asDouble("ratedU"));
                d.col(end.asDouble("r"));
                d.col(end.asDouble("x"));
                d.col(end.asDouble("g"));
                d.col(end.asDouble("b"));
            }
            for (; k < 3; k++) {
                d.col("-");
                d.col("-");
                d.col("-");
                d.col("-");
                d.col("-");
            }

            if (tcs == null) {
                // num tap changers is zero
                d.col(0);
            } else {
                long rtc1 = tcs.stream().filter(tc -> end(tc) == 1 && isRatio(tc)).count();
                long ptc1 = tcs.stream().filter(tc -> end(tc) == 1 && isPhase(tc)).count();
                long rtc2 = tcs.stream().filter(tc -> end(tc) == 2 && isRatio(tc)).count();
                long ptc2 = tcs.stream().filter(tc -> end(tc) == 2 && isPhase(tc)).count();
                long rtc3 = tcs.stream().filter(tc -> end(tc) == 3 && isRatio(tc)).count();
                long ptc3 = tcs.stream().filter(tc -> end(tc) == 3 && isPhase(tc)).count();
                List<Integer> steps = tcs.stream().map(this::steps).collect(Collectors.toList());
                boolean sameSteps = steps.isEmpty()
                        || steps.stream().allMatch(steps.get(0)::equals);
                d.col(tcs.size());
                d.col(sameSteps);
                d.col(rtc1);
                d.col(ptc1);
                d.col(rtc2);
                d.col(ptc2);
                d.col(rtc3);
                d.col(ptc3);
                tcs.forEach(tc -> {
                    String tcId = tcId(tc);
                    int lowStep = tc.asInt(CgmesNames.LOW_STEP);
                    int highStep = tc.asInt(CgmesNames.HIGH_STEP);
                    int neutralStep = tc.asInt("neutralStep");
                    int step = (int) tc.asDouble("SVtapStep", neutralStep);
                    boolean atNeutral = neutralStep == step;
                    boolean regulating = tc.asBoolean("regulatingControlEnabled", false);
                    d.col(tcId);
                    d.col(end(tc));
                    d.col(lowStep);
                    d.col(highStep);
                    d.col(neutralStep);
                    d.col(step);
                    d.col(atNeutral);
                    d.col(regulating);
                });
            }
            d.end(output);
        });
    }

    private String transformerId(PropertyBag tc) {
        String endId = tc.getId(CgmesNames.TRANSFORMER_END);
        return endTx.get(endId);
    }

    private int end(PropertyBag tc) {
        String endId = tc.getId(CgmesNames.TRANSFORMER_END);
        String txId = endTx.get(endId);
        List<PropertyBag> txe = txEnds.get(txId);
        PropertyBag end = txe.stream()
                .filter(e -> e.getId(CgmesNames.TRANSFORMER_END).equals(endId))
                .reduce((a, b) -> {
                    throw new ConversionException("Multiple transformerEnds with id: " + endId);
                })
                .orElseThrow(() -> new ConversionException("No transformerEnd with id: " + endId));
        int endNumber = end.asInt("terminalSequenceNumber", -1);
        if (endNumber == -1) {
            return end.asInt("endNumber", -1);
        }
        return endNumber;
    }

    private String txName(String txId) {
        List<PropertyBag> txe = txEnds.get(txId);
        PropertyBag end = txe.get(0);
        return end.getLocal("name");
    }

    private boolean isRatio(PropertyBag tc) {
        return tc.containsKey("RatioTapChanger");
    }

    private int steps(PropertyBag tc) {
        int lowStep = tc.asInt(CgmesNames.LOW_STEP);
        int highStep = tc.asInt(CgmesNames.HIGH_STEP);
        return highStep - lowStep + 1;
    }

    private boolean isPhase(PropertyBag tc) {
        return tc.containsKey("PhaseTapChanger");
    }

    private String tcId(PropertyBag tc) {
        return isRatio(tc) ? tc.getId("RatioTapChanger") : tc.getId("PhaseTapChanger");
    }

    private final CgmesModel cgmes;
    private final Consumer<String> output;
    private final String dataSource;
    private final String modelId;

    private final Map<String, List<PropertyBag>> txTapChangers;
    private final Map<String, List<PropertyBag>> txEnds;
    private final Map<String, String> endTx;
}
