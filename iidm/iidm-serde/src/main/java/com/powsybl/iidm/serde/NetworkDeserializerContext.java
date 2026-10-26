/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;

import java.util.*;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkDeserializerContext extends AbstractNetworkSerDeContext<ImportOptions> implements DeserializerContext {

    private final TreeDataReader reader;

    private final List<DeserializationEndTask> endTasks = new ArrayList<>();
    private final Map<ExtraPropertiesKey, Object> extraPropertiesByIdentifiable = new HashMap<>();
    private final ImportOptions options;

    private final Map<String, String> extensionVersions;
    private final EnumSet<DeserializationEndTask.Step> processedEndTasksSteps = EnumSet.noneOf(DeserializationEndTask.Step.class);

    private ValidationLevel networkValidationLevel;

    private Set<String> ignoredEquipments = new HashSet<>();
    private final Map<IdentifiableIdSide, Collection<String>> selectedOperationalLimitGroupsByIdentifiableId = new HashMap<>();

    public NetworkDeserializerContext(Anonymizer anonymizer, TreeDataReader reader) {
        this(anonymizer, reader, new ImportOptions(), CURRENT_IIDM_VERSION, Collections.emptyMap());
    }

    public NetworkDeserializerContext(Anonymizer anonymizer, TreeDataReader reader, ImportOptions options, IidmVersion version,
                                      Map<String, String> extensionVersions) {
        super(anonymizer, version);
        this.reader = Objects.requireNonNull(reader);
        this.options = Objects.requireNonNull(options);
        this.extensionVersions = extensionVersions;
    }

    @Override
    public TreeDataReader getReader() {
        return reader;
    }

    public void addEndTask(DeserializationEndTask.Step step, Runnable task) {
        endTasks.add(new DeserializationEndTask(step, task));
    }

    public void executeEndTasks(Network network, DeserializationEndTask.Step step, ReportNode reportNode) {
        if (!processedEndTasksSteps.add(step)) {
            // Skip if step was already processed
            return;
        }
        Networks.executeWithReportNode(network, reportNode, () -> endTasks.stream()
                .filter(t -> t.getStep() == step
                        || step == DeserializationEndTask.Step.AFTER_EXTENSIONS &&
                            t.getStep() == DeserializationEndTask.Step.BEFORE_EXTENSIONS && !t.isProcessed()) // If no "extensions" tag was found, BEFORE_EXTENSIONS tasks were not processed.
                .forEach(t -> {
                    t.setProcessed(true);
                    t.getTask().run();
                }));
    }

    @Override
    public ImportOptions getOptions() {
        return options;
    }

    public boolean containsExtensionVersion(String extensionName, String version) {
        return version != null && version.equals(extensionVersions.get(extensionName));
    }

    public Optional<String> getExtensionVersion(ExtensionSerDe<?, ?> extensionSerDe) {
        return Optional.ofNullable(extensionVersions.get(extensionSerDe.getExtensionName()));
    }

    public NetworkDeserializerContext setNetworkValidationLevel(ValidationLevel validationLevel) {
        this.networkValidationLevel = validationLevel;
        return this;
    }

    public ValidationLevel getNetworkValidationLevel() {
        return this.networkValidationLevel;
    }

    public void addIgnoredEquipment(String equipment) {
        ignoredEquipments.add(equipment);
    }

    public boolean isIgnoredEquipment(String equipment) {
        return ignoredEquipments.contains(equipment);
    }

    public void addSelectedGroupIds(String identifiableId, ThreeSides side, Collection<String> selectedGroupIds) {
        selectedOperationalLimitGroupsByIdentifiableId
            .computeIfAbsent(new IdentifiableIdSide(identifiableId, side), id -> new ArrayList<>())
            .addAll(selectedGroupIds);
    }

    public Collection<String> getSelectedGroupIds(String identifiableId, ThreeSides side) {
        IdentifiableIdSide key = new IdentifiableIdSide(identifiableId, side);
        Collection<String> selectedGroupIds = selectedOperationalLimitGroupsByIdentifiableId.get(key);
        if (selectedGroupIds != null) {
            return selectedGroupIds;
        } else {
            throw new NoSuchElementException("No identifiable of ID " + identifiableId + " has had its selected groups stored when deserializing the network");
        }
    }

    public String deanonymizeFromMinimumVersion(String val, IidmVersion version) {
        return getVersion().compareTo(version) >= 0 ? getAnonymizer().deanonymizeString(val) : val;
    }

    /**
     * <p>Define extra properties for the given identifiable and process key.</p>
     * <p>Extra properties can be used to pass additional data required by the end tasks.</p>
     * @param identifiable the identifiable which extra properties are used for
     * @param processKey an additional key to indicate which operation is processed
     * @param extraProperties the extra properties to store
     */
    public void setExtraProperties(Identifiable<?> identifiable, String processKey, Object extraProperties) {
        this.extraPropertiesByIdentifiable.put(new ExtraPropertiesKey(identifiable, processKey), extraProperties);
    }

    /**
     * <p>Get the extra properties associated to the given identifiable and process key.</p>
     * <p>The result is cast to respect the given clazz.</p>
     * @param identifiable the identifiable
     * @param processKey an additional key to indicate which operation is processed
     * @param clazz the class of the object to retrieve
     * @return the associated extra properties
     * @param <T> The type of the extra properties object
     */
    public <T> Optional<T> getExtraProperties(Identifiable<?> identifiable, String processKey, Class<T> clazz) {
        return Optional.ofNullable(extraPropertiesByIdentifiable.get(new ExtraPropertiesKey(identifiable, processKey))).map(clazz::cast);
    }

    /**
     * <p>Remove the extra properties associated to the given identifiable and process key.</p>
     * @param identifiable the identifiable
     */
    public void removeExtraProperties(Identifiable<?> identifiable, String processKey) {
        this.extraPropertiesByIdentifiable.remove(new ExtraPropertiesKey(identifiable, processKey));
    }

    private record ExtraPropertiesKey(String identifiableId, String processKey) {
        ExtraPropertiesKey(Identifiable<?> identifiable, String processKey) {
            this(identifiable.getId(), processKey);
        }
    }

    private record IdentifiableIdSide(String identifiableId, ThreeSides side) { }

}
