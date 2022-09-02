/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.iidm.xml.anonymizer.Anonymizer;

import javax.xml.stream.XMLStreamReader;
import java.util.*;
import java.util.stream.Stream;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlReaderContext extends AbstractNetworkXmlContext<ImportOptions> implements XmlReaderContext {

    private final XMLStreamReader reader;

    private final List<Runnable> endTasks = new ArrayList<>();
    private final ImportOptions options;

    private final Set<String> extensionsNamespaceUri = new HashSet<>();

    public NetworkXmlReaderContext(Anonymizer anonymizer, XMLStreamReader reader) {
        this(anonymizer, reader, new ImportOptions(), CURRENT_IIDM_XML_VERSION);
    }

    public NetworkXmlReaderContext(Anonymizer anonymizer, XMLStreamReader reader, ImportOptions options, IidmXmlVersion version) {
        super(anonymizer, version);
        this.reader = Objects.requireNonNull(reader);
        this.options = Objects.requireNonNull(options);
    }

    @Override
    public XMLStreamReader getReader() {
        return reader;
    }

    public List<Runnable> getEndTasks() {
        return endTasks;
    }

    @Override
    public ImportOptions getOptions() {
        return options;
    }

    public void buildExtensionNamespaceUriList(Stream<ExtensionXmlSerializer> providers) {
        providers.filter(e -> reader.getNamespaceURI(e.getNamespacePrefix()) != null)
                .forEach(e -> extensionsNamespaceUri.add(reader.getNamespaceURI(e.getNamespacePrefix())));
    }

    public boolean containsExtensionNamespaceUri(String extensionNamespaceUri) {
        return extensionsNamespaceUri.contains(extensionNamespaceUri);
    }

    public Optional<String> getExtensionVersion(ExtensionXmlSerializer<?, ?> extensionXmlSerializer) {
        return extensionXmlSerializer.getVersions()
                .stream()
                .filter(v -> extensionsNamespaceUri
                        .stream()
                        .anyMatch(uri -> extensionXmlSerializer.getNamespaceUri(v).equals(uri)))
                .findFirst();
    }
}
