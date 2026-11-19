/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_TERMINAL_SIGN;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class RegulatingControlMapping {

    static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES terminal: %s";

    private final RegulatingControlMappingForTransformers regulatingControlMappingForTransformers;
    private final RegulatingControlMappingForVscConverters regulatingControlMappingForVscConverters;
    private final Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();
    private final Context context;

    RegulatingControlMapping(Context context) {
        this.context = context;
        regulatingControlMappingForTransformers = new RegulatingControlMappingForTransformers(this, context);
        regulatingControlMappingForVscConverters = new RegulatingControlMappingForVscConverters(context);
    }

    public RegulatingControlMappingForTransformers forTransformers() {
        return regulatingControlMappingForTransformers;
    }

    public RegulatingControlMappingForVscConverters forVscConverters() {
        return regulatingControlMappingForVscConverters;
    }

    public RegulatingControl fetchRegulatingControl(String regulatingControlId) {
        return cachedRegulatingControls.get(regulatingControlId);
    }

    void storeRegulatingControl(PropertyBag p) {
        String regulatingControlId = p.getId(CgmesNames.REGULATING_CONTROL);
        cachedRegulatingControls.put(regulatingControlId, new RegulatingControl(p));
    }

    void setAllRegulatingControls(Network network) {
        network.getGeneratorStream().forEach(this::setRegulatingControl);
        network.getShuntCompensatorStream().forEach(this::setRegulatingControl);
        network.getStaticVarCompensatorStream().forEach(this::setRegulatingControl);
        regulatingControlMappingForVscConverters.applyRegulatingControls(network);
        regulatingControlMappingForTransformers.applyTapChangersRegulatingControl(network);
        cachedRegulatingControls.clear();
    }

    private void setRegulatingControl(VoltageRegulationHolder<?> holder) {
        // Retrieve the ConductingEquipment's RegulatingControl id
        String regulatingControlId = ((Identifiable<?>) holder).getProperty(PROPERTY_REGULATING_CONTROL);
        if (regulatingControlId == null) {
            return;
        }

        // Retrieve the stored EQ RegulatingControl
        RegulatingControl regulatingControl = fetchRegulatingControl(regulatingControlId);
        if (regulatingControl == null) {
            context.missing(String.format("Regulating control %s", regulatingControlId));
            return;
        }

        // Set the regulation data
        if (RegulatingControlMapping.isControlModeVoltage(regulatingControl.mode())) {
            setRegulatingControlVoltage(holder, regulatingControl.terminalId());
        } else if (RegulatingControlMapping.isControlModeReactivePower(regulatingControl.mode())) {
            setRegulatingControlReactivePower(holder, regulatingControl.terminalId());
        } else {
            context.ignored(regulatingControl.mode(), "Unsupported regulation mode for " + ((Identifiable<?>) holder).getId());
        }
    }

    private void setRegulatingControlVoltage(VoltageRegulationHolder<?> holder, String terminalId) {
        // Retrieve the IIDM regulated terminal. Left it empty if it corresponds to the holder's terminal.
        Terminal regulatingTerminal = RegulatingTerminalMapper
                .mapForVoltageControl(terminalId, context)
                .orElse(holder.getTerminal());
        if (regulatingTerminal.getConnectable().getId().equals(((Identifiable<?>) holder).getId())) {
            regulatingTerminal = null;
        }

        // Create the voltage regulation
        holder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(regulatingTerminal)
            .withRegulating(false)
            .build();
    }

    private void setRegulatingControlReactivePower(VoltageRegulationHolder<?> holder, String terminalId) {
        // Retrieve the IIDM regulated terminal. Return if it hasn't been mapped.
        RegulatingTerminalMapper.TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(terminalId, context)
                .orElseGet(() -> new RegulatingTerminalMapper.TerminalAndSign(null, 1));
        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.ignored(((Identifiable<?>) holder).getId(), String.format("Regulation terminal %s is not mapped or mapped to a switch", terminalId));
            return;
        }

        // Store the terminal sign returned by the terminal mapping for later use during update of the targetValue.
        ((Identifiable<?>) holder).setProperty(PROPERTY_TERMINAL_SIGN, String.valueOf(mappedRegulatingTerminal.getSign()));

        // Create the voltage regulation
        holder.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            // always set the terminal in case of reactive power regulation
            .withTerminal(mappedRegulatingTerminal.getTerminal())
            .withRegulating(false)
            .build();
    }

    public static boolean isControlModeVoltage(String controlMode) {
        return controlMode != null && controlMode.endsWith(CgmesNames.VOLTAGE_TAG);
    }

    public static boolean isControlModeReactivePower(String controlMode) {
        return controlMode != null && controlMode.toLowerCase(Locale.ROOT).endsWith(CgmesNames.REACTIVE_POWER);
    }

    protected static String getRegulatingControlId(PropertyBag p) {
        return p.getId(CgmesNames.REGULATING_CONTROL);
    }

    public record RegulatingControl(String mode, String terminalId) {

        public RegulatingControl(PropertyBag p) {
            this(p.get("mode").toLowerCase(Locale.ROOT), p.getId(CgmesNames.TERMINAL));
        }
    }
}
