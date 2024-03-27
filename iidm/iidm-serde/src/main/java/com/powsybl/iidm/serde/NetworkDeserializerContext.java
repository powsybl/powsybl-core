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
import com.powsybl.iidm.network.Network;
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

    private final List<Runnable> endTasks = new ArrayList<>();
    private final ImportOptions options;

    private final Map<String, String> extensionVersions;

    private ValidationLevel networkValidationLevel;

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

    public List<Runnable> getEndTasks() {
        return endTasks;
    }

    public void executeEndTasks(Network network, ReportNode reportNode) {
        Networks.executeWithReportNode(network, reportNode, () -> getEndTasks().forEach(Runnable::run));
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
}
