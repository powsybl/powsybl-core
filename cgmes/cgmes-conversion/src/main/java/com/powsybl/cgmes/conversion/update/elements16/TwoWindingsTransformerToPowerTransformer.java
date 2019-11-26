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
 */
public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes {

    public TwoWindingsTransformerToPowerTransformer() {
        ignore("p1");
        ignore("q1");
        ignore("p2");
        ignore("q2");

        // These are examples of not-so-simple updates where
        // we have to find a CGMES sub-object related to IIDM main object
        // From the transformer, we have to find to proper transformer end
        unsupported("r", "cim:PowerTransformerEnd.r", CgmesSubset.EQUIPMENT);
        unsupported("x", "cim:PowerTransformerEnd.x", CgmesSubset.EQUIPMENT);
        unsupported("g", "cim:PowerTransformerEnd.g", CgmesSubset.EQUIPMENT);
        unsupported("b", "cim:PowerTransformerEnd.b", CgmesSubset.EQUIPMENT);
        unsupported("ratedU1", "cim:PowerTransformerEnd.ratedU", CgmesSubset.EQUIPMENT);
        unsupported("ratedU2", "cim:PowerTransformerEnd.ratedU", CgmesSubset.EQUIPMENT);

        computedSubjectUpdate("ratioTapChanger.tapPosition", "cim:TapChanger.step", CgmesSubset.STEADY_STATE_HYPOTHESIS,
            this::tapChangerPosition, this::ratioTapChangerId);
    }

    private void unsupported(String attribute, String predicate, CgmesSubset subset) {
        super.unsupported("TwoWindingsTransformer", attribute, predicate, subset);
    }

    private String tapChangerPosition(Identifiable id) {
        requireTwoWindingsTransformer(id);
        TwoWindingsTransformer twt = (TwoWindingsTransformer) id;
        return String.valueOf(twt.getRatioTapChanger().getTapPosition());
    }

    private String ratioTapChangerId(Identifiable id, CgmesModelTripleStore cgmes) {
        requireTwoWindingsTransformer(id);
        String end;
        idEnd1 = transformerEndId(id, cgmes).get(ID_END1);
        idEnd2 = transformerEndId(id, cgmes).get(ID_END2);
        for (PropertyBag pb : cgmes.ratioTapChangers()) {
            end = pb.getId(TRANSFORMER_END);
            if (end.equals(idEnd1) || end.equals(idEnd2)) {
                return pb.getId("RatioTapChanger");
            } else {
                continue;
            }
        }
        return null;
    }

    private Map<String, String> transformerEndId(Identifiable id, CgmesModelTripleStore cgmes) {
        String twt;
        String windingType;
        String identifiableId = id.getId();
        Map<String, String> ids = new HashMap<>();
        for (PropertyBag pb : cgmes.transformerEnds()) {
            twt = pb.getId(POWER_TRANSFORMER);
            windingType = pb.get("endNumber");
            if (twt.equals(identifiableId) && windingType.equals("1")) {
                idEnd1 = pb.getId(TRANSFORMER_END);
                ids.put(ID_END1, idEnd1);

            } else if (twt.equals(identifiableId) && windingType.equals("2")) {
                idEnd2 = pb.getId(TRANSFORMER_END);
                ids.put(ID_END2, idEnd2);
            } else {
                continue;
            }
        }
        return ids;
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
    private static final String ID_END1 = "idEnd1";
    private static final String ID_END2 = "idEnd2";
}
