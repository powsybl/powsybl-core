package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;
import com.powsybl.cgmes.model.CgmesModelException;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class TransformerRegulatingControlMapping {

    public TransformerRegulatingControlMapping() {
        twoWinding = new HashMap<>();
        threeWinding = new HashMap<>();
    }

    public void add(String transformerId, RegulatingDataRatio rdRtc, RegulatingDataPhase rdPtc) {
        if (twoWinding.containsKey(transformerId)) {
            throw new CgmesModelException("Transformer already added, Transformer id : " + transformerId);
        }

        TwoWindingTransformerRegulatingData rd = new TwoWindingTransformerRegulatingData();
        rd.ratioTapChanger = rdRtc;
        rd.phaseTapChanger = rdPtc;
        twoWinding.put(transformerId, rd);
    }

    public void add(String transformerId, RegulatingDataRatio rdRtc1, RegulatingDataPhase rdPtc1,
        RegulatingDataRatio rdRtc2, RegulatingDataPhase rdPtc2, RegulatingDataRatio rdRtc3,
        RegulatingDataPhase rdPtc3) {
        if (threeWinding.containsKey(transformerId)) {
            throw new CgmesModelException("Transformer already added, Transformer id : " + transformerId);
        }

        TwoWindingTransformerRegulatingData rd1 = new TwoWindingTransformerRegulatingData();
        rd1.ratioTapChanger = rdRtc1;
        rd1.phaseTapChanger = rdPtc1;

        TwoWindingTransformerRegulatingData rd2 = new TwoWindingTransformerRegulatingData();
        rd2.ratioTapChanger = rdRtc2;
        rd2.phaseTapChanger = rdPtc2;

        TwoWindingTransformerRegulatingData rd3 = new TwoWindingTransformerRegulatingData();
        rd3.ratioTapChanger = rdRtc3;
        rd3.phaseTapChanger = rdPtc3;

        ThreeWindingTransformerRegulatingData rdThree = new ThreeWindingTransformerRegulatingData();
        rdThree.winding1 = rd1;
        rdThree.winding2 = rd2;
        rdThree.winding3 = rd3;
        threeWinding.put(transformerId, rdThree);
    }

    public RegulatingDataRatio buildEmptyRegulatingDataRatio() {
        RegulatingDataRatio rtc = new RegulatingDataRatio();
        rtc.id = null;
        rtc.regulating = false;
        rtc.regulatingControlId = null;
        rtc.side = 0;
        rtc.tculControlMode = null;
        rtc.tapChangerControlEnabled = false;

        return rtc;
    }

    public RegulatingDataRatio buildRegulatingDataRatio(String id, boolean regulating, String regulatingControlId,
        int side, String tculControlMode, boolean tapChangerControlEnabled) {
        RegulatingDataRatio rtc = new RegulatingDataRatio();
        rtc.id = id;
        rtc.regulating = regulating;
        rtc.regulatingControlId = regulatingControlId;
        rtc.side = side;
        rtc.tculControlMode = tculControlMode;
        rtc.tapChangerControlEnabled = tapChangerControlEnabled;

        return rtc;
    }

    public RegulatingDataPhase buildEmptyRegulatingDataPhase() {
        RegulatingDataPhase rtc = new RegulatingDataPhase();
        rtc.id = null;
        rtc.regulating = false;
        rtc.regulatingControlId = null;
        rtc.side = 0;

        return rtc;
    }

    public RegulatingDataPhase buildRegulatingDataPhase(String id, boolean regulating, String regulatingControlId,
        int side) {
        RegulatingDataPhase rtc = new RegulatingDataPhase();
        rtc.id = id;
        rtc.regulating = regulating;
        rtc.regulatingControlId = regulatingControlId;
        rtc.side = side;

        return rtc;
    }

    public TwoWindingTransformerRegulatingData findTwo(String transformerId) {
        return twoWinding.get(transformerId);
    }

    public ThreeWindingTransformerRegulatingData findThree(String transformerId) {
        return threeWinding.get(transformerId);
    }

    public static class RegulatingDataRatio {
        String id;
        boolean regulating;
        String regulatingControlId;
        int side;
        String tculControlMode;
        boolean tapChangerControlEnabled;
    }

    public static class RegulatingDataPhase {
        String id;
        boolean regulating;
        String regulatingControlId;
        int side;
    }

    public static class TwoWindingTransformerRegulatingData {
        RegulatingDataRatio ratioTapChanger;
        RegulatingDataPhase phaseTapChanger;
    }

    public static class ThreeWindingTransformerRegulatingData {
        TwoWindingTransformerRegulatingData winding1;
        TwoWindingTransformerRegulatingData winding2;
        TwoWindingTransformerRegulatingData winding3;
    }

    private final Map<String, TwoWindingTransformerRegulatingData> twoWinding;
    private final Map<String, ThreeWindingTransformerRegulatingData> threeWinding;
}
