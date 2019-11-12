package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * For conversion onCreate of TwoWindingsTransformer we need to create
 * additional elements: End1 and End2, tapChangers, tables, table points if
 * required. All have distinct ID (Subject) and contain reference to the parents
 * id.
 */
public class TwoWindingsTransformerToPowerTransformer extends AbstractIidmToCgmes {

    private TwoWindingsTransformerToPowerTransformer() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {
        return Collections.unmodifiableMap(Stream.of(
            entry("equipmentContainer",
                new CgmesPredicateDetails("cim:Equipment.EquipmentContainer", "_EQ", true, value)),
            entry("b", new CgmesPredicateDetails("cim:PowerTransformerEnd.b", "_EQ", false, value, newSubject)),
            entry("r", new CgmesPredicateDetails("cim:PowerTransformerEnd.r", "_EQ", false, value, newSubject)),
            entry("x", new CgmesPredicateDetails("cim:PowerTransformerEnd.x", "_EQ", false, value, newSubject)),
            entry("g", new CgmesPredicateDetails("cim:PowerTransformerEnd.g", "_EQ", false, value, newSubject)),
            entry("ratedU",
                new CgmesPredicateDetails("cim:PowerTransformerEnd.ratedU", "_EQ", false, value, newSubject)))
            .collect(entriesToMap()));
        /**
         * PowerTransformerEnd1
         */
        /**
         * PowerTransformerEnd2
         */
        /**
         * PhaseTapChanger
         */
//        if (twt.getPhaseTapChanger() != null) {
//            PhaseTapChangerToPhaseTapChanger phtc = new PhaseTapChangerToPhaseTapChanger(change, cgmes);
//            map.putAll(phtc.mapIidmAtrribute());
//        }
//        /**
//         * RatioTapChanger
//         */
//        if (twt.getRatioTapChanger() != null) {
//            RatioTapChangerToRatioTapChanger rttc = new RatioTapChangerToRatioTapChanger(change, cgmes);
//            map.putAll(rttc.mapIidmAtrribute());
//        }
    }

    public Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof TwoWindingsTransformer)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        twt = (TwoWindingsTransformer) change.getIdentifiable();
        currId = change.getIdentifiableId();
        Map<String, String> ends = getEndsId(currId, cgmes.transformerEnds());
        idEnd1 = (ends.get(ID_END1) != null) ? ends.get(ID_END1) : currId.concat("_OR");
        idEnd2 = (ends.get(ID_END2) != null) ? ends.get(ID_END2) : currId.concat("_CL");
        idPTC = getTapChangerId(PHASE_TAP_CHANGER, cgmes);
        idRTC = getTapChangerId(RATIO_TAP_CHANGER, cgmes);
        idPTCTable = getTapChangerTableId(idPTC, PHASE_TAP_CHANGER, cgmes);
        idRTCTable = getTapChangerTableId(idRTC, RATIO_TAP_CHANGER, cgmes);
        idPTCTablePoint = getTapChangerTablePointId(PHASE_TAP_CHANGER, cgmes);
        idRTCTablePoint = getTapChangerTablePointId(RATIO_TAP_CHANGER, cgmes);
        double r0 = twt.getR();
        double x0 = twt.getX();
        double b0 = twt.getB();
        double g0 = twt.getG();
        double r2 = 0;
        double x2 = 0;
        double b2 = 0;
        double g2 = 0;
        double ratedU1 = twt.getRatedU1();
        double ratedU2 = twt.getRatedU2();
        double rho0 = ratedU2 / ratedU1;
        double rho0Square = rho0 * rho0;
        double r1 = (r0 - r2) / rho0Square;
        double x1 = (x0 - x2) / rho0Square;
        double b1 = (b0 + b2) * rho0Square;
        double g1 = (g0 + g2) * rho0Square;

        return ImmutableMap.<String, String>builder()
            .put("rdfType", "cim:PowerTransformer")
            .put("name", twt.getName())
            .put("substationId", twt.getSubstation().getId())
            .put("rdfTypeEnd", "cim:PowerTransformerEnd")
            .put("b1", String.valueOf(b1))
            .put("r1", String.valueOf(r1))
            .put("x1", String.valueOf(x1))
            .put("g1", String.valueOf(g1))
            .put("idEnd1", idEnd1)
            .put("idEnd2", idEnd2)
            .put("ratedU1", String.valueOf(ratedU1))
            .put("ratedU2", String.valueOf(ratedU2))
            .build();
    }

    /**
     * Check if TransformerEnd elements already exist in grid, if yes - returns the
     * id.
     *
     */
    private Map<String, String> getEndsId(String currId, PropertyBags transformerEnds) {
        Map<String, String> ids = new HashMap<>();
        for (PropertyBag pb : transformerEnds) {
            String windingType = pb.get("endNumber");
            if (pb.getId(POWER_TRANSFORMER).equals(currId)
                && windingType.equals("1")) {
                idEnd1 = pb.getId(TRANSFORMER_END);
                ids.put(ID_END1, idEnd1);

            } else if (pb.getId(POWER_TRANSFORMER).equals(currId)
                && windingType.equals("2")) {
                idEnd2 = pb.getId(TRANSFORMER_END);
                ids.put(ID_END2, idEnd2);
            } else {
                continue;
            }
        }
        return ids;
    }

    /**
     * Check if TapChanger elements already exist in grid, if yes - returns the id.
     * TapChanger is on End1;
     */
    private String getTapChangerId(String tapChangerType, CgmesModel cgmes) {
        PropertyBags tapChangers = (tapChangerType.equals(RATIO_TAP_CHANGER)) ? cgmes.ratioTapChangers()
            : cgmes.phaseTapChangers();
        for (PropertyBag pb : tapChangers) {
            if (pb.getId(TRANSFORMER_END).equals(idEnd1)) {
                return pb.getId(tapChangerType);
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Check if TapChangerTable elements already exist in grid, if yes - returns the
     * id. Otherwise, return concatenated string.
     */
    private String getTapChangerTableId(String tapChangerId, String tapChangerType, CgmesModel cgmes) {
        String propertyName = tapChangerType.concat("Table");
        PropertyBags tapChangers = (tapChangerType.equals(RATIO_TAP_CHANGER)) ? cgmes.ratioTapChangers()
            : cgmes.phaseTapChangers();
        for (PropertyBag pb : tapChangers) {
            if (pb.getId(tapChangerType).equals(tapChangerId) && pb.containsKey(propertyName)) {
                return pb.getId(propertyName);
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String getTapChangerTablePointId(String tapChangerType, CgmesModel cgmes) {
        String propertyName = tapChangerType.concat("TablePoint");
        PropertyBags phaseTapChangerTable = (tapChangerType.equals(RATIO_TAP_CHANGER))
            ? cgmes.ratioTapChangerTable(idRTCTable)
            : cgmes.phaseTapChangerTable(idPTCTable);
        Iterator i = phaseTapChangerTable.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("step").equals("check")) {
                return pb.getId(propertyName);
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }

    TwoWindingsTransformer twt;
    String currId;
    String name;
    String idEnd1;
    String idEnd2;
    String idRTC;
    String idPTC;
    String idPTCTable;
    String idRTCTable;
    String idPTCTablePoint;
    String idRTCTablePoint;
}
