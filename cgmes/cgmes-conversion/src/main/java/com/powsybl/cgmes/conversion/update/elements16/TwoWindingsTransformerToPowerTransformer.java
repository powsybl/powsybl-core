/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes {

    public TwoWindingsTransformerToPowerTransformer() {
        ignore("p1");
        ignore("q1");
        ignore("p2");
        ignore("q2");
        // TODO elena the below parameters must be completed with appropriate changes for TapChangers steps
        unsupported("ratedU1");
        unsupported("ratedU2");
        unsupported("r");
        unsupported("x");
        unsupported("b");
        unsupported("g");

        // These are examples of not-so-simple updates where
        // we have to find a CGMES sub-object related to IIDM main object
        // From the transformer, we have to find to proper transformer end
        computedValueAndSubjectUpdate("r", "cim:PowerTransformerEnd.r", CgmesSubset.EQUIPMENT, this::r1, this::end1);
        computedValueAndSubjectUpdate("r", "cim:PowerTransformerEnd.r", CgmesSubset.EQUIPMENT, this::r2, this::end2);
        computedValueAndSubjectUpdate("x", "cim:PowerTransformerEnd.x", CgmesSubset.EQUIPMENT, this::x1, this::end1);
        computedValueAndSubjectUpdate("x", "cim:PowerTransformerEnd.x", CgmesSubset.EQUIPMENT, this::x2, this::end2);
        computedValueAndSubjectUpdate("b", "cim:PowerTransformerEnd.b", CgmesSubset.EQUIPMENT, this::b1, this::end1);
        computedValueAndSubjectUpdate("b", "cim:PowerTransformerEnd.b", CgmesSubset.EQUIPMENT, this::b2, this::end2);
        computedValueAndSubjectUpdate("g", "cim:PowerTransformerEnd.g", CgmesSubset.EQUIPMENT, this::g1, this::end1);
        computedValueAndSubjectUpdate("g", "cim:PowerTransformerEnd.g", CgmesSubset.EQUIPMENT, this::g2, this::end2);

        computedSubjectUpdate("ratedU1", "cim:PowerTransformerEnd.ratedU", CgmesSubset.EQUIPMENT, this::end1);
        computedSubjectUpdate("ratedU2", "cim:PowerTransformerEnd.ratedU", CgmesSubset.EQUIPMENT, this::end2);
        // RTC
        computedSubjectUpdate("ratioTapChanger.tapPosition", "cim:TapChanger.step", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::ratioTapChangerId);
        computedSubjectUpdate("ratioTapChanger.targetV", "cim:RegulatingControl.targetValue", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::tapChangerControlId);
        computedSubjectUpdate("ratioTapChanger.targetDeadband", "cim:RegulatingControl.targetDeadband", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::tapChangerControlId);
        computedSubjectUpdate("ratioTapChanger.regulating", "cim:RegulatingControl.enabled", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::tapChangerControlId);
        computedSubjectUpdate("ratioTapChanger.regulating", "cim:TapChanger.controlEnabled", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::ratioTapChangerId);
    }

    private void unsupported(String attribute, String predicate, CgmesSubset subset) {
        super.unsupported("TwoWindingsTransformer", attribute, predicate, subset);
    }

    private String ratioTapChangerId(Identifiable id, CgmesModelTripleStore cgmes) {
        requireTwoWindingsTransformer(id);
        String end;
        idEnd1 = transformerEndId(id, cgmes).get(ID_END1);
        idEnd2 = transformerEndId(id, cgmes).get(ID_END2);
        for (PropertyBag rtc : cgmes.ratioTapChangers()) {
            end = rtc.getId(TRANSFORMER_END);
            if (end.equals(idEnd1) || end.equals(idEnd2)) {
                return rtc.getId("RatioTapChanger");
            } else {
                continue;
            }
        }
        return null;
    }

    private String tapChangerControlId(Identifiable id, CgmesModelTripleStore cgmes) {
        requireTwoWindingsTransformer(id);
        String end;
        idEnd1 = transformerEndId(id, cgmes).get(ID_END1);
        idEnd2 = transformerEndId(id, cgmes).get(ID_END2);
        for (PropertyBag rtc : cgmes.ratioTapChangers()) {
            end = rtc.getId(TRANSFORMER_END);
            if (end.equals(idEnd1) || end.equals(idEnd2)) {
                return rtc.getId("TapChangerControl");
            } else {
                continue;
            }
        }
        return null;
    }

    private String end1(Identifiable id, CgmesModelTripleStore cgmes) {
        return transformerEndId(id, cgmes).get(ID_END1);
    }

    private String end2(Identifiable id, CgmesModelTripleStore cgmes) {
        return transformerEndId(id, cgmes).get(ID_END2);
    }

    private Map<String, String> transformerEndId(Identifiable id, CgmesModelTripleStore cgmes) {
        String twt;
        String windingType;
        String identifiableId = id.getId();
        Map<String, String> ids = new HashMap<>();
        for (PropertyBag end : cgmes.transformerEnds()) {
            twt = end.getId(POWER_TRANSFORMER);
            windingType = end.get(END_NUMBER);
            if (twt.equals(identifiableId) && windingType.equals("1")) {
                ids.put(ID_END1, end.getId(TRANSFORMER_END));
            } else if (twt.equals(identifiableId) && windingType.equals("2")) {
                ids.put(ID_END2, end.getId(TRANSFORMER_END));
            } else {
                continue;
            }
        }
        return ids;
    }

    private String r1(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("r1");
    }

    private String r2(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("r2");
    }

    private String x1(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("x1");
    }

    private String x2(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("x2");
    }

    private String b1(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("b1");
    }

    private String b2(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("b2");
    }

    private String g1(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("g1");
    }

    private String g2(Identifiable id, CgmesModelTripleStore cgmes) {
        return computeTransformerValues(id, cgmes).get("g2");
    }

    private Map<String, String> computeTransformerValues(Identifiable id, CgmesModelTripleStore cgmes) {
        requireTwoWindingsTransformer(id);
        TwoWindingsTransformer twt = (TwoWindingsTransformer) id;
        Map<String, String> map = new HashMap<>();
        double r1 = 0.0;
        double x1 = 0.0;
        double b1 = 0.0;
        double g1 = 0.0;
        double r2 = 0.0;
        double x2 = 0.0;
        double b2 = 0.0;
        double g2 = 0.0;
        double ratedU1 = twt.getRatedU1();
        double ratedU2 = twt.getRatedU2();
        double rNetwork = twt.getR();
        double xNetwork = twt.getX();
        double gNetwork = twt.getG();
        double bNetwork = twt.getB();
        for (PropertyBag end : cgmes.transformerEnds()) {
            if (end.getId(POWER_TRANSFORMER).equals(id.getId())) {
                if (end.get(END_NUMBER).equals("1")) {
                    r1 = end.asDouble("r");
                    x1 = end.asDouble("x");
                    b1 = end.asDouble("b");
                    g1 = end.asDouble("g");
                } else if (end.get(END_NUMBER).equals("2")) {
                    r2 = end.asDouble("r");
                    x2 = end.asDouble("x");
                    b2 = end.asDouble("b");
                    g2 = end.asDouble("g");
                }
                // FIXME elena check non-zero division
                double rho0 = ratedU2 / ratedU1;
                double rho0Square = rho0 * rho0;
                if (r2 != 0.0) {
                    r2 = rNetwork - r1 * rho0Square;
                } else {
                    r1 = (rNetwork - r2) / rho0Square;
                }
                if (x2 != 0.0) {
                    x2 = xNetwork - x1 * rho0Square;
                } else {
                    x1 = (xNetwork - x2) / rho0Square;
                }
                if (b2 != 0.0) {
                    b2 = b1 / rho0Square - bNetwork;
                } else {
                    b1 = (bNetwork + b2) * rho0Square;
                }
                if (g2 != 0.0) {
                    g2 = g1 / rho0Square - bNetwork;
                } else {
                    g1 = (gNetwork + g2) * rho0Square;
                }
            }
        }
        map.put("r1", String.valueOf(r1));
        map.put("r2", String.valueOf(r2));
        map.put("x1", String.valueOf(x1));
        map.put("x2", String.valueOf(x2));
        map.put("b1", String.valueOf(b1));
        map.put("b2", String.valueOf(b2));
        map.put("g1", String.valueOf(g1));
        map.put("g2", String.valueOf(g2));
        return map;
    }

    private void requireTwoWindingsTransformer(Identifiable id) {
        if (!(id instanceof TwoWindingsTransformer)) {
            throw new ClassCastException("Expected TwoWindingsTransformer, got " + id.getClass().getSimpleName());
        }
    }

    String idEnd1;
    String idEnd2;
    private static final String POWER_TRANSFORMER = "PowerTransformer";
    private static final String TRANSFORMER_END = "TransformerEnd";
    private static final String END_NUMBER = "endNumber";
    private static final String ID_END1 = "idEnd1";
    private static final String ID_END2 = "idEnd2";
}
