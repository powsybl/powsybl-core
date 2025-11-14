/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion;
import com.powsybl.cgmes.conversion.elements.AbstractObjectConversion;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.extensions.CgmesTapChangersAdder;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.*;

import static com.powsybl.cgmes.conversion.CgmesReports.*;
import static com.powsybl.cgmes.conversion.Conversion.CGMES_PREFIX_ALIAS_PROPERTIES;
import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractTransformerConversion extends AbstractConductingEquipmentConversion {

    protected static final String END_NUMBER = "endNumber";

    AbstractTransformerConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

    protected static void setToIidmRatioTapChanger(TapChanger rtc, RatioTapChangerAdder rtca) {
        boolean isLtcFlag = rtc.isLtcFlag();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        Integer solvedPosition = rtc.getSolvedTapPosition();
        rtca.setLoadTapChangingCapabilities(isLtcFlag).setLowTapPosition(lowStep).setTapPosition(position).setSolvedTapPosition(solvedPosition);

        rtc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double r = step.getR();
            double x = step.getX();
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            rtca.beginStep()
                    .setRho(1 / ratio)
                    .setR(r)
                    .setX(x)
                    .setB(b1)
                    .setG(g1)
                    .endStep();
        });
        rtca.add();
    }

    protected static void setToIidmPhaseTapChanger(TapChanger ptc, PhaseTapChangerAdder ptca, Context context) {
        boolean isLtcFlag = ptc.isLtcFlag();
        int lowStep = ptc.getLowTapPosition();
        int position = ptc.getTapPosition();
        Integer solvedPosition = ptc.getSolvedTapPosition();
        ptca.setLoadTapChangingCapabilities(isLtcFlag).setLowTapPosition(lowStep).setTapPosition(position).setSolvedTapPosition(solvedPosition);

        ptc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            if (Double.isNaN(x)) {
                context.fixed("ptc.step.x", "ptc.step.x is undefined", x, 0.0);
                x = 0.0;
            }
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            ptca.beginStep()
                    .setRho(1 / ratio)
                    .setAlpha(-angle)
                    .setR(r)
                    .setX(x)
                    .setB(b1)
                    .setG(g1)
                    .endStep();
        });
        ptca.add();
    }

    protected CgmesRegulatingControlRatio setContextRegulatingDataRatio(TapChanger tc) {
        if (tc != null) {
            return context.regulatingControlMapping().forTransformers().buildRegulatingControlRatio(tc.getId(), tc.getRegulatingControlId(), tc.getTculControlMode());
        }
        return null;
    }

    protected CgmesRegulatingControlPhase setContextRegulatingDataPhase(TapChanger tc) {
        if (tc != null) {
            return context.regulatingControlMapping().forTransformers().buildRegulatingControlPhase(tc.getId(), tc.getRegulatingControlId());
        }
        return null;
    }

    @Override
    protected void addAliasesAndProperties(Identifiable<?> identifiable) {
        // Add PowerTransformer aliases
        super.addAliasesAndProperties(identifiable);

        // Add PowerTransformerEnds aliases
        String alias;
        String aliasType;
        for (PropertyBag end : ps) {
            alias = end.getId("TransformerEnd");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + TRANSFORMER_END + end.getLocal(END_NUMBER);
            identifiable.addAlias(alias, aliasType);
        }

        // Add RatioTapChangers aliases
        for (PropertyBag rtc : context.ratioTapChangers(identifiable.getId())) {
            alias = rtc.getId("RatioTapChanger");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + RATIO_TAP_CHANGER + rtc.getLocal(END_NUMBER);
            identifiable.addAlias(alias, aliasType, context.config().isEnsureIdAliasUnicity());
        }

        // Add PhaseTapChangers aliases
        for (PropertyBag ptc : context.phaseTapChangers(identifiable.getId())) {
            alias = ptc.getId("PhaseTapChanger");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + PHASE_TAP_CHANGER + ptc.getLocal(END_NUMBER);
            identifiable.addAlias(alias, aliasType, context.config().isEnsureIdAliasUnicity());
        }
    }

    protected static <C extends Connectable<C>> void addCgmesReferences(C transformer, TapChanger tc) {
        if (tc == null || tc.getId() == null) {
            return;
        }
        TapChanger tch = tc.getHiddenCombinedTapChanger();

        CgmesTapChangers<C> tapChangers = transformer.getExtension(CgmesTapChangers.class);
        if (tapChangers == null) {
            transformer.newExtension(CgmesTapChangersAdder.class).add();
            tapChangers = transformer.getExtension(CgmesTapChangers.class);
        }
        // normalStep is always recorded in the step attribute
        tapChangers.newTapChanger()
                .setId(tc.getId())
                .setType(tc.getType())
                .setStep(tc.getTapPosition())
                .setControlId(tc.getRegulatingControlId())
                .add();
        if (tch != null) {
            tapChangers.newTapChanger()
                    .setId(tch.getId())
                    .setCombinedTapChangerId(tc.getId())
                    .setHiddenStatus(true)
                    .setStep(tch.getTapPosition())
                    .setType(tch.getType())
                    .add();
        }
    }

    static <C extends Connectable<C>> void updateRatioTapChanger(Connectable<C> tw, RatioTapChanger rtc, Context context, boolean isRegulatingAllowed) {
        updateRatioTapChanger(tw, rtc, "", context, isRegulatingAllowed);
    }

    static <C extends Connectable<C>> void updateRatioTapChanger(Connectable<C> tw, RatioTapChanger rtc, ThreeSides side, Context context, boolean isRegulatingAllowed) {
        updateRatioTapChanger(tw, rtc, String.valueOf(side.getNum()), context, isRegulatingAllowed);
    }

    static <C extends Connectable<C>> void updatePhaseTapChanger(Connectable<C> tw, PhaseTapChanger ptc, Context context, boolean isRegulatingAllowed) {
        updatePhaseTapChanger(tw, ptc, "", context, isRegulatingAllowed);
    }

    static <C extends Connectable<C>> void updatePhaseTapChanger(Connectable<C> tw, PhaseTapChanger ptc, ThreeSides side, Context context, boolean isRegulatingAllowed) {
        updatePhaseTapChanger(tw, ptc, String.valueOf(side.getNum()), context, isRegulatingAllowed);
    }

    private static <C extends Connectable<C>> void updateRatioTapChanger(Connectable<C> tw, RatioTapChanger rtc, String end, Context context, boolean isRegulatingAllowed) {
        String ratioTapChangerId = findTapChangerId(tw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + end);

        int defaultTapPosition = getDefaultTapPosition(tw, rtc, ratioTapChangerId, getClosestNeutralStep(rtc), context);
        rtc.setTapPosition(findValidTapPosition(rtc, ratioTapChangerId, defaultTapPosition, context));
        findValidSolvedTapPosition(rtc, ratioTapChangerId, context).ifPresent(rtc::setSolvedTapPosition);

        if (rtc.getRegulationTerminal() != null) {
            Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(tw, ratioTapChangerId, context);
            double defaultTargetV = getDefaultTargetV(rtc, context);
            double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);

            double defaultTargetDeadband = getDefaultTargetDeadband(rtc, context);
            double targetDeadband = cgmesRegulatingControl.map(propertyBag -> findTargetDeadband(propertyBag, defaultTargetDeadband, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetDeadband);

            boolean defaultRegulatingOn = getDefaultRegulatingOn(rtc, context);
            boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

            // We always keep the targetValue
            // If targetValue is not valid, emit a warning and deactivate regulating control
            boolean validTargetV = isValidTargetV(targetV);
            if (!validTargetV) {
                context.invalid(ratioTapChangerId, "Regulating control has a bad target voltage " + targetV);
                badVoltageTargetValueRegulatingControlReport(context.getReportNode(), ratioTapChangerId, targetV);
            }
            boolean validTargetDeadband = isValidTargetDeadband(targetDeadband);
            if (!validTargetDeadband) {
                context.invalid(ratioTapChangerId, "Regulating control has a bad target deadband " + targetDeadband);
                badTargetDeadbandRegulatingControlReport(context.getReportNode(), ratioTapChangerId, targetDeadband);
                targetDeadband = Double.NaN; // To avoid an exception from checkTargetDeadband
            }

            boolean regulating = regulatingOn && isRegulatingAllowed && validTargetV && validTargetDeadband;
            if (regulating && !rtc.hasLoadTapChangingCapabilities()) {
                badLoadTapChangingCapabilityTapChangerReport(context.getReportNode(), ratioTapChangerId);
                rtc.setLoadTapChangingCapabilities(true);
            }

            setRegulation(rtc, targetV, targetDeadband, regulating);
        }
    }

    private static <C extends Connectable<C>> Optional<PropertyBag> findCgmesRegulatingControl(Connectable<C> tw, String tapChangerId, Context context) {
        CgmesTapChangers<C> cgmesTcs = tw.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null && tapChangerId != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            return cgmesTc != null ? Optional.ofNullable(context.regulatingControl(cgmesTc.getControlId())) : Optional.empty();
        }
        return Optional.empty();
    }

    // Regulation values (targetValue and targetDeadband) must be valid before the regulation is turned on,
    // and the regulation must be turned off before assigning potentially invalid regulation values,
    // to ensure consistency with the applied checks
    private static void setRegulation(RatioTapChanger rtc, double targetV, double targetDeadband, boolean regulatingOn) {
        if (regulatingOn) {
            rtc.setTargetV(targetV)
                    .setTargetDeadband(targetDeadband)
                    .setRegulating(true);
        } else {
            rtc.setRegulating(false)
                    .setTargetV(targetV)
                    .setTargetDeadband(targetDeadband);
        }
    }

    private static <C extends Connectable<C>> void updatePhaseTapChanger(Connectable<C> tw, PhaseTapChanger ptc, String end, Context context, boolean isRegulatingAllowed) {
        String phaseTapChangerId = findTapChangerId(tw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + end);

        int defaultTapPosition = getDefaultTapPosition(tw, ptc, phaseTapChangerId, getClosestNeutralStep(ptc), context);
        ptc.setTapPosition(findValidTapPosition(ptc, phaseTapChangerId, defaultTapPosition, context));
        findValidSolvedTapPosition(ptc, phaseTapChangerId, context).ifPresent(ptc::setSolvedTapPosition);

        if (ptc.getRegulationTerminal() != null) {
            Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(tw, phaseTapChangerId, context);
            double defaultTargetValue = getDefaultTargetValue(ptc, context);
            double targetValue = cgmesRegulatingControl.map(propertyBag -> findTargetValue(propertyBag, findTerminalSign(tw, end), defaultTargetValue, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetValue);

            double defaultTargetDeadband = getDefaultTargetDeadband(ptc, context);
            double targetDeadband = cgmesRegulatingControl.map(propertyBag -> findTargetDeadband(propertyBag, defaultTargetDeadband, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetDeadband);

            boolean defaultRegulatingOn = getDefaultRegulatingOn(ptc, context);
            boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

            boolean validTargetValue = isValidTargetValue(targetValue);
            if (!validTargetValue) {
                context.invalid(phaseTapChangerId, "Regulating control has a bad target value " + targetValue);
                badTargetValueRegulatingControlReport(context.getReportNode(), phaseTapChangerId, targetValue);
            } else {
                if (ptc.getRegulationMode() == PhaseTapChanger.RegulationMode.CURRENT_LIMITER && targetValue < 0.0) {
                    context.fixed(tw.getId(), "PhaseTapChanger " + end + " : Regulating value is negative while regulationMode is set to CURRENT_LIMITER : fixed to absolute value");
                    targetValue = Math.abs(targetValue);
                }
            }

            boolean validTargetDeadband = isValidTargetDeadband(targetDeadband);
            if (!validTargetDeadband) {
                context.invalid(phaseTapChangerId, "Regulating control has a bad target deadband " + targetDeadband);
                badTargetDeadbandRegulatingControlReport(context.getReportNode(), phaseTapChangerId, targetDeadband);
                targetDeadband = Double.NaN; // To avoid an exception from checkTargetDeadband
            }

            boolean regulating = regulatingOn && isRegulatingAllowed && isValidTargetValue(targetValue) && validTargetDeadband;
            if (regulating && !ptc.hasLoadTapChangingCapabilities()) {
                badLoadTapChangingCapabilityTapChangerReport(context.getReportNode(), phaseTapChangerId);
                ptc.setLoadTapChangingCapabilities(true);
            }

            setRegulation(ptc, targetValue, targetDeadband, regulating);
        }
    }

    // Regulation values (targetValue and targetDeadband) must be valid before the regulation is turned on,
    // and the regulation must be turned off before assigning potentially invalid regulation values,
    // to ensure consistency with the applied checks
    private static void setRegulation(PhaseTapChanger ptc, double targetValue, double targetDeadband, boolean regulatingOn) {
        if (regulatingOn) {
            ptc.setRegulationValue(targetValue)
                    .setTargetDeadband(targetDeadband)
                    .setRegulating(true);
        } else {
            ptc.setRegulating(false)
                    .setRegulationValue(targetValue)
                    .setTargetDeadband(targetDeadband);
        }
    }

    private static Optional<PropertyBag> findCgmesRatioTapChanger(String ratioTapChangerId, Context context) {
        return ratioTapChangerId != null ? Optional.ofNullable(context.ratioTapChanger(ratioTapChangerId)) : Optional.empty();
    }

    private static Optional<PropertyBag> findCgmesPhaseTapChanger(String phaseTapChangerId, Context context) {
        return phaseTapChangerId != null ? Optional.ofNullable(context.phaseTapChanger(phaseTapChangerId)) : Optional.empty();
    }

    private static int findValidTapPosition(RatioTapChanger ratioTapChanger, String ratioTapChangerId, int defaultTapPosition, Context context) {
        int tapPosition = findCgmesRatioTapChanger(ratioTapChangerId, context)
                .map(propertyBag -> findTapPosition(propertyBag, defaultTapPosition))
                .orElse(defaultTapPosition);
        return isValidTapPosition(ratioTapChanger, tapPosition) ? tapPosition : defaultTapPosition;
    }

    private static int findValidTapPosition(PhaseTapChanger phaseTapChanger, String phaseTapChangerId, int defaultTapPosition, Context context) {
        int tapPosition = findCgmesPhaseTapChanger(phaseTapChangerId, context)
                .map(propertyBag -> findTapPosition(propertyBag, defaultTapPosition))
                .orElse(defaultTapPosition);
        return isValidTapPosition(phaseTapChanger, tapPosition) ? tapPosition : defaultTapPosition;
    }

    private static int findTapPosition(PropertyBag p, int defaultTapPosition) {
        OptionalInt tapPosition = findTapPosition(p);
        return tapPosition.isPresent() ? tapPosition.getAsInt() : defaultTapPosition;
    }

    private static <C extends Connectable<C>> int getDefaultTapPosition(Connectable<C> tw, com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger, String tapChangerId, int closestNeutralTapPosition, Context context) {
        Integer validNormalStep = null;
        OptionalInt normalStep = getNormalStep(tw, tapChangerId);
        if (normalStep.isPresent() && isValidTapPosition(tapChanger, normalStep.getAsInt())) {
            validNormalStep = normalStep.getAsInt();
        }
        OptionalInt neutralPosition = tapChanger.getNeutralPosition();
        return getDefaultValue(validNormalStep,
                tapChanger.getTapPosition(),
                neutralPosition.isPresent() ? neutralPosition.getAsInt() : null,
                closestNeutralTapPosition,
                context);
    }

    private static boolean isValidTapPosition(com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger, int tapPosition) {
        return tapChanger.getLowTapPosition() <= tapPosition && tapPosition <= tapChanger.getHighTapPosition();
    }

    private static OptionalInt findTapPosition(PropertyBag p) {
        double tapPosition = p.asDouble(CgmesNames.STEP, p.asDouble(CgmesNames.SV_TAP_STEP));
        return Double.isFinite(tapPosition) ? OptionalInt.of(AbstractObjectConversion.fromContinuous(tapPosition)) : OptionalInt.empty();
    }

    private static OptionalInt findValidSolvedTapPosition(RatioTapChanger ratioTapChanger, String ratioTapChangerId, Context context) {
        Optional<PropertyBag> propertyBag = findCgmesRatioTapChanger(ratioTapChangerId, context);
        if (propertyBag.isPresent()) {
            OptionalInt tap = findSolvedTapPosition(propertyBag.get());
            if (tap.isPresent() && isValidTapPosition(ratioTapChanger, tap.getAsInt())) {
                return tap;
            }
        }
        return OptionalInt.empty();
    }

    private static OptionalInt findValidSolvedTapPosition(PhaseTapChanger phaseTapChanger, String phaseTapChangerId, Context context) {
        Optional<PropertyBag> propertyBag = findCgmesPhaseTapChanger(phaseTapChangerId, context);
        if (propertyBag.isPresent()) {
            OptionalInt tap = findSolvedTapPosition(propertyBag.get());
            if (tap.isPresent() && isValidTapPosition(phaseTapChanger, tap.getAsInt())) {
                return tap;
            }
        }
        return OptionalInt.empty();
    }

    private static OptionalInt findSolvedTapPosition(PropertyBag p) {
        double tapPosition = p.asDouble(CgmesNames.SV_TAP_STEP);
        return Double.isFinite(tapPosition) ? OptionalInt.of(AbstractObjectConversion.fromContinuous(tapPosition)) : OptionalInt.empty();
    }

    private static <C extends Connectable<C>> String findTapChangerId(Connectable<C> tw, String propertyTag) {
        List<String> tcIds = tw.getAliases().stream().filter(alias -> isValidTapChangerIdAlias(tw, alias, tw.getAliasType(alias).orElse(null), propertyTag)).toList();
        if (tcIds.size() == 1) {
            return tcIds.get(0);
        } else {
            throw new ConversionException("unexpected tapChangerId for transformer " + tw.getId());
        }
    }

    private static boolean isValidTapChangerIdAlias(Connectable<?> connectable, String alias, String aliasType, String propertyTag) {
        return alias != null && aliasType != null && aliasType.contains(propertyTag) && !isHiddenTapChanger(connectable, alias);
    }

    private static <C extends Connectable<C>> boolean isHiddenTapChanger(Connectable<C> tw, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = tw.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            if (cgmesTc != null) {
                return cgmesTc.isHidden();
            }
        }
        return false;
    }

    private static double getDefaultTargetV(com.powsybl.iidm.network.RatioTapChanger ratioTapChanger, Context context) {
        return getDefaultValue(null, ratioTapChanger.getTargetV(), Double.NaN, Double.NaN, context);
    }

    private static double getDefaultTargetValue(com.powsybl.iidm.network.PhaseTapChanger phaseTapChanger, Context context) {
        return getDefaultValue(null, phaseTapChanger.getRegulationValue(), Double.NaN, Double.NaN, context);
    }

    // targetDeadBand is optional in Cgmes and mandatory in IIDM then a default value is provided when it is not defined in Cgmes
    private static double getDefaultTargetDeadband(com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger, Context context) {
        return getDefaultValue(0.0, tapChanger.getTargetDeadband(), 0.0, 0.0, context);
    }

    private static boolean getDefaultRegulatingOn(com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger, Context context) {
        return getDefaultValue(false, tapChanger.isRegulating(), false, false, context);
    }

    static boolean checkOnlyOneEnabled(boolean isAllowedToRegulate, boolean previousTapChangerIsRegulatingOn) {
        return isAllowedToRegulate && !previousTapChangerIsRegulatingOn;
    }

    public static <C extends Connectable<C>> OptionalInt getNormalStep(Connectable<C> tw, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = tw.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            if (cgmesTc != null) {
                return cgmesTc.getStep();
            }
        }
        return OptionalInt.empty();
    }

    public static int getClosestNeutralStep(com.powsybl.iidm.network.RatioTapChanger rtc) {
        return rtc.getAllSteps().entrySet().stream()
                .min(Comparator.comparingDouble(entry -> Math.abs(entry.getValue().getRho() - 1.0)))
                .map(Map.Entry::getKey)
                .orElse(rtc.getLowTapPosition());
    }

    public static int getClosestNeutralStep(com.powsybl.iidm.network.PhaseTapChanger ptc) {
        return ptc.getAllSteps().entrySet().stream()
                .min(Comparator.comparingDouble(entry -> Math.abs(entry.getValue().getAlpha())))
                .map(Map.Entry::getKey)
                .orElse(ptc.getLowTapPosition());
    }

    static int computeClosestNeutralStep(com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger) {
        if (tapChanger instanceof PhaseTapChanger ptc) {
            return getClosestNeutralStep(ptc);
        } else if (tapChanger instanceof RatioTapChanger rtc) {
            return getClosestNeutralStep(rtc);
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }
    }

}
