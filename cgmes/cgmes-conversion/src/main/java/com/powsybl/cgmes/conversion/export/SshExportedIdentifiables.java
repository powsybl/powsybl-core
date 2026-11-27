/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.events.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.util.Map.entry;

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

    /** The capability of all the connection attributes of the terminals. */
    private static final String CONNECTED = "connected";

    /** The connection attributes of the terminals: connected, connected1, connected_dc1, ... */
    private static final Pattern CONNECTION = Pattern.compile("beginConnect|endConnect|beginDisconnect|endDisconnect|connected\\w*");

    /** The leg of a tap changer of a three windings transformer: ratioTapChanger2.tapPosition, ... */
    private static final Pattern TAP_CHANGER_LEG = Pattern.compile("^(ratio|phase)TapChanger\\d");

    /** The capability of the creation of an extension. */
    private static final String CREATION = "";

    private static final String RATIO_TAP_CHANGER = "ratioTapChanger.";
    private static final String PHASE_TAP_CHANGER = "phaseTapChanger.";
    private static final Set<String> TRANSFORMER = Set.of(CONNECTED,
        RATIO_TAP_CHANGER + "tapPosition", RATIO_TAP_CHANGER + REGULATING, RATIO_TAP_CHANGER + TARGET_VALUE, RATIO_TAP_CHANGER + TARGET_DEADBAND,
        PHASE_TAP_CHANGER + "tapPosition", PHASE_TAP_CHANGER + "regulating", PHASE_TAP_CHANGER + "regulationValue", PHASE_TAP_CHANGER + "targetDeadband");

    /** The attributes that the SSH data of each type of identifiable contains. */
    private static final Map<Class<? extends Identifiable>, Set<String>> SUPPORTED_ATTRIBUTES = Map.ofEntries(
        entry(Switch.class, Set.of(OPEN)),
        entry(DcSwitch.class, Set.of(OPEN)),
        entry(BusbarSection.class, Set.of(CONNECTED)),
        entry(Ground.class, Set.of(CONNECTED)),
        entry(Line.class, Set.of(CONNECTED)),
        entry(DcLine.class, Set.of(CONNECTED)),
        entry(DcGround.class, Set.of(CONNECTED)),
        entry(Load.class, Set.of(CONNECTED, P0, Q0)),
        // The deadband of a generator is not written: only discrete regulations have a deadband
        entry(Generator.class, Set.of(CONNECTED, TARGET_P, LOCAL_TARGET_Q, LOCAL_TARGET_V, REGULATING, TARGET_VALUE)),
        // A battery has no regulating control in the SSH
        entry(Battery.class, Set.of(CONNECTED, TARGET_P, LOCAL_TARGET_Q, REGULATING)),
        entry(ShuntCompensator.class, Set.of(CONNECTED, "sectionCount", LOCAL_TARGET_V, REGULATING, TARGET_VALUE, TARGET_DEADBAND)),
        entry(StaticVarCompensator.class, Set.of(CONNECTED, LOCAL_TARGET_Q, LOCAL_TARGET_V, REGULATING, TARGET_VALUE)),
        // The regulation mode of a VSC converter is its qPccControl, and the SSH has no regulation status for it
        entry(VscConverterStation.class, Set.of(CONNECTED, LOCAL_TARGET_Q, LOCAL_TARGET_V, TARGET_VALUE, REGULATION_MODE)),
        entry(LccConverterStation.class, Set.of(CONNECTED, "powerFactor")),
        entry(HvdcLine.class, Set.of("activePowerSetpoint", "convertersMode")),
        // A VsConverter of the detailed DC model has no control flag
        entry(VoltageSourceConverter.class, Set.of(CONNECTED, TARGET_P, "targetVdc", "controlMode", LOCAL_TARGET_Q, LOCAL_TARGET_V, TARGET_VALUE, REGULATION_MODE)),
        entry(LineCommutatedConverter.class, Set.of(CONNECTED, TARGET_P, "targetVdc", "controlMode")),
        entry(TwoWindingsTransformer.class, TRANSFORMER),
        entry(ThreeWindingsTransformer.class, TRANSFORMER),
        // The target active and reactive power of the generation of a boundary line are not written
        entry(BoundaryLine.class, Set.of(CONNECTED, P0, Q0, "targetV", "voltageRegulationOn")),
        entry(Area.class, Set.of("interchangeTarget")));

    /**
     * The supported changes of each extension. Of the active power control, only the participation factor is written, as GeneratingUnit.normalPF.
     */
    private static final Map<String, Set<String>> SUPPORTED_EXTENSION_ATTRIBUTES = Map.of(
        ActivePowerControl.NAME, Set.of(CREATION, "participationFactor"),
        ReferencePriorities.NAME, Set.of(CREATION, "referencePriority"));

    private SshExportedIdentifiables() {
    }

    /**
     * The changes that a partial SSH export can write, as a map from a type to its supported changes.
     * <ul>
     *     <li>The key of an identifiable is the simple name of its IIDM interface, for example {@code Generator}.
     *     Its changes are the attributes of the events, for example {@code targetP} or {@code VoltageRegulation.TargetValue}.
     *     The change {@code connected} contains all the connection attributes of the terminals,
     *     and a tap changer of a three windings transformer has no leg number: {@code ratioTapChanger.tapPosition}.</li>
     *     <li>The key of an extension is its name, for example {@code activePowerControl}.
     *     Its changes are the attributes of the events, and the empty string for the creation of the extension.</li>
     * </ul>
     * A supported change can have a condition: a change of {@code localTargetV} is supported only with a local voltage regulation.
     */
    public static Map<String, Set<String>> capabilities() {
        Map<String, Set<String>> capabilities = new TreeMap<>();
        SUPPORTED_ATTRIBUTES.forEach((type, attributes) -> capabilities.put(type.getSimpleName(), Collections.unmodifiableSet(new TreeSet<>(attributes))));
        SUPPORTED_EXTENSION_ATTRIBUTES.forEach((name, attributes) -> capabilities.put(name, Collections.unmodifiableSet(new TreeSet<>(attributes))));
        return Collections.unmodifiableMap(capabilities);
    }

    /**
     * The change of {@link #capabilities()} that contains the attribute of an event: {@code connected} for a connection attribute,
     * and the attribute without the leg number for a tap changer of a three windings transformer.
     */
    static String capability(String attribute) {
        if (CONNECTION.matcher(attribute).matches()) {
            return CONNECTED;
        }
        return TAP_CHANGER_LEG.matcher(attribute).replaceFirst("$1TapChanger");
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
            case ExtensionCreationNetworkEvent e -> isSupportedExtensionAttribute(e.extensionName(), CREATION);
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
        if (LOCAL_TARGET_V.equals(attribute) && identifiable instanceof VoltageRegulationHolder<?> holder && !writesLocalTargetV(holder)) {
            return false;
        }
        String capability = capability(attribute);
        return SUPPORTED_ATTRIBUTES.entrySet().stream()
            .anyMatch(e -> e.getKey().isInstance(identifiable) && e.getValue().contains(capability));
    }

    /**
     * True if the SSH data of the holder contains its local target voltage.
     * The SSH writes it only as the target value of a local voltage regulation,
     * except for an EquivalentInjection, which writes it as its regulation target.
     */
    private static boolean writesLocalTargetV(VoltageRegulationHolder<?> holder) {
        if (holder instanceof Generator generator && CgmesNames.EQUIVALENT_INJECTION.equals(generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS))) {
            return true;
        }
        VoltageRegulation regulation = holder.getVoltageRegulation();
        return regulation != null && regulation.getMode() == RegulationMode.VOLTAGE && regulation.getTerminal() == null;
    }

    /**
     * True if the SSH data contains the value of the extension attribute, or the extension if the attribute is {@link #CREATION}.
     */
    private static boolean isSupportedExtensionAttribute(String extensionName, String attribute) {
        return SUPPORTED_EXTENSION_ATTRIBUTES.getOrDefault(extensionName, Set.of()).contains(attribute);
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
