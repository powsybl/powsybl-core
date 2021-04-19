/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.conversion.elements.EquipmentAtBoundaryConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * TwoWindingsTransformer Cgmes Conversion
 * <p>
 * Cgmes conversion for transformers (two and three windings) is divided into four stages: load, interpret, convert and set.
 * <p>
 * Load <br>
 * Native CGMES data is loaded from the triple store query and is put in the CGMES model object (CgmesT2xModel).
 * <p>
 * Interpret <br>
 * CgmesT2xModel data is mapped to a more general two windings transformer model (InterpretedT2xModel)
 * according to a predefined configured alternative. It is an elemental process as the only objective is to put
 * Cgmes data in the fields of the general two windings transformer model.
 * All possible alternatives and the default one are defined in conversion class. See {@link Conversion} <br>
 * InterpretedT2xModel supports ratioTapChanger and phaseTapChanger at each end. Shunt admittances can be defined at both ends and
 * allows to specify the end of the structural ratio.
 * <p>
 * Convert <br>
 * Converts the interpreted model (InterpretedT2xModel) to the converted model object (ConvertedT2xModel). <br>
 * The ConvertedT2xModel only allows to define ratioTapChanger and phaseTapChanger at end1.
 * Shunt admittances and structural ratio must be also at end1. <br>
 * To do this process the following methods are used: <br>
 * moveTapChangerFrom2To1: To move a tapChanger from end2 to end1 <br>
 * combineTapChanger: To reduce two tapChangers to one <br>
 * moveRatioFrom2To1: To move structural ratio from end2 to end1 <br>
 * Finally shunt admittance of both ends are added to end1. This step is an approximation and only
 * will be possible to reproduce the exact case result if Cgmes shunts are defined at end1 or
 * are split and the LoadflowParameter splitShuntAdmittance option is selected. <br>
 * See {@link TapChangerConversion}
 * <p>
 * Set <br>
 * A direct map from ConvertedT2xModel to IIDM model
 * <p>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TwoWindingsTransformerConversion extends AbstractTransformerConversion implements EquipmentAtBoundaryConversion {

    public TwoWindingsTransformerConversion(PropertyBags ends, Context context) {
        super(CgmesNames.POWER_TRANSFORMER, ends, context);
    }

    @Override
    public boolean valid() {
        // An transformer end voltage level may be null
        // (when it is in the boundary and the boundary nodes are not converted)
        // So we do not use the generic validity check for conducting equipment
        // or branch. We only ensure we have nodes at both ends
        for (int k = 1; k <= 2; k++) {
            if (nodeId(k) == null) {
                missing(nodeIdPropertyName() + k);
                return false;
            }
        }
        return true;
    }

    @Override
    public void convert() {
        CgmesT2xModel cgmesT2xModel = new CgmesT2xModel(ps, context);
        InterpretedT2xModel interpretedT2xModel = new InterpretedT2xModel(cgmesT2xModel, context.config(), context);
        ConvertedT2xModel convertedT2xModel = new ConvertedT2xModel(interpretedT2xModel, context);

        setToIidm(convertedT2xModel);
    }

    @Override
    public void convertAtBoundary() {
        // If we have created buses and substations for boundary nodes,
        // convert as a regular line
        if (context.config().convertBoundary()) {
            convert();
            return;
        }

        if (isBoundary(1)) {
            convertTwoWindingsTransformerAtBoundary(1);
        } else if (isBoundary(2)) {
            convertTwoWindingsTransformerAtBoundary(2);
        } else {
            throw new ConversionException("Boundary must be at one end of the twoWindingsTransformer");
        }
    }

    @Override
    public BoundaryLine asBoundaryLine(String boundaryNode) {
        BoundaryLine boundaryLine = super.createBoundaryLine(boundaryNode);

        CgmesT2xModel cgmesT2xModel = new CgmesT2xModel(ps, context);
        InterpretedT2xModel interpretedT2xModel = new InterpretedT2xModel(cgmesT2xModel, context.config(), context);
        ConvertedT2xModel convertedT2xModel = new ConvertedT2xModel(interpretedT2xModel, context);

        double r = convertedT2xModel.r;
        double x = convertedT2xModel.x;
        double g = convertedT2xModel.end1.g;
        double b = convertedT2xModel.end1.b;
        boundaryLine.setParameters(r, x, g, b);

        return boundaryLine;
    }

    private void convertTwoWindingsTransformerAtBoundary(int boundarySide) {

        CgmesT2xModel cgmesT2xModel = new CgmesT2xModel(ps, context);
        InterpretedT2xModel interpretedT2xModel = new InterpretedT2xModel(cgmesT2xModel, context.config(), context);
        ConvertedT2xModel convertedT2xModel = new ConvertedT2xModel(interpretedT2xModel, context);

        double r = convertedT2xModel.r;
        double x = convertedT2xModel.x;
        double gch = convertedT2xModel.end1.g;
        double bch = convertedT2xModel.end1.b;

        convertToDanglingLine(boundarySide, r, x, gch, bch);
    }

    private void setToIidm(ConvertedT2xModel convertedT2xModel) {
        TwoWindingsTransformerAdder adder = substation().newTwoWindingsTransformer()
            .setR(convertedT2xModel.r)
            .setX(convertedT2xModel.x)
            .setG(convertedT2xModel.end1.g)
            .setB(convertedT2xModel.end1.b)
            .setRatedU1(convertedT2xModel.end1.ratedU)
            .setRatedU2(convertedT2xModel.end2.ratedU);
        identify(adder);
        connect(adder);
        TwoWindingsTransformer tx = adder.add();
        addAliasesAndProperties(tx);
        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        setToIidmRatioTapChanger(convertedT2xModel, tx);
        setToIidmPhaseTapChanger(convertedT2xModel, tx);

        setRegulatingControlContext(convertedT2xModel, tx);
        addCgmesReferences(tx, convertedT2xModel.end1.ratioTapChanger);
        addCgmesReferences(tx, convertedT2xModel.end1.phaseTapChanger);
    }

    private static void setToIidmRatioTapChanger(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        TapChanger rtc = convertedT2xModel.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(tx);
        setToIidmRatioTapChanger(rtc, rtca);
    }

    private static void setToIidmPhaseTapChanger(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        TapChanger ptc = convertedT2xModel.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(tx);
        setToIidmPhaseTapChanger(ptc, ptca);
    }

    private static RatioTapChangerAdder newRatioTapChanger(TwoWindingsTransformer tx) {
        return tx.newRatioTapChanger();
    }

    private static PhaseTapChangerAdder newPhaseTapChanger(TwoWindingsTransformer tx) {
        return tx.newPhaseTapChanger();
    }

    private void setRegulatingControlContext(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        CgmesRegulatingControlRatio rcRtc = setContextRegulatingDataRatio(convertedT2xModel.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc = setContextRegulatingDataPhase(convertedT2xModel.end1.phaseTapChanger);

        context.regulatingControlMapping().forTransformers().add(tx.getId(), rcRtc, rcPtc);
    }
}
