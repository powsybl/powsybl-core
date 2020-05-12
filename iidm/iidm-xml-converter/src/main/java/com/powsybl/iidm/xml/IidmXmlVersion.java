/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public enum IidmXmlVersion {
    V_1_0("itesla_project.eu", ImmutableList.of(1, 0)),
    V_1_1("powsybl.org", ImmutableList.of(1, 1)),
    V_1_2("powsybl.org", ImmutableList.of(1, 2)),
    V_1_3("powsybl.org", ImmutableList.of(1, 3));

    private final String domain;
    private final List<Integer> versionArray;

    IidmXmlVersion(String domain, List<Integer> versionArray) {
        this.domain = domain;
        this.versionArray = versionArray;
    }

    public String toString(String separator) {
        return versionArray.stream().map(Object::toString).collect(Collectors.joining(separator));
    }

    public String getNamespaceURI() {
        return "http://www." + domain + "/schema/iidm/" + toString("_");
    }

    public String getXsd() {
        return "iidm_V" + toString("_") + ".xsd";
    }

    public static IidmXmlVersion fromNamespaceURI(String namespaceURI) {
        String version = namespaceURI.substring(namespaceURI.lastIndexOf('/') + 1);
        IidmXmlVersion v = of(version, "_");
        String namespaceUriV = v.getNamespaceURI();
        if (!namespaceURI.equals(namespaceUriV)) {
            throw new PowsyblException("Namespace " + namespaceURI + " is not supported. " +
                    "The namespace for IIDM XML version " + v.toString(".") + " is: " + namespaceUriV + ".");
        }
        return v;
    }

    public static IidmXmlVersion of(String version, String separator) {
        return Stream.of(IidmXmlVersion.values())
                .filter(v -> version.equals(v.toString(separator)))
                .findFirst() // there can only be 0 or exactly 1 match
                .orElseThrow(() -> new PowsyblException("Version " + version + " is not supported."));
    }
}
