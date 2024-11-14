/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.elements.OperationalLimitConversion;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.conversion.elements.EquipmentAtBoundaryConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Optional;

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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class TwoWindingsTransformerConversion extends AbstractTransformerConversion implements EquipmentAtBoundaryConversion {

    private DanglingLine danglingLine;

    public TwoWindingsTransformerConversion(PropertyBags ends, Context context) {
        super(CgmesNames.POWER_TRANSFORMER, ends, context);
    }

    @Override
    public boolean valid() {
        // A transformer end voltage level may be null
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

        String eqInstance = ps.get(0).get("graph");
        if (isBoundary(1)) {
            convertTwoWindingsTransformerAtBoundary(eqInstance, 1);
        } else if (isBoundary(2)) {
            convertTwoWindingsTransformerAtBoundary(eqInstance, 2);
        } else {
            throw new ConversionException("Boundary must be at one end of the twoWindingsTransformer");
        }
    }

    @Override
    public Optional <DanglingLine> getDanglingLine() {
        return Optional.ofNullable(danglingLine);
    }

    private void convertTwoWindingsTransformerAtBoundary(String eqInstance, int boundarySide) {

        CgmesT2xModel cgmesT2xModel = new CgmesT2xModel(ps, context);
        InterpretedT2xModel interpretedT2xModel = new InterpretedT2xModel(cgmesT2xModel, context.config(), context);
        ConvertedT2xModel convertedT2xModel = new ConvertedT2xModel(interpretedT2xModel, context);

        // The twoWindingsTransformer is converted to a danglingLine with different VoltageLevels at its ends.
        // As the current danglingLine only supports shunt admittance at the end1 we can only map twoWindingsTransformers with
        // ratio 1.0 and angle 0.0
        // Since the ratio has been fixed to 1.0, if the current (ratio, angle) of the transformer
        // (getRatio(convertedT2xModel), getAngle(convertedT2xModel)) is not (1.0, 0.0)
        // we will have differences in the LF computation.
        // TODO support in the danglingLine the complete twoWindingsTransformer model (transformer + tapChangers)
        danglingLine = convertToDanglingLine(eqInstance, boundarySide, getR(convertedT2xModel), getX(convertedT2xModel), getG(convertedT2xModel), getB(convertedT2xModel));
    }

    private void setToIidm(ConvertedT2xModel convertedT2xModel) {
        TwoWindingsTransformerAdder adder = substation()
                .map(Substation::newTwoWindingsTransformer)
                .orElseThrow(() -> new PowsyblException("Substation null! Transformer must be within a substation"))
                .setR(convertedT2xModel.r)
                .setX(convertedT2xModel.x)
                .setG(Double.isNaN(convertedT2xModel.end1.g) ? 0.0 : convertedT2xModel.end1.g)
                .setB(Double.isNaN(convertedT2xModel.end1.b) ? 0.0 : convertedT2xModel.end1.b)
                .setRatedU1(convertedT2xModel.end1.ratedU)
                .setRatedU2(convertedT2xModel.end2.ratedU);
        if (convertedT2xModel.ratedS != null) {
            adder.setRatedS(convertedT2xModel.ratedS);
        }
        identify(adder);
        connectWithOnlyEq(adder);
        TwoWindingsTransformer tx = adder.add();
        addAliasesAndProperties(tx);
        convertedTerminalsWithOnlyEq(tx.getTerminal1(), tx.getTerminal2());

        setToIidmRatioTapChanger(convertedT2xModel, tx);
        setToIidmPhaseTapChanger(convertedT2xModel, tx, context);

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

    private static void setToIidmPhaseTapChanger(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx, Context context) {
        TapChanger ptc = convertedT2xModel.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(tx);
        setToIidmPhaseTapChanger(ptc, ptca, context);
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

    private static int getStepIndex(TapChanger tapChanger) {
        return tapChanger.getTapPosition();
    }

    private static double getStepR(TapChanger tapChanger) {
        if (tapChanger != null) {
            return tapChanger.getSteps().get(getStepIndex(tapChanger)).getR();
        }
        return 0.0;
    }

    private static double getStepX(TapChanger tapChanger) {
        if (tapChanger != null) {
            return tapChanger.getSteps().get(getStepIndex(tapChanger)).getX();
        }
        return 0.0;
    }

    private static double getStepG1(TapChanger tapChanger) {
        if (tapChanger != null) {
            return tapChanger.getSteps().get(getStepIndex(tapChanger)).getG1();
        }
        return 0.0;
    }

    private static double getStepB1(TapChanger tapChanger) {
        if (tapChanger != null) {
            return tapChanger.getSteps().get(getStepIndex(tapChanger)).getB1();
        }
        return 0.0;
    }

    private static double getValue(double initialValue, double rtcStepValue, double ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    private static double getR(ConvertedT2xModel convertedT2xModel) {
        return getValue(convertedT2xModel.r, getStepR(convertedT2xModel.end1.ratioTapChanger), getStepR(convertedT2xModel.end1.phaseTapChanger));
    }

    private static double getX(ConvertedT2xModel convertedT2xModel) {
        return getValue(convertedT2xModel.x, getStepX(convertedT2xModel.end1.ratioTapChanger), getStepX(convertedT2xModel.end1.phaseTapChanger));
    }

    private static double getG(ConvertedT2xModel convertedT2xModel) {
        return getValue(convertedT2xModel.end1.g, getStepG1(convertedT2xModel.end1.ratioTapChanger), getStepG1(convertedT2xModel.end1.phaseTapChanger));
    }

    private static double getB(ConvertedT2xModel convertedT2xModel) {
        return getValue(convertedT2xModel.end1.b, getStepB1(convertedT2xModel.end1.ratioTapChanger), getStepB1(convertedT2xModel.end1.phaseTapChanger));
    }

    public static void update(TwoWindingsTransformer t2w, Context context) {
        updateTerminals(t2w, context, t2w.getTerminal1(), t2w.getTerminal2());

        boolean isAllowedToRegulatePtc = true;
        t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> updatePhaseTapChanger(t2w, ptc, "", context, isAllowedToRegulatePtc));

        boolean isAllowedToRegulateRtc = checkOnlyOneEnabled(isAllowedToRegulatePtc, t2w.getOptionalPhaseTapChanger().map(com.powsybl.iidm.network.TapChanger::isRegulating).orElse(false));
        t2w.getOptionalRatioTapChanger().ifPresent(rtc -> updateRatioTapChanger(t2w, rtc, "", context, isAllowedToRegulateRtc));

        t2w.getOperationalLimitsGroups1().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(operationalLimitsGroup, "1", t2w, context));
        t2w.getOperationalLimitsGroups2().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(operationalLimitsGroup, "2", t2w, context));
    }
}
