package com.powsybl.cgmes.update.elements14;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * For conversion onCreate of TwoWindingsTransformer we need to create
 * additional elements: End1 and End2, tapChangers, tables, if required. All
 * have distinct ID (Subject) and contain reference to the parent
 * PowerTransformer element.
 */
public class TwoWindingsTransformerToPowerTransformer implements ConversionMapper {

    public TwoWindingsTransformerToPowerTransformer(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
        this.currId = change.getIdentifiableId();
        this.idEnd1 = (getEndsId().get("idEnd1") != null) ? getEndsId().get("idEnd1")
            : currId.concat("_OR");
        this.idEnd2 = (getEndsId().get("idEnd2") != null) ? getEndsId().get("idEnd2")
            : currId.concat("_CL");
        this.idRTCH = getTapChangerId("RatioTapChanger");
        this.idPHTC = getTapChangerId("PhaseTapChanger");
    }

    @Override
    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Multimap<String, CgmesPredicateDetails> map = ArrayListMultimap.create();
        TwoWindingsTransformer newTwoWindingsTransformer = (TwoWindingsTransformer) change.getIdentifiable();
        String ptId = newTwoWindingsTransformer.getId();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_TP", false, "cim:PowerTransformer"));

        String name = newTwoWindingsTransformer.getName();
        map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));

        String substationId = newTwoWindingsTransformer.getSubstation().getId();
        map.put("equipmentContainer", new CgmesPredicateDetails(
            "cim:Equipment.MemberOf_EquipmentContainer", "_EQ", true, substationId));

        /**
         * PowerTransformerEnd1
         */
        map.put("rdfTypeEnd1", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:TransformerWinding", idEnd1));

        map.put("powerTransformerEnd1", new CgmesPredicateDetails(
            "cim:TransformerWinding.MemberOf_PowerTransformer", "_EQ", true, ptId, idEnd1));

        map.put("end1Type", new CgmesPredicateDetails(
            "cim:TransformerWinding.windingType", "_EQ", false, "cim:WindingType.primary", idEnd1));

        double b = newTwoWindingsTransformer.getB();
        if (!String.valueOf(b).equals("NaN")) {
            map.put("b",
                new CgmesPredicateDetails("cim:TransformerWinding.b", "_EQ", false, String.valueOf(b), idEnd1));
        }

        double r = newTwoWindingsTransformer.getR();
        if (!String.valueOf(r).equals("NaN")) {
            map.put("r",
                new CgmesPredicateDetails("cim:TransformerWinding.r", "_EQ", false, String.valueOf(r), idEnd1));
        }

        double x = newTwoWindingsTransformer.getX();
        if (!String.valueOf(x).equals("NaN")) {
            map.put("x",
                new CgmesPredicateDetails("cim:TransformerWinding.x", "_EQ", false, String.valueOf(x), idEnd1));
        }

        double g = newTwoWindingsTransformer.getG();
        if (!String.valueOf(g).equals("NaN")) {
            map.put("g",
                new CgmesPredicateDetails("cim:TransformerWinding.g", "_EQ", false, String.valueOf(g), idEnd1));
        }

        double ratedU1 = newTwoWindingsTransformer.getRatedU1();
        if (!String.valueOf(ratedU1).equals("NaN")) {
            map.put("ratedU1", new CgmesPredicateDetails("cim:TransformerWinding.ratedU", "_EQ", false,
                String.valueOf(ratedU1), idEnd1));
        }

        /**
         * PowerTransformerEnd2
         */
        map.put("rdfTypeEnd2", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:TransformerWinding", idEnd2));

        map.put("powerTransformerEnd2", new CgmesPredicateDetails(
            "cim:TransformerWinding.MemberOf_PowerTransformer", "_EQ", true, ptId, idEnd2));

        map.put("end2Type", new CgmesPredicateDetails(
            "cim:TransformerWinding.windingType", "_EQ", false, "cim:WindingType.secondary", idEnd2));

        map.put("bEnd2", new CgmesPredicateDetails(
            "cim:TransformerWinding.b", "_EQ", false, String.valueOf(0.0), idEnd2));

        map.put("rEnd2", new CgmesPredicateDetails(
            "cim:TransformerWinding.r", "_EQ", false, String.valueOf(0.0), idEnd2));

        map.put("xEnd2", new CgmesPredicateDetails(
            "cim:TransformerWinding.x", "_EQ", false, String.valueOf(0.0), idEnd2));

        map.put("gEnd2", new CgmesPredicateDetails(
            "cim:TransformerWinding.g", "_EQ", false, String.valueOf(0.0), idEnd2));

        double ratedU2 = newTwoWindingsTransformer.getRatedU2();
        if (!String.valueOf(ratedU1).equals("NaN")) {
            map.put("ratedU2", new CgmesPredicateDetails("cim:TransformerWinding.ratedU", "_EQ", false,
                String.valueOf(ratedU2), idEnd2));
        }
        /**
         * PhaseTapChanger
         */
        PhaseTapChanger newPhaseTapChanger = newTwoWindingsTransformer.getPhaseTapChanger();

        if (newPhaseTapChanger != null) {
            map.put("rdfTypePHTC", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTabular", idPHTC));

            map.put("namePHTC", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_PHTC"), idPHTC));

            map.put("TransformerWindingPHTC", new CgmesPredicateDetails(
                "cim:PhaseTapChanger.TransformerWinding", "_EQ", true, idEnd1, idPHTC));

            int lowTapPositionPHTC = newPhaseTapChanger.getLowTapPosition();
            map.put("phaseTapChanger.lowTapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionPHTC), idPHTC));

            int tapPositionPHTC = newPhaseTapChanger.getTapPosition();
            map.put("phaseTapChanger.tapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.neutralStep", "_EQ", false, String.valueOf(tapPositionPHTC + 1), idPHTC));
        }
        /**
         * RatioTapChanger
         */
        RatioTapChanger newRatioTapChanger = newTwoWindingsTransformer.getRatioTapChanger();

        if (newRatioTapChanger != null) {
            map.put("rdfTypeRTCH", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChanger", idRTCH));

            map.put("nameRTCH", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_RTTC"), idRTCH));

            map.put("TransformerWindingRTCH", new CgmesPredicateDetails(
                "cim:RatioTapChanger.TransformerWinding", "_EQ", true, idEnd1, idRTCH));

            int lowTapPositionRTCH = newRatioTapChanger.getLowTapPosition();
            map.put("ratioTapChanger.lowTapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionRTCH), idRTCH));

            int tapPositionRTCH = newRatioTapChanger.getTapPosition();
            map.put("ratioTapChanger.tapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.neutralStep", "_EQ", false, String.valueOf(tapPositionRTCH + 1), idRTCH));
        }

        return map;
    }

    /**
     * Check if TransformerWinding elements already exist in grid, if yes - returns
     * the id.
     *
     */
    private Map<String, String> getEndsId() {
        PropertyBags transformerEnds = cgmes.transformerEnds();
        Map<String, String> ids = new HashMap<>();
        Iterator i = transformerEnds.iterator();

        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            String windingType = pb.get("windingType");
            if (pb.getId("PowerTransformer").equals(currId)
                && windingType.endsWith("primary")) {
                idEnd1 = pb.getId("TransformerWinding");
                ids.put("idEnd1", idEnd1);
            } else if (pb.getId("PowerTransformer").equals(currId)
                && windingType.endsWith("secondary")) {
                idEnd2 = pb.getId("TransformerWinding");
                ids.put("idEnd2", idEnd2);
            } else {
                continue;
            }
        }
        return ids;
    }

    /**
     * Check if TapChanger elements already exist in grid, if yes - returns the id.
     *
     */
    private String getTapChangerId(String tapChangerType) {
        PropertyBags tapChangers = (tapChangerType.equals("RatioTapChanger")) ? cgmes.ratioTapChangers()
            : cgmes.phaseTapChangers();
        Iterator i = tapChangers.iterator();

        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("TransformerWinding").equals(idEnd1)) {
                return pb.getId(tapChangerType);
            } else {
                continue;
            }
        }
        return (tapChangerType.equals("RatioTapChanger")) ? idEnd1.concat("_RTTC") : idEnd1.concat("_PHTC");
    }

    private IidmChange change;
    private CgmesModel cgmes;
    private String currId;
    private String idEnd1;
    private String idEnd2;
    private String idRTCH;
    private String idPHTC;
}
