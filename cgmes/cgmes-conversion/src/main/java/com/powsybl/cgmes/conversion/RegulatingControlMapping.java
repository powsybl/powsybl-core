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
import java.util.Optional;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.elements.transformers.AbstractTransformerConversion.findTapChangerId;
import static com.powsybl.cgmes.conversion.export.CgmesExportUtil.*;

/**
 * Handle mapping of RegulatingControl and TapChangerControl from the EQ profile.
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class RegulatingControlMapping {

    private final Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();
    private final Map<String, String> vscToPccTerminal = new HashMap<>();
    private final Context context;

    /**
     * Create an instance of RegulatingControlMapping
     */
    RegulatingControlMapping(Context context) {
        this.context = context;
    }

    /**
     *  <p>
     *  Set the regulation of all regulation-capable equipments of the network.
     *  </p>
     *  <p>
     *  Only the regulation data from the EQ profile is set here.
     *  Data coming from other profiles (e.g. SSH) is applied later.
     *  </p>
     *  <p>
     *  This method is expected to be called only once
     *  after all other connectable and their terminal have been created,
     *  so that regulation on a remote terminal can be processed.
     *  </p>
     */
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
        // Set ratio and phase tap changer control of the 2-winding transformer.
        transformer.getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, ""));
        transformer.getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, ""));
    }

    private void setTapChangerControl(ThreeWindingsTransformer transformer) {
        // Set ratio and phase tap changer control of each leg of the 3-winding transformer.
        transformer.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, "1"));
        transformer.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, "2"));
        transformer.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> setTapChangerControl(transformer, rtc, "3"));
        transformer.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, "1"));
        transformer.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, "2"));
        transformer.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> setTapChangerControl(transformer, ptc, "3"));
    }

    private <C extends Connectable<C>> void setTapChangerControl(C transformer, TapChanger<?, ?, ?, ?> tapChanger, String end) {
        // Retrieve TapChangerControl id and set the regulation for this TapChanger
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

        // Set the voltage regulation for this connectable
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
            setRegulatingControlVoltage(connectable, holder, regulatingControl.terminal());
        } else if (checkControlMode(regulatingControl.mode(), CgmesNames.REACTIVE_POWER)) {
            setRegulatingControlReactivePower(connectable, holder, regulatingControl.terminal(), end);
        } else {
            context.ignored(regulatingControlId, "Unsupported RegulatingControl mode: " + regulatingControl.mode());
        }
    }

    private void setRegulatingControlVoltage(Connectable<?> connectable, VoltageRegulationHolder<?> holder, String terminalId) {
        // Retrieve the IIDM regulated terminal
        Optional<Terminal> regulatingTerminal = RegulatingTerminalMapper
            .mapForVoltageControl(terminalId, context);
        Terminal mappedRegulatingTerminal = null;
        if (regulatingTerminal.isEmpty()) {
            context.invalid(connectable.getId(), String.format("Regulation terminal %s is not mapped or mapped to a switch", terminalId));
            ((PropertiesHolder) holder).setProperty(Conversion.PROPERTY_UNMAPPED_REGULATING_TERMINAL, terminalId);
        } else {
            mappedRegulatingTerminal = regulatingTerminal.get();
            if (!(holder instanceof TapChanger) && mappedRegulatingTerminal.getConnectable().getId().equals(connectable.getId())) {
                mappedRegulatingTerminal = null;
            }
        }

        // Create the voltage regulation
        holder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(mappedRegulatingTerminal)
            .withRegulating(false)
            .build();

        if (connectable instanceof StaticVarCompensator svc && context.config().getUseSvcRegulationDataAsAlternative()) {
            double targetV = Double.parseDouble(svc.getProperty(PROPERTY_SVC_EQ_VOLTAGE_SET_POINT));
            if (targetV > 0.0) {
                holder.setLocalTargetV(targetV);
                holder.getVoltageRegulation().setRegulating(true);
            }
        }
    }

    private void setRegulatingControlReactivePower(Connectable<?> connectable, VoltageRegulationHolder<?> holder, String terminalId, String end) {
        // Retrieve the IIDM regulated terminal
        Optional<RegulatingTerminalMapper.TerminalAndSign> regulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(terminalId, context);
        if (regulatingTerminal.isEmpty()) {
            context.invalid(connectable.getId(), String.format("Regulation terminal %s is not mapped or mapped to a switch", terminalId));
            ((PropertiesHolder) holder).setProperty(Conversion.PROPERTY_UNMAPPED_REGULATING_TERMINAL, terminalId);
            regulatingTerminal = Optional.of(new RegulatingTerminalMapper.TerminalAndSign(holder.getTerminal(), 1));
        }
        Terminal mappedRegulatingTerminal = regulatingTerminal.get().getTerminal();

        // Store the terminal sign returned by the terminal mapping for later use during update of the targetValue.
        connectable.setProperty(getTerminalSignPropertyName(end), String.valueOf(regulatingTerminal.get().getSign()));

        // Create the voltage regulation
        holder.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withTerminal(mappedRegulatingTerminal)
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
            .mapForFlowControl(regulatingControl.terminal(), context)
            .orElseGet(() -> new RegulatingTerminalMapper.TerminalAndSign(null, 1));
        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.missing(String.format("IIDM terminal for this CGMES terminal: %s", regulatingControl.terminal()));
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

    /**
     * Store the RegulatingControl data of the regulating conducting equipment defined in the Equipment profile.
     */
    public void storeRegulatingControl(PropertyBag p) {
        String regulatingControlId = p.getId(CgmesNames.REGULATING_CONTROL);
        cachedRegulatingControls.put(regulatingControlId, new RegulatingControl(p));
    }

    /**
     * Store the PccTerminal data of the VsConverter defined in the Equipment profile.
     */
    public void storePccTerminal(String vscId, PropertyBag p) {
        String pccTerminal = p.getId("PccTerminal");
        if (pccTerminal != null) {
            vscToPccTerminal.put(vscId, pccTerminal);
        }
    }

    /**
     * Check that the RegulatingControl mode is of the specified RegulatingControlModeKind.
     */
    public static boolean checkControlMode(String mode, String kind) {
        return mode != null && mode.toLowerCase(Locale.ROOT).endsWith(kind);
    }

    /**
     * Check if the regulating terminal has been mapped for that regulation holder.
     */
    public static boolean regulatingTerminalMapped(PropertiesHolder holder) {
        return !holder.hasProperty(Conversion.PROPERTY_UNMAPPED_REGULATING_TERMINAL);
    }

    /**
     * A RegulatingControl as defined in the Equipment profile.
     */
    public record RegulatingControl(String mode, String terminal) {

        public RegulatingControl(PropertyBag p) {
            this(p.get("mode").toLowerCase(Locale.ROOT), p.getId(CgmesNames.TERMINAL));
        }
    }
}
