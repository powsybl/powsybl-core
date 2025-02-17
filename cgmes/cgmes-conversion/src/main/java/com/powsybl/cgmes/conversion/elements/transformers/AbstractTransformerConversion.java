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
import com.powsybl.cgmes.model.WindingType;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static com.powsybl.cgmes.conversion.CgmesReports.*;
import static com.powsybl.cgmes.conversion.Conversion.CGMES_PREFIX_ALIAS_PROPERTIES;
import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
abstract class AbstractTransformerConversion extends AbstractConductingEquipmentConversion {

    AbstractTransformerConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

    protected static void setToIidmRatioTapChanger(TapChanger rtc, RatioTapChangerAdder rtca) {
        boolean isLtcFlag = rtc.isLtcFlag();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        rtca.setLoadTapChangingCapabilities(isLtcFlag).setLowTapPosition(lowStep).setTapPosition(position);

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
        int lowStep = ptc.getLowTapPosition();
        int position = ptc.getTapPosition();
        ptca.setLowTapPosition(lowStep).setTapPosition(position);

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
            return context.regulatingControlMapping().forTransformers().buildRegulatingControlPhase(tc.getId(), tc.getRegulatingControlId(), tc.isLtcFlag());
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
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + TRANSFORMER_END + WindingType.endNumber(end);
            identifiable.addAlias(alias, aliasType);
        }

        // Add RatioTapChangers aliases
        for (PropertyBag rtc : context.ratioTapChangers(identifiable.getId())) {
            alias = rtc.getId("RatioTapChanger");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + RATIO_TAP_CHANGER + WindingType.endNumber(rtc);
            identifiable.addAlias(alias, aliasType, context.config().isEnsureIdAliasUnicity());
        }

        // Add PhaseTapChangers aliases
        for (PropertyBag ptc : context.phaseTapChangers(identifiable.getId())) {
            alias = ptc.getId("PhaseTapChanger");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + PHASE_TAP_CHANGER + WindingType.endNumber(ptc);
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

        DefaultValueInteger defaultTapPosition = getDefaultTapPosition(tw, rtc, ratioTapChangerId);
        int tapPosition = findCgmesRatioTapChanger(ratioTapChangerId, context)
                .map(propertyBag -> findTapPosition(propertyBag, defaultTapPosition, context))
                .orElse(defaultValue(defaultTapPosition, context));
        int validTapPosition = isValidTapPosition(rtc, tapPosition) ? tapPosition : defaultValue(defaultTapPosition, context);
        rtc.setTapPosition(validTapPosition);

        if (regulatingControlIsDefined(rtc.getRegulationTerminal())) {
            Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(tw, ratioTapChangerId, context);
            DefaultValueDouble defaultTargetV = getDefaultTargetV(rtc);
            double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetV, context));

            DefaultValueDouble defaultTargetDeadband = getDefaultTargetDeadband(rtc);
            double targetDeadband = cgmesRegulatingControl.map(propertyBag -> findTargetDeadband(propertyBag, defaultTargetDeadband, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetDeadband, context));

            DefaultValueBoolean defaultRegulatingOn = getDefaultRegulatingOn(rtc);
            boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultRegulatingOn, context));

            // We always keep the targetValue
            // It targetValue is not valid, emit a warning and deactivate regulating control
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

            setRegulation(rtc, targetV, targetDeadband, regulatingOn && isRegulatingAllowed && validTargetV && validTargetDeadband);
        }
    }

    private static void setRegulation(RatioTapChanger rtc, double targetV, double targetDeadband, boolean regulatingOn) {
        if (regulatingOn) {
            rtc.setTargetV(targetV).setTargetDeadband(targetDeadband).setRegulating(true);
        } else {
            rtc.setRegulating(false).setTargetV(targetV).setTargetDeadband(targetDeadband);
        }
    }

    private static <C extends Connectable<C>> void updatePhaseTapChanger(Connectable<C> tw, PhaseTapChanger ptc, String end, Context context, boolean isRegulatingAllowed) {
        String phaseTapChangerId = findTapChangerId(tw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + end);

        DefaultValueInteger defaultTapPosition = getDefaultTapPosition(tw, ptc, phaseTapChangerId);
        int tapPosition = findCgmesPhaseTapChanger(phaseTapChangerId, context)
                .map(propertyBag -> findTapPosition(propertyBag, defaultTapPosition, context))
                .orElse(defaultValue(defaultTapPosition, context));
        int validTapPosition = isValidTapPosition(ptc, tapPosition) ? tapPosition : defaultValue(defaultTapPosition, context);
        ptc.setTapPosition(validTapPosition);

        if (regulatingControlIsDefined(ptc.getRegulationTerminal())) {
            Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(tw, phaseTapChangerId, context);
            DefaultValueDouble defaultTargetValue = getDefaultTargetValue(ptc);
            double targetValue = cgmesRegulatingControl.map(propertyBag -> findTargetValue(propertyBag, findTerminalSign(tw, end), defaultTargetValue, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetValue, context));

            DefaultValueDouble defaultTargetDeadband = getDefaultTargetDeadband(ptc);
            double targetDeadband = cgmesRegulatingControl.map(propertyBag -> findTargetDeadband(propertyBag, defaultTargetDeadband, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetDeadband, context));

            DefaultValueBoolean defaultRegulatingOn = getDefaultRegulatingOn(ptc);
            boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultRegulatingOn, context));

            boolean fixedRegulating = regulatingOn;
            if (regulatingOn && ptc.getRegulationMode() == PhaseTapChanger.RegulationMode.FIXED_TAP) {
                context.fixed(phaseTapChangerId, "RegulationMode: regulating is set to true whereas regulationMode is set to FIXED_TAP: regulating fixed to false");
                fixedRegulating = false;
            }

            boolean validTargetValue = isValidTargetValue(targetValue);
            if (!validTargetValue) {
                context.invalid(phaseTapChangerId, "Regulating control has a bad target value " + targetValue);
                badTargetValueRegulatingControlReport(context.getReportNode(), phaseTapChangerId, targetValue);
            }

            boolean validTargetDeadband = isValidTargetDeadband(targetDeadband);
            if (!validTargetDeadband) {
                context.invalid(phaseTapChangerId, "Regulating control has a bad target deadband " + targetDeadband);
                badTargetDeadbandRegulatingControlReport(context.getReportNode(), phaseTapChangerId, targetDeadband);
                targetDeadband = Double.NaN; // To avoid an exception from checkTargetDeadband
            }

            setRegulation(ptc, targetValue, targetDeadband, fixedRegulating && isRegulatingAllowed && isValidTargetValue(targetValue) && validTargetDeadband);
        }
    }

    private static void setRegulation(PhaseTapChanger ptc, double targetValue, double targetDeadband, boolean regulatingOn) {
        if (regulatingOn) {
            ptc.setRegulationValue(targetValue).setTargetDeadband(targetDeadband).setRegulating(true);
        } else {
            ptc.setRegulating(false).setRegulationValue(targetValue).setTargetDeadband(targetDeadband);
        }
    }

    private static boolean regulatingControlIsDefined(Terminal regulatedTerminal) {
        return regulatedTerminal != null;
    }

    private static Optional<PropertyBag> findCgmesRatioTapChanger(String ratioTapChangerId, Context context) {
        return ratioTapChangerId != null ? Optional.ofNullable(context.ratioTapChanger(ratioTapChangerId)) : Optional.empty();
    }

    private static Optional<PropertyBag> findCgmesPhaseTapChanger(String phaseTapChangerId, Context context) {
        return phaseTapChangerId != null ? Optional.ofNullable(context.phaseTapChanger(phaseTapChangerId)) : Optional.empty();
    }

    private static int findTapPosition(PropertyBag p, DefaultValueInteger defaultTapPosition, Context context) {
        OptionalInt tapPosition = findTapPosition(p, context);
        return tapPosition.isPresent() ? tapPosition.getAsInt() : defaultValue(defaultTapPosition, context);
    }

    private static <C extends Connectable<C>> DefaultValueInteger getDefaultTapPosition(Connectable<C> tw, com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger, String tapChangerId) {
        return new DefaultValueInteger(getNormalStep(tw, tapChangerId),
                tapChanger.getTapPosition(),
                tapChanger.getNeutralPosition().orElse(getNormalStep(tw, tapChangerId)),
                tapChanger.getNeutralPosition().orElse(getNormalStep(tw, tapChangerId)));
    }

    private static boolean isValidTapPosition(com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger, int tapPosition) {
        return tapChanger.getLowTapPosition() <= tapPosition && tapPosition <= tapChanger.getHighTapPosition();
    }

    private static OptionalInt findTapPosition(PropertyBag p, Context context) {
        double tapPosition = findDoubleTapPosition(p, context);
        return Double.isFinite(tapPosition) ? OptionalInt.of(AbstractObjectConversion.fromContinuous(tapPosition)) : OptionalInt.empty();
    }

    private static double findDoubleTapPosition(PropertyBag p, Context context) {
        return switch (context.config().getProfileForInitialValuesShuntSectionsTapPositions()) {
            case SSH -> p.asDouble(CgmesNames.STEP, p.asDouble(CgmesNames.SV_TAP_STEP));
            case SV -> p.asDouble(CgmesNames.SV_TAP_STEP, p.asDouble(CgmesNames.STEP));
        };
    }

    private static <C extends Connectable<C>> int getNormalStep(Connectable<C> tw, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = tw.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            if (cgmesTc != null) {
                return cgmesTc.getStep().orElseThrow();
            }
        }
        throw new ConversionException("normalStep must be defined in transformer: " + tw.getId());
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

    private static DefaultValueDouble getDefaultTargetV(com.powsybl.iidm.network.RatioTapChanger ratioTapChanger) {
        return new DefaultValueDouble(null, ratioTapChanger.getTargetV(), Double.NaN, Double.NaN);
    }

    private static DefaultValueDouble getDefaultTargetValue(com.powsybl.iidm.network.PhaseTapChanger phaseTapChanger) {
        return new DefaultValueDouble(null, phaseTapChanger.getRegulationValue(), Double.NaN, Double.NaN);
    }

    // targetDeadBand is optional in Cgmes and mandatory in IIDM then a default value is provided when it is not defined in Cgmes
    private static DefaultValueDouble getDefaultTargetDeadband(com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger) {
        return new DefaultValueDouble(0.0, tapChanger.getTargetDeadband(), 0.0, 0.0);
    }

    private static DefaultValueBoolean getDefaultRegulatingOn(com.powsybl.iidm.network.TapChanger<?, ?, ?, ?> tapChanger) {
        return new DefaultValueBoolean(false, tapChanger.isRegulating(), false, false);
    }

    static boolean checkOnlyOneEnabled(boolean isAllowedToRegulate, boolean previousTapChangerIsRegulatingOn) {
        return isAllowedToRegulate && !previousTapChangerIsRegulatingOn;
    }
}
