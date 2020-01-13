/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * ThreeWindingsTransformer Cgmes Conversion
 * <p>
 * Cgmes conversion for transformers (two and three windings) is divided into four stages: load, interpret, convert and set.
 * <p>
 * Load <br>
 * Native CGMES data is loaded from the triple store query and is put in the CGMES model object (CgmesT3xModel).
 * <p>
 * Interpret <br>
 * CgmesT3xModel data is mapped to a more general three windings transformer model (InterpretedT3xModel)
 * according to a predefined configured alternative. It is an elemental process as the only objective is to put
 * Cgmes data in the fields of the general three windings transformer model.
 * All possible alternatives and the default one are defined in conversion class. See {@link Conversion} <br>
 * InterpretedT3xModel supports ratioTapChanger and phaseTapChanger at each end of any leg. Shunt admittances
 * can also be defined at both ends of each leg and allows to specify the end of the structural ratio by leg.
 * <p>
 * Convert <br>
 * Converts the interpreted model (InterpretedT3xModel) to the converted model object (ConvertedT3xModel). <br>
 * The ConvertedT3xModel only allows to define ratioTapChanger and phaseTapChanger at the network side of any leg.
 * Shunt admittances and structural ratio must be also at the network side. <br>
 * To do this process the following methods are applied to each leg: <br>
 * moveTapChangerFrom2To1: To move a tapChanger from star bus side to network side <br>
 * combineTapChanger: To reduce two tapChangers to one <br>
 * moveRatioFrom2To1: To move structural ratio from star bus side to network side <br>
 * Finally shunt admittance of both ends of the leg are added to network side. This step is an approximation and only
 * will be possible to reproduce the exact case result if Cgmes shunts are defined at network side or
 * are split and the LoadflowParameter splitShuntAdmittance option is selected. <br>
 * See {@link TapChangerConversion}
 * <p>
 * Set <br>
 * A direct map from ConvertedT3xModel to IIDM model
 * <p>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class NewThreeWindingsTransformerConversion extends AbstractTransformerConversion {

    public NewThreeWindingsTransformerConversion(PropertyBags ends, Context context) {
        super(CgmesNames.POWER_TRANSFORMER, ends, context);
    }

    @Override
    public void convert() {
        CgmesT3xModel cgmesT3xModel = new CgmesT3xModel(ps, context);
        InterpretedT3xModel interpretedT3xModel = new InterpretedT3xModel(cgmesT3xModel, context.config());
        ConvertedT3xModel convertedT3xModel = new ConvertedT3xModel(interpretedT3xModel, context);

        setToIidm(convertedT3xModel);
    }

    private void setToIidm(ConvertedT3xModel convertedT3xModel) {
        ThreeWindingsTransformerAdder txadder = substation().newThreeWindingsTransformer()
            .setRatedU0(convertedT3xModel.ratedU0);
        identify(txadder);

        LegAdder l1adder = txadder.newLeg1();
        setToIidmWindingAdder(convertedT3xModel.winding1, l1adder);
        connect(l1adder, 1);
        l1adder.add();

        LegAdder l2adder = txadder.newLeg2();
        setToIidmWindingAdder(convertedT3xModel.winding2, l2adder);
        connect(l2adder, 2);
        l2adder.add();

        LegAdder l3adder = txadder.newLeg3();
        setToIidmWindingAdder(convertedT3xModel.winding3, l3adder);
        connect(l3adder, 3);
        l3adder.add();

        ThreeWindingsTransformer tx = txadder.add();

        convertedTerminals(
            tx.getLeg1().getTerminal(),
            tx.getLeg2().getTerminal(),
            tx.getLeg3().getTerminal());

        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding1, tx);
        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding2, tx);
        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding3, tx);

        setRegulatingControlContext(convertedT3xModel, tx);
    }

    private static void setToIidmWindingAdder(ConvertedT3xModel.ConvertedWinding convertedModelWinding, LegAdder ladder) {
        ladder.setR(convertedModelWinding.r)
            .setX(convertedModelWinding.x)
            .setG(convertedModelWinding.end1.g)
            .setB(convertedModelWinding.end1.b)
            .setRatedU(convertedModelWinding.end1.ratedU);
    }

    private static void setToIidmWindingTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedT3xModel.ConvertedWinding convertedModelWinding,
        ThreeWindingsTransformer tx) {
        setToIidmRatioTapChanger(convertedT3xModel, convertedModelWinding, tx);
        setToIidmPhaseTapChanger(convertedT3xModel, convertedModelWinding, tx);
    }

    private static void setToIidmRatioTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedT3xModel.ConvertedWinding convertedWinding, ThreeWindingsTransformer tx) {
        TapChanger rtc = convertedWinding.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(convertedT3xModel, tx, convertedWinding.end1.terminal);
        setToIidmRatioTapChanger(rtc, rtca);
    }

    private static void setToIidmPhaseTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedT3xModel.ConvertedWinding convertedWinding, ThreeWindingsTransformer tx) {
        TapChanger ptc = convertedWinding.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(convertedT3xModel, tx, convertedWinding.end1.terminal);
        setToIidmPhaseTapChanger(ptc, ptca);
    }

    private static RatioTapChangerAdder newRatioTapChanger(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx,
        String terminal) {
        if (convertedT3xModel.winding1.end1.terminal.equals(terminal)) {
            return tx.getLeg1().newRatioTapChanger();
        } else if (convertedT3xModel.winding2.end1.terminal.equals(terminal)) {
            return tx.getLeg2().newRatioTapChanger();
        } else if (convertedT3xModel.winding3.end1.terminal.equals(terminal)) {
            return tx.getLeg3().newRatioTapChanger();
        }
        return null;
    }

    private static PhaseTapChangerAdder newPhaseTapChanger(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx,
        String terminal) {
        if (convertedT3xModel.winding1.end1.terminal.equals(terminal)) {
            return tx.getLeg1().newPhaseTapChanger();
        } else if (convertedT3xModel.winding2.end1.terminal.equals(terminal)) {
            return tx.getLeg2().newPhaseTapChanger();
        } else if (convertedT3xModel.winding3.end1.terminal.equals(terminal)) {
            return tx.getLeg3().newPhaseTapChanger();
        }
        return null;
    }

    private void setRegulatingControlContext(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx) {
        CgmesRegulatingControlRatio rcRtc1 = setContextRegulatingDataRatio(convertedT3xModel.winding1.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc1 = setContextRegulatingDataPhase(convertedT3xModel.winding1.end1.phaseTapChanger);
        CgmesRegulatingControlRatio rcRtc2 = setContextRegulatingDataRatio(convertedT3xModel.winding2.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc2 = setContextRegulatingDataPhase(convertedT3xModel.winding2.end1.phaseTapChanger);
        CgmesRegulatingControlRatio rcRtc3 = setContextRegulatingDataRatio(convertedT3xModel.winding3.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc3 = setContextRegulatingDataPhase(convertedT3xModel.winding3.end1.phaseTapChanger);

        context.regulatingControlMapping().forTransformers().add(tx.getId(), rcRtc1, rcPtc1, rcRtc2, rcPtc2, rcRtc3, rcPtc3);
    }
}
