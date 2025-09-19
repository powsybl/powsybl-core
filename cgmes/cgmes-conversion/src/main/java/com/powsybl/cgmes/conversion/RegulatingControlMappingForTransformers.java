/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.conversion.RegulatingTerminalMapper.TerminalAndSign;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */

public class RegulatingControlMappingForTransformers {

    private static final String TAP_CHANGER_CONTROL = "TapChangerControl";

    RegulatingControlMappingForTransformers(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        t2xMapping = new HashMap<>();
        t3xMapping = new HashMap<>();
    }

    public void add(String transformerId, CgmesRegulatingControlRatio rcRtc, CgmesRegulatingControlPhase rcPtc) {
        CgmesRegulatingControlForTwoWindingsTransformer rc = new CgmesRegulatingControlForTwoWindingsTransformer();
        rc.ratioTapChanger = rcRtc;
        rc.phaseTapChanger = rcPtc;
        t2xMapping.put(transformerId, rc);
    }

    public void add(String transformerId, CgmesRegulatingControlRatio rcRtc1, CgmesRegulatingControlPhase rcPtc1,
                    CgmesRegulatingControlRatio rcRtc2, CgmesRegulatingControlPhase rcPtc2, CgmesRegulatingControlRatio rcRtc3,
                    CgmesRegulatingControlPhase rcPtc3) {
        CgmesRegulatingControlForThreeWindingsTransformer rc = new CgmesRegulatingControlForThreeWindingsTransformer();
        rc.ratioTapChanger1 = rcRtc1;
        rc.phaseTapChanger1 = rcPtc1;
        rc.ratioTapChanger2 = rcRtc2;
        rc.phaseTapChanger2 = rcPtc2;
        rc.ratioTapChanger3 = rcRtc3;
        rc.phaseTapChanger3 = rcPtc3;
        t3xMapping.put(transformerId, rc);
    }

    public CgmesRegulatingControlRatio buildRegulatingControlRatio(String id, String regulatingControlId, String tculControlMode) {
        CgmesRegulatingControlRatio rtc = new CgmesRegulatingControlRatio();
        rtc.id = id;
        rtc.regulatingControlId = regulatingControlId;
        rtc.tculControlMode = tculControlMode;
        return rtc;
    }

    public CgmesRegulatingControlPhase buildRegulatingControlPhase(String id, String regulatingControlId) {
        CgmesRegulatingControlPhase rtc = new CgmesRegulatingControlPhase();
        rtc.id = id;
        rtc.regulatingControlId = regulatingControlId;
        return rtc;
    }

    void applyTapChangersRegulatingControl(Network network) {
        network.getTwoWindingsTransformerStream().forEach(this::applyTapChangersRegulatingControl);
        network.getThreeWindingsTransformerStream().forEach(this::applyTapChangersRegulatingControl);
    }

    private void applyTapChangersRegulatingControl(TwoWindingsTransformer twt) {
        CgmesRegulatingControlForTwoWindingsTransformer rc = t2xMapping.get(twt.getId());
        if (rc == null) {
            return;
        }

        RegulatingControl rtcControl = getTapChangerControl(rc.ratioTapChanger);
        RegulatingControl ptcControl = getTapChangerControl(rc.phaseTapChanger);

        setPhaseTapChangerControl(ptcControl, twt.getPhaseTapChanger(), twt, "");
        setRatioTapChangerControl(rtcControl, twt.getRatioTapChanger());
    }

    private void applyTapChangersRegulatingControl(ThreeWindingsTransformer twt) {
        CgmesRegulatingControlForThreeWindingsTransformer rc = t3xMapping.get(twt.getId());
        if (rc == null) {
            return;
        }

        RegulatingControl rtcControl1 = getTapChangerControl(rc.ratioTapChanger1);
        RegulatingControl ptcControl1 = getTapChangerControl(rc.phaseTapChanger1);

        RegulatingControl rtcControl2 = getTapChangerControl(rc.ratioTapChanger2);
        RegulatingControl ptcControl2 = getTapChangerControl(rc.phaseTapChanger2);

        RegulatingControl rtcControl3 = getTapChangerControl(rc.ratioTapChanger3);
        RegulatingControl ptcControl3 = getTapChangerControl(rc.phaseTapChanger3);

        setPhaseTapChangerControl(ptcControl1, twt.getLeg1().getPhaseTapChanger(), twt, "1");
        setRatioTapChangerControl(rtcControl1, twt.getLeg1().getRatioTapChanger());

        setPhaseTapChangerControl(ptcControl2, twt.getLeg2().getPhaseTapChanger(), twt, "2");
        setRatioTapChangerControl(rtcControl2, twt.getLeg2().getRatioTapChanger());

        setPhaseTapChangerControl(ptcControl3, twt.getLeg3().getPhaseTapChanger(), twt, "3");
        setRatioTapChangerControl(rtcControl3, twt.getLeg3().getRatioTapChanger());
    }

    private void setRatioTapChangerControl(RegulatingControl control, RatioTapChanger rtc) {
        if (control == null || rtc == null) {
            return;
        }

        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode)) {
            okSet = setRtcRegulatingControlVoltage(control, rtc, context);
        } else if (!isControlModeFixed(control.mode)) {
            context.fixed(control.mode, "Unsupported regulation mode for Ratio tap changer. Considered as a fixed ratio tap changer.");
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setRtcRegulatingControlVoltage(RegulatingControl control, RatioTapChanger rtc, Context context) {
        Optional<Terminal> regulatingTerminal = RegulatingTerminalMapper.mapForVoltageControl(control.cgmesTerminal, context);
        if (regulatingTerminal.isEmpty()) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.cgmesTerminal));
            return false;
        }

        rtc.setRegulationTerminal(regulatingTerminal.get())
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE);
        return true;
    }

    private void setPhaseTapChangerControl(RegulatingControl control, PhaseTapChanger ptc, Connectable<?> twt, String end) {
        if (control == null || ptc == null) {
            return;
        }

        boolean okSet = false;
        if (control.mode.endsWith("currentflow")) {
            okSet = setPtcRegulatingControlCurrentFlow(control, ptc, context, twt, end);
        } else if (control.mode.endsWith("activepower")) {
            okSet = setPtcRegulatingControlActivePower(control, ptc, context, twt, end);
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setPtcRegulatingControlCurrentFlow(RegulatingControl control, PhaseTapChanger ptc, Context context, Connectable<?> twt, String end) {
        return setPtcRegulatingControl(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, control, ptc, context, twt, end);
    }

    private boolean setPtcRegulatingControlActivePower(RegulatingControl control, PhaseTapChanger ptc, Context context, Connectable<?> twt, String end) {
        return setPtcRegulatingControl(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, control, ptc, context, twt, end);
    }

    private boolean setPtcRegulatingControl(PhaseTapChanger.RegulationMode regulationMode, RegulatingControl control, PhaseTapChanger ptc, Context context, Connectable<?> twt, String end) {
        TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(control.cgmesTerminal, context)
                .orElseGet(() -> new TerminalAndSign(null, 1));

        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.cgmesTerminal));
            return false;
        }

        ptc.setRegulationTerminal(mappedRegulatingTerminal.getTerminal())
                .setRegulationMode(regulationMode);

        twt.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN + end, String.valueOf(mappedRegulatingTerminal.getSign()));
        return true;
    }

    private RegulatingControl getTapChangerControl(CgmesRegulatingControl rc) {
        if (rc == null) {
            return null;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            LOG.trace("Regulating control Id not present for tap changer {}", rc.id);
            return null;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return null;
        }

        return control;
    }

    public static String getRegulatingControlId(PropertyBag p) {
        return p.getId(TAP_CHANGER_CONTROL);
    }

    public boolean getRegulating(String controlId) {
        return controlId != null;
    }

    private static boolean isControlModeFixed(String controlMode) {
        return controlMode != null && controlMode.endsWith("fixed");
    }

    private static final class CgmesRegulatingControlForThreeWindingsTransformer {
        CgmesRegulatingControlRatio ratioTapChanger1;
        CgmesRegulatingControlPhase phaseTapChanger1;
        CgmesRegulatingControlRatio ratioTapChanger2;
        CgmesRegulatingControlPhase phaseTapChanger2;
        CgmesRegulatingControlRatio ratioTapChanger3;
        CgmesRegulatingControlPhase phaseTapChanger3;
    }

    private static final class CgmesRegulatingControlForTwoWindingsTransformer {
        CgmesRegulatingControlRatio ratioTapChanger;
        CgmesRegulatingControlPhase phaseTapChanger;
    }

    public static class CgmesRegulatingControlRatio extends CgmesRegulatingControl {
        String tculControlMode;
    }

    public static class CgmesRegulatingControlPhase extends CgmesRegulatingControl {
    }

    private static class CgmesRegulatingControl {
        String id;
        String regulatingControlId;
    }

    private final RegulatingControlMapping parent;
    private final Context context;
    private final Map<String, CgmesRegulatingControlForTwoWindingsTransformer> t2xMapping;
    private final Map<String, CgmesRegulatingControlForThreeWindingsTransformer> t3xMapping;

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingControlMappingForTransformers.class);
}
