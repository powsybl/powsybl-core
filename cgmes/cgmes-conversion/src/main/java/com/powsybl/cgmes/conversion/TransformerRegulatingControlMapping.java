package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;
import com.powsybl.cgmes.model.CgmesModelException;

public class TransformerRegulatingControlMapping {

    public TransformerRegulatingControlMapping() {
        twoWinding = new HashMap<>();
        threeWinding = new HashMap<>();
    }

    public void add(String transformerId, RegulatingDataTapChanger rdRtc, RegulatingDataTapChanger rdPtc) {
        if (twoWinding.containsKey(transformerId)) {
            throw new CgmesModelException("Transformer already added, Transformer id : " + transformerId);
        }

        RegulatingData rd = new RegulatingData();
        rd.ratioTapChanger = rdRtc;
        rd.phaseTapChanger = rdPtc;
        twoWinding.put(transformerId, rd);
    }

    public void add(String transformerId, RegulatingDataTapChanger rdRtc1, RegulatingDataTapChanger rdPtc1,
        RegulatingDataTapChanger rdRtc2, RegulatingDataTapChanger rdPtc2, RegulatingDataTapChanger rdRtc3,
        RegulatingDataTapChanger rdPtc3) {
        if (threeWinding.containsKey(transformerId)) {
            throw new CgmesModelException("Transformer already added, Transformer id : " + transformerId);
        }

        RegulatingData rd1 = new RegulatingData();
        rd1.ratioTapChanger = rdRtc1;
        rd1.phaseTapChanger = rdPtc1;

        RegulatingData rd2 = new RegulatingData();
        rd1.ratioTapChanger = rdRtc2;
        rd1.phaseTapChanger = rdPtc2;

        RegulatingData rd3 = new RegulatingData();
        rd1.ratioTapChanger = rdRtc3;
        rd1.phaseTapChanger = rdPtc3;

        RegulatingDataThree rdThree = new RegulatingDataThree();
        rdThree.winding1 = rd1;
        rdThree.winding2 = rd2;
        rdThree.winding3 = rd3;
        threeWinding.put(transformerId, rdThree);
    }

    public RegulatingDataTapChanger buildEmptyRegulatingDataTapChanger() {
        RegulatingDataTapChanger rtc = new RegulatingDataTapChanger();
        rtc.regulating = false;
        rtc.regulatingControlId = null;

        return rtc;
    }

    public RegulatingDataTapChanger buildRegulatingDataTapChanger(boolean regulating, String regulatingControlId) {
        RegulatingDataTapChanger rtc = new RegulatingDataTapChanger();
        rtc.regulating = regulating;
        rtc.regulatingControlId = regulatingControlId;

        return rtc;
    }

    public RegulatingData findTwo(String transformerId) {
        return twoWinding.get(transformerId);
    }

    public RegulatingDataThree findThree(String transformerId) {
        return threeWinding.get(transformerId);
    }

    public static class RegulatingDataTapChanger {
        boolean regulating;
        String regulatingControlId;
    }

    public static class RegulatingData {
        RegulatingDataTapChanger ratioTapChanger;
        RegulatingDataTapChanger phaseTapChanger;
    }

    public static class RegulatingDataThree {
        RegulatingData winding1;
        RegulatingData winding2;
        RegulatingData winding3;
    }

    private final Map<String, RegulatingData> twoWinding;
    private final Map<String, RegulatingDataThree> threeWinding;
}
