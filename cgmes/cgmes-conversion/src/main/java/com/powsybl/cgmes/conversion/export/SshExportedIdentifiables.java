/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.events.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Build the identifiables of a partial SSH export from the events recorded on a network,
 * for example with a {@link com.powsybl.iidm.network.NetworkEventRecorder}.
 *
 * <p>A partial SSH export writes all the SSH data of each changed identifiable.
 * A change is supported only if this SSH data contains the changed value.
 * The other changes are unsupported: the creation or the removal of an identifiable,
 * a change of equipment data (for example the resistance of a line), a change of a property,
 * the removal of an extension, or a change recorded on another variant.
 * {@link UnsupportedChangeBehavior} tells what to do with them.</p>
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class SshExportedIdentifiables {

    /**
     * What to do with a recorded change that a partial SSH export cannot write.
     */
    public enum UnsupportedChangeBehavior {
        /** Throw a {@link PowsyblException} at the first unsupported change. */
        FAIL,
        /** Log a warning for each unsupported change, and leave it out of the export. */
        IGNORE
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SshExportedIdentifiables.class);
    private static final String SUPPORTED_CHANGES_DOC = "See the section \"Supported changes\" "
        + "of the CGMES export documentation (docs/grid_exchange_formats/cgmes/export.md).";

    private static final String OPEN = "open";
    private static final String P0 = "p0";
    private static final String Q0 = "q0";
    private static final String TARGET_P = "targetP";
    private static final String LOCAL_TARGET_Q = "localTargetQ";
    private static final String LOCAL_TARGET_V = "localTargetV";
    private static final String VOLTAGE_REGULATION = "VoltageRegulation.";
    private static final String REGULATING = VOLTAGE_REGULATION + "isRegulating";
    private static final String TARGET_VALUE = VOLTAGE_REGULATION + "TargetValue";
    private static final String TARGET_DEADBAND = VOLTAGE_REGULATION + "TargetDeadband";
    private static final String REGULATION_MODE = VOLTAGE_REGULATION + "RegulationMode";

    /** The connection attributes of the terminals: connected, connected1, connected_dc1, ... */
    private static final Pattern CONNECTION = Pattern.compile("beginConnect|endConnect|beginDisconnect|endDisconnect|connected\\w*");

    /** The attributes of a tap changer, reported on its transformer: ratioTapChanger.tapPosition, phaseTapChanger2.regulating, ... */
    private static final Pattern TAP_CHANGER = Pattern.compile("(ratio|phase)TapChanger\\d?\\."
        + "(tapPosition|regulating|regulationValue|targetDeadband|VoltageRegulation\\.(isRegulating|TargetValue|TargetDeadband))");

    private SshExportedIdentifiables() {
    }

    /**
     * The identifiables changed by the events, each one only once, in the order of their first event.
     * The events that are not a change of the network state, for example the creation of a variant, are skipped.
     *
     * @param network the network of the events. Its working variant is the exported variant
     * @param events the recorded events
     * @param behavior what to do with a change that the partial SSH export cannot write
     * @throws PowsyblException with {@link UnsupportedChangeBehavior#FAIL}, at the first unsupported change
     */
    public static Set<Identifiable<?>> fromEvents(Network network, Collection<NetworkEvent> events, UnsupportedChangeBehavior behavior) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(events);
        Objects.requireNonNull(behavior);
        String workingVariantId = network.getVariantManager().getWorkingVariantId();
        Set<Identifiable<?>> identifiables = new LinkedHashSet<>();
        for (NetworkEvent event : events) {
            if (event instanceof VariantNetworkEvent) {
                continue;
            }
            String reason = unsupportedReason(network, event, workingVariantId);
            if (reason == null) {
                identifiables.add(network.getIdentifiable(identifiableId(event)));
            } else {
                unsupported(event, reason, behavior);
            }
        }
        return identifiables;
    }

    /**
     * Why the partial SSH export cannot write the change, or null if it can write it.
     */
    private static String unsupportedReason(Network network, NetworkEvent event, String workingVariantId) {
        String variantId = switch (event) {
            case UpdateNetworkEvent e -> e.variantId();
            case ExtensionUpdateNetworkEvent e -> e.variantId();
            default -> null;
        };
        // An attribute that does not depend on the variant has no variant id: its change applies to all the variants
        if (variantId != null && !variantId.equals(workingVariantId)) {
            return "the change was recorded on variant " + variantId + ", but the exported variant is " + workingVariantId;
        }
        String id = identifiableId(event);
        if (id == null) {
            return "only the changes of attributes and extensions can be exported, but this is a " + event.getClass().getSimpleName();
        }
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            return "the network has no identifiable with id " + id;
        }
        boolean supported = switch (event) {
            case UpdateNetworkEvent e -> isSupportedAttribute(identifiable, e.attribute());
            case ExtensionCreationNetworkEvent e -> isSupportedExtension(e.extensionName());
            case ExtensionUpdateNetworkEvent e -> isSupportedExtensionAttribute(e.extensionName(), e.attribute());
            default -> false;
        };
        return supported ? null : "no SSH data of " + identifiable.getType() + " contains this value";
    }

    /**
     * The id of the identifiable of a change of an attribute or of an extension, or null for the other events.
     */
    private static String identifiableId(NetworkEvent event) {
        return switch (event) {
            case UpdateNetworkEvent e -> e.id();
            case ExtensionCreationNetworkEvent e -> e.id();
            case ExtensionUpdateNetworkEvent e -> e.id();
            default -> null;
        };
    }

    /**
     * True if the SSH data of the identifiable contains the value of the attribute.
     */
    private static boolean isSupportedAttribute(Identifiable<?> identifiable, String attribute) {
        if ((identifiable instanceof Connectable<?> || identifiable instanceof DcConnectable<?>) && CONNECTION.matcher(attribute).matches()) {
            // The SSH data of a connectable contains the connection state of its terminals
            return true;
        }
        return switch (identifiable) {
            case Switch ignored -> OPEN.equals(attribute);
            case DcSwitch ignored -> OPEN.equals(attribute);
            case Load ignored -> Set.of(P0, Q0).contains(attribute);
            // The deadband of a generator is not written: only discrete regulations have a deadband
            case Generator ignored -> Set.of(TARGET_P, LOCAL_TARGET_Q, LOCAL_TARGET_V, REGULATING, TARGET_VALUE).contains(attribute);
            // A battery has no regulating control in the SSH
            case Battery ignored -> Set.of(TARGET_P, LOCAL_TARGET_Q, REGULATING).contains(attribute);
            case ShuntCompensator ignored -> Set.of("sectionCount", LOCAL_TARGET_V, REGULATING, TARGET_VALUE, TARGET_DEADBAND).contains(attribute);
            case StaticVarCompensator ignored -> Set.of(LOCAL_TARGET_Q, LOCAL_TARGET_V, REGULATING, TARGET_VALUE).contains(attribute);
            // The regulation mode of a VSC converter is its qPccControl
            case VscConverterStation ignored -> Set.of(LOCAL_TARGET_Q, LOCAL_TARGET_V, REGULATING, TARGET_VALUE, REGULATION_MODE).contains(attribute);
            case LccConverterStation ignored -> "powerFactor".equals(attribute);
            case HvdcLine ignored -> Set.of("activePowerSetpoint", "convertersMode").contains(attribute);
            // A VsConverter of the detailed DC model has no control flag
            case VoltageSourceConverter ignored -> Set.of(TARGET_P, "targetVdc", "controlMode", LOCAL_TARGET_Q, LOCAL_TARGET_V, TARGET_VALUE, REGULATION_MODE).contains(attribute);
            case LineCommutatedConverter ignored -> Set.of(TARGET_P, "targetVdc", "controlMode").contains(attribute);
            case TwoWindingsTransformer ignored -> TAP_CHANGER.matcher(attribute).matches();
            case ThreeWindingsTransformer ignored -> TAP_CHANGER.matcher(attribute).matches();
            // The target active and reactive power of the generation of a boundary line are not written
            case BoundaryLine ignored -> Set.of(P0, Q0, "targetV", "voltageRegulationOn").contains(attribute);
            case Area ignored -> "interchangeTarget".equals(attribute);
            // The fictitious injection of a bus of a bus-breaker voltage level
            case Bus ignored -> Set.of("fictitiousP0", "fictitiousQ0").contains(attribute);
            default -> false;
        };
    }

    private static boolean isSupportedExtension(String extensionName) {
        return ActivePowerControl.NAME.equals(extensionName) || ReferencePriorities.NAME.equals(extensionName);
    }

    /**
     * True if the SSH data contains the value of the extension attribute.
     * Of the active power control, only the participation factor is written, as GeneratingUnit.normalPF.
     */
    private static boolean isSupportedExtensionAttribute(String extensionName, String attribute) {
        return ActivePowerControl.NAME.equals(extensionName) && "participationFactor".equals(attribute)
            || ReferencePriorities.NAME.equals(extensionName);
    }

    private static void unsupported(NetworkEvent event, String reason, UnsupportedChangeBehavior behavior) {
        String message = "Change can not be exported to a partial SSH: " + reason + ". Change: " + describe(event) + ". " + SUPPORTED_CHANGES_DOC;
        if (behavior == UnsupportedChangeBehavior.FAIL) {
            throw new PowsyblException(message);
        }
        LOGGER.warn("{}", message);
    }

    private static String describe(NetworkEvent event) {
        return switch (event) {
            case UpdateNetworkEvent e -> e.id() + "." + e.attribute();
            case ExtensionUpdateNetworkEvent e -> e.id() + "." + e.extensionName() + "." + e.attribute();
            case ExtensionCreationNetworkEvent e -> e.id() + "." + e.extensionName();
            default -> String.valueOf(event);
        };
    }
}
