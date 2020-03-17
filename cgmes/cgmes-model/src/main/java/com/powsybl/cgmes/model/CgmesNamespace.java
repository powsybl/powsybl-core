/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class CgmesNamespace {

    private CgmesNamespace() {
    }

    // cim14 is the CIM version corresponding to ENTSO-E Profile 1
    // It is used in this project to explore how to support future CGMES versions
    // We have sample models in cim14 and we use a different set of queries to obtain data

    public static final String CIM_16_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    public static final String CIM_14_NAMESPACE = "http://iec.ch/TC57/2009/CIM-schema-cim14#";
    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String ENTSOE_NAMESPACE = "http://entsoe.eu/CIM/SchemaExtension/3/1#";
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";
}
