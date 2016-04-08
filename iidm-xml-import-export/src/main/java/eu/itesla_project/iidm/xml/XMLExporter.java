/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.auto.service.AutoService;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.export.Exporter;
import eu.itesla_project.iidm.network.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *         <td>iidm.export.xml.force-bus-branch-topo</td>
 *         <td>if true remove switches and aggregate buses</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.only-main-cc</td>
 *         <td>if true only export equipments of the main connected component</td>
 *         <td>true or false</td>
 *     </tr>
 * </table>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class XMLExporter implements Exporter, XmlConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLExporter.class);

    public static final String INDENT_PROPERTY = "iidm.export.xml.indent";

    public static final String WITH_BRANCH_STATE_VARIABLES_PROPERTY = "iidm.export.xml.with-branch-state-variables";

    public static final String FORCE_BUS_BRANCH_TOPO_PROPERTY = "iidm.export.xml.force-bus-branch-topo";

    public static final String ONLY_MAIN_CC_PROPERTIES = "iidm.export.xml.only-main-cc";

    @Override
    public String getFormat() {
        return "XML";
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
        boolean indent = true;
        if (parameters != null) {
            indent = !"false".equals(parameters.getProperty(INDENT_PROPERTY));
        }
        boolean withBranchSV = (parameters != null
                && "true".equals(parameters.getProperty(WITH_BRANCH_STATE_VARIABLES_PROPERTY)));
        boolean forceBusBranchTopo = false;
        if (parameters != null) {
            forceBusBranchTopo = "true".equals(parameters.getProperty(FORCE_BUS_BRANCH_TOPO_PROPERTY, "false"));
        }
        boolean onlyMainCc = false;
        if (parameters != null) {
            onlyMainCc = "true".equals(parameters.getProperty(ONLY_MAIN_CC_PROPERTIES));
        }

        XMLExportOptions options = new XMLExportOptions(withBranchSV, forceBusBranchTopo, indent, onlyMainCc);
        try {
            long startTime = System.currentTimeMillis();

            try (OutputStream os = dataSource.newOutputStream(null, "xml", false);
                 BufferedOutputStream bos = new BufferedOutputStream(os)) {
                NetworkXml.write(network, options, bos);
            }

            LOGGER.debug("XML export done in {} ms", (System.currentTimeMillis() - startTime));
        } catch (XMLStreamException|IOException e) {
            throw new RuntimeException(e);
        }
    }
}
