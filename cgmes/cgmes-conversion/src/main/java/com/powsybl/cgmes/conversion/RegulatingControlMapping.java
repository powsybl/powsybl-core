/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.elements.transformers.AbstractTransformerConversion.findTapChangerId;
import static com.powsybl.cgmes.conversion.export.CgmesExportUtil.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class RegulatingControlMapping {

    private final Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();
    private final Map<String, String> vscToPccTerminal = new HashMap<>();
    private final Context context;

    RegulatingControlMapping(Context context) {
        this.context = context;
    }

    public void setAllRegulatingControls(Network network) {
        network.getGeneratorStream().forEach(this::setRegulatingControl);
        network.getShuntCompensatorStream().forEach(this::setRegulatingControl);
        network.getStaticVarCompensatorStream().forEach(this::setRegulatingControl);
        network.getVscConverterStationStream()
            .filter(vsc -> vscToPccTerminal.containsKey(vsc.getId()))
            .forEach(vsc -> setRegulatingControlReactivePower(vsc, vsc, vscToPccTerminal.get(vsc.getId()), ""));
        network.getTwoWindingsTransformerStream().forEach(this::setTapChangerControl);
        network.getThreeWindingsTransformerStream().forEach(this::setTapChangerControl);

        cachedRegulatingControls.clear();
        vscToPccTerminal.clear();
    }

    private void setTapChangerControl(TwoWindingsTransformer transformer) {
        transformer.getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, ""));
        transformer.getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, ""));
    }

    private void setTapChangerControl(ThreeWindingsTransformer transformer) {
        transformer.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, "1"));
        transformer.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, "2"));
        transformer.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, "3"));
        transformer.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, "1"));
        transformer.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, "2"));
        transformer.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, "3"));
    }

    private <C extends Connectable<C>> void setTapChangerControl(C transformer, TapChanger<?, ?, ?, ?> tapChanger, String end) {
        String tapChangerAlias = tapChanger instanceof RatioTapChanger ? getRatioTapChangerAliasType(end) : getPhaseTapChangerAliasType(end);
        String tapChangerId = findTapChangerId(transformer, tapChangerAlias);
        CgmesTapChangers<C> cgmesTcs = transformer.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            if (cgmesTc != null) {
                if (tapChanger instanceof RatioTapChanger rtc) {
                    setRegulatingControl(transformer, rtc, cgmesTc.getControlId(), end);
                } else if (tapChanger instanceof PhaseTapChanger ptc) {
                    setRegulatingControl(transformer, ptc, cgmesTc.getControlId(), end);
                }
            }
        }
    }

    private void setRegulatingControl(VoltageRegulationHolder<?> holder) {
        // Retrieve the ConductingEquipment's RegulatingControl id
        Connectable<?> connectable = (Connectable<?>) holder;
        String regulatingControlId = connectable.getProperty(PROPERTY_REGULATING_CONTROL);
        if (regulatingControlId == null) {
            return;
        }

        setRegulatingControl(connectable, holder, regulatingControlId, "");
    }

    private void setRegulatingControl(Connectable<?> connectable, VoltageRegulationHolder<?> holder, String regulatingControlId, String end) {
        // Retrieve the stored EQ RegulatingControl
        RegulatingControl regulatingControl = cachedRegulatingControls.get(regulatingControlId);
        if (regulatingControl == null) {
            context.missing(String.format("Regulating control %s", regulatingControlId));
            return;
        }

        // Set the regulation data
        if (checkControlMode(regulatingControl.mode(), CgmesNames.VOLTAGE)) {
            setRegulatingControlVoltage(connectable, holder, regulatingControl.terminalId());
        } else if (checkControlMode(regulatingControl.mode(), CgmesNames.REACTIVE_POWER)) {
            setRegulatingControlReactivePower(connectable, holder, regulatingControl.terminalId(), end);
        } else {
            context.ignored(regulatingControlId, "Unsupported RegulatingControl mode: " + regulatingControl.mode());
        }
    }

    private void setRegulatingControlVoltage(Connectable<?> connectable, VoltageRegulationHolder<?> holder, String terminalId) {
        // Retrieve the IIDM regulated terminal. Left it empty if it corresponds to the holder's terminal.
        Terminal regulatingTerminal = RegulatingTerminalMapper
                .mapForVoltageControl(terminalId, context)
                .orElse(holder.getTerminal());
        if (!(connectable instanceof TwoWindingsTransformer)
            && !(connectable instanceof ThreeWindingsTransformer)
            && regulatingTerminal.getConnectable().getId().equals(connectable.getId())) {
            regulatingTerminal = null;
        }

        // Create the voltage regulation
        holder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(regulatingTerminal)
            .withRegulating(false)
            .build();
    }

    private void setRegulatingControlReactivePower(Connectable<?> connectable, VoltageRegulationHolder<?> holder, String terminalId, String end) {
        // Retrieve the IIDM regulated terminal. Return if it hasn't been mapped.
        RegulatingTerminalMapper.TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(terminalId, context)
                .orElseGet(() -> new RegulatingTerminalMapper.TerminalAndSign(null, 1));
        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.ignored(connectable.getId(), String.format("Regulation terminal %s is not mapped or mapped to a switch", terminalId));
            return;
        }

        // Store the terminal sign returned by the terminal mapping for later use during update of the targetValue.
        connectable.setProperty(getTerminalSignPropertyName(end), String.valueOf(mappedRegulatingTerminal.getSign()));

        // Create the voltage regulation
        holder.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            // always set the terminal in case of reactive power regulation
            .withTerminal(mappedRegulatingTerminal.getTerminal())
            .withRegulating(false)
            .build();
    }

    private void setRegulatingControl(Connectable<?> transformer, PhaseTapChanger phaseTapChanger, String regulatingControlId, String end) {
        // Retrieve the stored EQ RegulatingControl
        RegulatingControl regulatingControl = cachedRegulatingControls.get(regulatingControlId);
        if (regulatingControl == null) {
            context.missing(String.format("Regulating control %s", regulatingControlId));
            return;
        }

        // Retrieve the IIDM regulated terminal. Return if it hasn't been mapped.
        RegulatingTerminalMapper.TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
            .mapForFlowControl(regulatingControl.terminalId(), context)
            .orElseGet(() -> new RegulatingTerminalMapper.TerminalAndSign(null, 1));
        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.missing(String.format("IIDM terminal for this CGMES terminal: %s", regulatingControl.terminalId()));
            return;
        }

        // Create the regulation
        if (checkControlMode(regulatingControl.mode(), CgmesNames.ACTIVE_POWER)) {
            phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
        } else if (checkControlMode(regulatingControl.mode(), CgmesNames.CURRENT_FLOW)) {
            phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
        } else {
            context.ignored(regulatingControlId, "Unsupported TapChangerControl mode: " + regulatingControl.mode());
            return;
        }
        phaseTapChanger.setRegulationTerminal(mappedRegulatingTerminal.getTerminal());

        transformer.setProperty(getTerminalSignPropertyName(end), String.valueOf(mappedRegulatingTerminal.getSign()));
    }

    public void storeRegulatingControl(PropertyBag p) {
        String regulatingControlId = p.getId(CgmesNames.REGULATING_CONTROL);
        cachedRegulatingControls.put(regulatingControlId, new RegulatingControl(p));
    }

    public void storePccTerminal(String vscId, PropertyBag p) {
        String pccTerminal = p.getId("PccTerminal");
        if (pccTerminal != null) {
            vscToPccTerminal.put(vscId, pccTerminal);
        }
    }

    public static boolean checkControlMode(String mode, String kind) {
        return mode != null && mode.toLowerCase(Locale.ROOT).endsWith(kind);
    }

    public record RegulatingControl(String mode, String terminalId) {

        public RegulatingControl(PropertyBag p) {
            this(p.get("mode").toLowerCase(Locale.ROOT), p.getId(CgmesNames.TERMINAL));
        }
    }
}
