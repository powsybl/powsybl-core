/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.anonymizer.Anonymizer;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.*;

/**
 * XML export of an IIDM model.<p>
 * <table border="1">
 *     <tr>
 *         <td><b>property name</b></td>
 *         <td><b>comment</b></td>
 *         <td><b>possible values</b></td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.indent</td>
 *         <td>if true write indented xml (4 spaces)</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.with-branch-state-variables</td>
 *         <td>if true export branches state (active and reactive flow)</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.only-main-cc</td>
 *         <td>if true only export equipments of the main connected component</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.topology-level</td>
 *         <td>the detail level used in the export of voltage levels</td>
 *         <td>NODE_BREAKER, BUS_BREAKER, BUS_BRANCH</td>
 *     </tr>
 * </table>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class XMLExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLExporter.class);

    public static final String INDENT_PROPERTY = "iidm.export.xml.indent";

    public static final String WITH_BRANCH_STATE_VARIABLES_PROPERTY = "iidm.export.xml.with-branch-state-variables";

    public static final String ONLY_MAIN_CC_PROPERTIES = "iidm.export.xml.only-main-cc";

    public static final String ANONYMISED_PROPERTIES = "iidm.export.xml.anonymised";

    public static final String SKIP_EXTENSIONS_PROPERTIES = "iidm.export.xml.skip-extensions";

    public static final String TOPOLOGY_LEVEL_PROPERTY = "iidm.export.xml.topology-level";

    @Override
    public String getFormat() {
        return "XIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM XML v" + VERSION + " exporter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }

        ExportOptions options = new ExportOptions();
        if (parameters != null) {
            options.setIndent(Boolean.parseBoolean(parameters.getProperty(INDENT_PROPERTY, Boolean.TRUE.toString())))
                .setWithBranchSV(Boolean.parseBoolean(parameters.getProperty(WITH_BRANCH_STATE_VARIABLES_PROPERTY, Boolean.TRUE.toString())))
                .setOnlyMainCc(Boolean.parseBoolean(parameters.getProperty(ONLY_MAIN_CC_PROPERTIES, Boolean.FALSE.toString())))
                .setAnonymized(Boolean.parseBoolean(parameters.getProperty(ANONYMISED_PROPERTIES, Boolean.FALSE.toString())))
                .setSkipExtensions(Boolean.parseBoolean(parameters.getProperty(SKIP_EXTENSIONS_PROPERTIES, Boolean.FALSE.toString())))
                .setTopologyLevel(TopologyLevel.valueOf(parameters.getProperty(TOPOLOGY_LEVEL_PROPERTY, TopologyLevel.NODE_BREAKER.name())));
        }

        try {
            long startTime = System.currentTimeMillis();

            try (OutputStream os = dataSource.newOutputStream(null, "xiidm", false);
                 BufferedOutputStream bos = new BufferedOutputStream(os)) {
                Anonymizer anonymizer = NetworkXml.write(network, options, bos);
                if (anonymizer != null) {
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dataSource.newOutputStream("_mapping", "csv", false), StandardCharsets.UTF_8))) {
                        anonymizer.write(writer);
                    }
                }
            }

            LOGGER.debug("XIIDM export done in {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
