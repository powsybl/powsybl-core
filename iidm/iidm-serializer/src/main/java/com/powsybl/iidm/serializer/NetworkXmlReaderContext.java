/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.iidm.serializer.anonymizer.Anonymizer;

import java.util.*;

import static com.powsybl.iidm.serializer.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkXmlReaderContext extends AbstractNetworkXmlContext<ImportOptions> implements XmlReaderContext {

    private final TreeDataReader reader;

    private final List<Runnable> endTasks = new ArrayList<>();
    private final ImportOptions options;

    private final Map<String, String> extensionVersions;

    public NetworkXmlReaderContext(Anonymizer anonymizer, TreeDataReader reader) {
        this(anonymizer, reader, new ImportOptions(), CURRENT_IIDM_XML_VERSION, Collections.emptyMap());
    }

    public NetworkXmlReaderContext(Anonymizer anonymizer, TreeDataReader reader, ImportOptions options, IidmXmlVersion version,
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

    @Override
    public ImportOptions getOptions() {
        return options;
    }

    public boolean containsExtensionVersion(String extensionName, String version) {
        return version != null && version.equals(extensionVersions.get(extensionName));
    }

    public Optional<String> getExtensionVersion(ExtensionXmlSerializer<?, ?> extensionXmlSerializer) {
        return Optional.ofNullable(extensionVersions.get(extensionXmlSerializer.getExtensionName()));
    }
}
