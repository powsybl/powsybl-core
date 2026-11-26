/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Replaces the id of every named identifiable by its name, keeping the original id
 * reachable as an alias of type {@link #ALIAS_TYPE_ORIGINAL_ID}.
 * <p>
 * An id is changed only when the new id can be used as a unique identifier in the network.
 * The following checks are applied in order. The first one that fails skips the change,
 * and the corresponding {@link ChangeSkippedReason} is reported:
 * <ul>
 *   <li>The identifiable must not be a {@code Network}, including subnetworks
 *       ({@link ChangeSkippedReason#NETWORK}). Changing network ids is out of scope.</li>
 *   <li>The identifiable must not already have an alias of type {@link #ALIAS_TYPE_ORIGINAL_ID}
 *       ({@link ChangeSkippedReason#ORIGINAL_ID_ALIAS_ALREADY_PRESENT}). As a consequence, ids
 *       replaced by a previous application of this modification are left untouched.</li>
 *   <li>The identifiable must have a name, and it must not be empty or blank ({@link ChangeSkippedReason#NO_NAME}).
 *       An empty or blank name can not be used as an id.</li>
 *   <li>The name must be different from the id ({@link ChangeSkippedReason#NAME_EQUALS_ID}).</li>
 *   <li>The name must be unique among all the identifiables considered. A duplicated name cannot be
 *       used directly: the first identifiable processed would take it and the others would be left
 *       out, making the result dependent on the iteration order. The behavior depends on the
 *       {@link #CONFIG_SUFFIX_WITH_ID_IF_NAME_IS_DUPLICATED} parameter:
 *       <ul>
 *         <li>{@code true} (default): the new id is the name suffixed with the original id,
 *             {@code name + "_" + originalId}. This new id must not already be in use as an id or
 *             as an alias ({@link ChangeSkippedReason#SUFFIXED_NAME_IS_ALREADY_AN_ID}).</li>
 *         <li>{@code false}: the change is skipped ({@link ChangeSkippedReason#DUPLICATED_NAME}).</li>
 *       </ul></li>
 *   <li>A unique name must not already be in use as an id or as an alias of another identifiable
 *       ({@link ChangeSkippedReason#UNIQUE_NAME_IS_ALREADY_AN_ID}).
 *       {@code Network.getIdentifiable} resolves aliases, so a single lookup covers both cases.
 *       This happens, for example, with the fictitious switches created for disconnected CGMES
 *       terminals: they are named after their terminal, and that identifier is already registered
 *       as an alias of the equipment owning the terminal.</li>
 * </ul>
 * <p>
 * Identifiables whose change is skipped keep their original id. When the
 * {@link #CONFIG_REPORT_SKIPPED_CHANGES} parameter is {@code true}
 * (default is {@code false}), skipped changes are
 * reported under a dedicated child of the given report node, with the id, name, type and reason.
 * This child is created only when at least one skipped change is reported.
 * Only reasons that indicate a conflict are reported (see {@link ChangeSkippedReason#isReported()}):
 * identifiables that are networks, have no name, or whose name already equals their id are skipped silently.
 * <p>
 * Notes on the implementation:
 * <ul>
 *   <li>The identifiables are collected into a list first: {@code setId} mutates the
 *       network index, and {@code getIdentifiables()} is a live view of it.</li>
 *   <li>{@code setId} must be called before {@code addAlias}: if the object still has
 *       the original id, {@code NetworkIndex.addAlias} would silently ignore the request.</li>
 * </ul>
 * <p>
 * Parameters are read from the optional platform configuration module {@value #CONFIG_MODULE_NAME}:
 * <pre>
 * replace-ids-by-names-default-parameters:
 *   suffix-with-id-if-name-is-duplicated: true
 *   report-skipped-changes: false
 * </pre>
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class ReplaceIdsByNames extends AbstractNetworkModification {

    public static final String NAME = "ReplaceIdsByNames";

    public static final String CONFIG_MODULE_NAME = "replace-ids-by-names-default-parameters";
    public static final String CONFIG_SUFFIX_WITH_ID_IF_NAME_IS_DUPLICATED = "suffix-with-id-if-name-is-duplicated";
    public static final String CONFIG_REPORT_SKIPPED_CHANGES = "report-skipped-changes";
    public static final String ALIAS_TYPE_ORIGINAL_ID = "original-id";

    public static final String REPORT_SKIPPED_REPORT = "core.iidm.modification.replace-ids-by-names.skipped-changes";
    public static final String REPORT_SKIPPED_MESSAGE = "core.iidm.modification.replace-ids-by-names.skipped-change";
    public static final String REPORT_SKIPPED_TYPE = "type";
    public static final String REPORT_SKIPPED_ID = "id";
    public static final String REPORT_SKIPPED_NAME = "name";
    public static final String REPORT_SKIPPED_REASON = "reason";

    private final Params params;

    public ReplaceIdsByNames() {
        this(PlatformConfig.defaultConfig());
    }

    public ReplaceIdsByNames(PlatformConfig platformConfig) {
        this.params = Params.load(Objects.requireNonNull(platformConfig));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        SkippedChangesReport skippedReport = params.reportSkippedChanges() ? new SkippedChangesReport(reportNode) : null;

        Map<String, Long> nameCount = network.getIdentifiables().stream()
                .filter(i -> !(i instanceof Network))
                .map(ReplaceIdsByNames::nameOf)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        for (Identifiable<?> i : network.getIdentifiables().stream().toList()) {
            String name = nameOf(i);
            switch (checkChange(i, name, nameCount, network)) {
                case Proceed p -> change(i, p.newId());
                case Skip s -> {
                    if (skippedReport != null) {
                        skippedReport.add(i, name, s.reason());
                    }
                }
            }
        }
    }

    /**
     * The name of the identifiable, or {@code null} if it has no name or its name is only blanks.
     */
    private static String nameOf(Identifiable<?> i) {
        return i.getOptionalName().filter(n -> !n.isBlank()).orElse(null);
    }

    private void change(Identifiable<?> i, String newId) {
        String originalId = i.getId();
        i.setId(newId);
        i.addAlias(originalId, ALIAS_TYPE_ORIGINAL_ID);
    }

    private sealed interface ChangeDecision permits Proceed, Skip {
    }

    private record Proceed(String newId) implements ChangeDecision {
    }

    private record Skip(ChangeSkippedReason reason) implements ChangeDecision {
    }

    public enum ChangeSkippedReason {
        NETWORK(false),
        ORIGINAL_ID_ALIAS_ALREADY_PRESENT(true),
        NO_NAME(false),
        NAME_EQUALS_ID(false),
        DUPLICATED_NAME(true),
        SUFFIXED_NAME_IS_ALREADY_AN_ID(true),
        UNIQUE_NAME_IS_ALREADY_AN_ID(true);

        private final boolean reported;

        ChangeSkippedReason(boolean reported) {
            this.reported = reported;
        }

        public boolean isReported() {
            return reported;
        }
    }

    private ChangeDecision checkChange(Identifiable<?> i, String name, Map<String, Long> nameCount, Network network) {
        if (i instanceof Network) {
            return new Skip(ChangeSkippedReason.NETWORK);
        }
        if (i.getAliasFromType(ALIAS_TYPE_ORIGINAL_ID).isPresent()) {
            return new Skip(ChangeSkippedReason.ORIGINAL_ID_ALIAS_ALREADY_PRESENT);
        }
        if (name == null) {
            return new Skip(ChangeSkippedReason.NO_NAME);
        }
        if (name.equals(i.getId())) {
            return new Skip(ChangeSkippedReason.NAME_EQUALS_ID);
        }
        if (nameCount.getOrDefault(name, 0L) != 1) {
            if (!params.suffixWithIdIfNameIsDuplicated()) {
                return new Skip(ChangeSkippedReason.DUPLICATED_NAME);
            }
            String candidate = name + "_" + i.getId();
            return network.getIdentifiable(candidate) == null
                    ? new Proceed(candidate)
                    : new Skip(ChangeSkippedReason.SUFFIXED_NAME_IS_ALREADY_AN_ID);
        }
        if (network.getIdentifiable(name) != null) {
            return new Skip(ChangeSkippedReason.UNIQUE_NAME_IS_ALREADY_AN_ID);
        }
        return new Proceed(name);
    }

    /**
     * Report skipped changes under a child of the given report node.
     * The child is created on the first reported skipped change.
     * The child is not added if there is nothing to be reported.
     * A new instance is used for each application of the modification.
     */
    private static final class SkippedChangesReport {

        private final ReportNode parent;
        private ReportNode header;

        private SkippedChangesReport(ReportNode parent) {
            this.parent = Objects.requireNonNull(parent);
        }

        private void add(Identifiable<?> i, String name, ChangeSkippedReason reason) {
            if (!reason.isReported()) {
                return;
            }
            if (header == null) {
                header = parent.newReportNode()
                        .withMessageTemplate(REPORT_SKIPPED_REPORT)
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add();
            }
            header.newReportNode()
                    .withMessageTemplate(REPORT_SKIPPED_MESSAGE)
                    .withUntypedValue(REPORT_SKIPPED_ID, i.getId())
                    .withUntypedValue(REPORT_SKIPPED_NAME, name == null ? "" : name)
                    .withUntypedValue(REPORT_SKIPPED_TYPE, i.getType().name())
                    .withUntypedValue(REPORT_SKIPPED_REASON, reason.name())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
        }
    }

    private record Params(boolean suffixWithIdIfNameIsDuplicated, boolean reportSkippedChanges) {

        private static final boolean DEFAULT_SUFFIX_WITH_ID_IF_NAME_IS_DUPLICATED = true;
        private static final boolean DEFAULT_REPORT_SKIPPED_CHANGES = false;

        private static Params load(PlatformConfig platformConfig) {
            return platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME)
                    .map(config -> new Params(
                            config.getBooleanProperty(CONFIG_SUFFIX_WITH_ID_IF_NAME_IS_DUPLICATED, DEFAULT_SUFFIX_WITH_ID_IF_NAME_IS_DUPLICATED),
                            config.getBooleanProperty(CONFIG_REPORT_SKIPPED_CHANGES, DEFAULT_REPORT_SKIPPED_CHANGES)))
                    .orElseGet(() -> new Params(DEFAULT_SUFFIX_WITH_ID_IF_NAME_IS_DUPLICATED, DEFAULT_REPORT_SKIPPED_CHANGES));
        }
    }
}
