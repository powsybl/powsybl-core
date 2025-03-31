/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class CgmesNamespace {

    private CgmesNamespace() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(CgmesNamespace.class);

    public static final String CIM_100_NAMESPACE = "http://iec.ch/TC57/CIM100#";
    public static final String CIM_16_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";

    private static final Set<String> VALID_CIM_NAMESPACES = Set.of(CIM_16_NAMESPACE, CIM_100_NAMESPACE);
    private static final Pattern CIM_100_PLUS_NAMESPACE_PATTERN = Pattern.compile(".*/CIM\\d+#$");

    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String ENTSOE_NAMESPACE = "http://entsoe.eu/CIM/SchemaExtension/3/1#";
    public static final String EU_NAMESPACE = "http://iec.ch/TC57/CIM100-European#";
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";

    public static final String CIM_16_EQ_PROFILE = "http://entsoe.eu/CIM/EquipmentCore/3/1";
    public static final String CIM_16_EQ_OPERATION_PROFILE = "http://entsoe.eu/CIM/EquipmentOperation/3/1";
    public static final String CIM_16_TP_PROFILE = "http://entsoe.eu/CIM/Topology/4/1";
    public static final String CIM_16_SV_PROFILE = "http://entsoe.eu/CIM/StateVariables/4/1";
    public static final String CIM_16_SSH_PROFILE = "http://entsoe.eu/CIM/SteadyStateHypothesis/1/1";
    public static final String CIM_16_EQ_BD_PROFILE = "http://entsoe.eu/CIM/EquipmentBoundary/3/1";
    public static final String CIM_16_TP_BD_PROFILE = "http://entsoe.eu/CIM/TopologyBoundary/3/1";

    public static final String CGMES_EQ_3_OR_GREATER_PREFIX = "http://iec.ch/TC57/ns/CIM/CoreEquipment-EU/";
    public static final String CIM_100_EQ_PROFILE = "http://iec.ch/TC57/ns/CIM/CoreEquipment-EU/3.0";
    public static final String CIM_100_EQ_OPERATION_PROFILE = "http://iec.ch/TC57/ns/CIM/Operation-EU/3.0";
    public static final String CIM_100_TP_PROFILE = "http://iec.ch/TC57/ns/CIM/Topology-EU/3.0";
    public static final String CIM_100_SV_PROFILE = "http://iec.ch/TC57/ns/CIM/StateVariables-EU/3.0";
    public static final String CIM_100_SSH_PROFILE = "http://iec.ch/TC57/ns/CIM/SteadyStateHypothesis-EU/3.0";
    public static final String CIM_100_EQ_BD_PROFILE = "http://iec.ch/TC57/ns/CIM/EquipmentBoundary-EU/3.0";

    public static final Cim CIM_16 = new Cim16();
    public static final Cim CIM_100 = new Cim100();

    public static final List<Cim> CIM_LIST = List.of(CIM_16, CIM_100);

    public interface Cim {

        int getVersion();

        String getNamespace();

        boolean hasProfileUri(String profileUri);

        String getProfileUri(String profile);

        String getProfile(String profileUri);

        String getEuPrefix();

        String getEuNamespace();
    }

    private abstract static class AbstractCim implements Cim {
        private final int version;
        private final String namespace;
        private final String euPrefix;
        private final String euNamespace;
        private final BiMap<String, String> profiles = HashBiMap.create();

        private AbstractCim(int version, String namespace, String euPrefix, String euNamespace, Map<String, String> profiles) {
            this.version = version;
            this.namespace = namespace;
            this.euPrefix = euPrefix;
            this.euNamespace = euNamespace;
            this.profiles.putAll(profiles);
        }

        @Override
        public int getVersion() {
            return version;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public String getEuPrefix() {
            return euPrefix;
        }

        @Override
        public String getEuNamespace() {
            return euNamespace;
        }

        @Override
        public boolean hasProfileUri(String profileUri) {
            return profiles.containsValue(profileUri);
        }

        @Override
        public String getProfileUri(String profile) {
            return profiles.get(profile);
        }

        @Override
        public String getProfile(String profileUri) {
            return profiles.inverse().get(profileUri);
        }
    }

    private static final class Cim16 extends AbstractCim {

        private Cim16() {
            super(16, CIM_16_NAMESPACE, "entsoe", ENTSOE_NAMESPACE,
                    Map.of("EQ", CIM_16_EQ_PROFILE,
                            "EQ_OP", CIM_16_EQ_OPERATION_PROFILE,
                            "SSH", CIM_16_SSH_PROFILE,
                            "SV", CIM_16_SV_PROFILE,
                            "TP", CIM_16_TP_PROFILE,
                            "EQ_BD", CIM_16_EQ_BD_PROFILE,
                            "TP_BD", CIM_16_TP_BD_PROFILE));
        }
    }

    private static final class Cim100 extends AbstractCim {

        private Cim100() {
            super(100, CIM_100_NAMESPACE, "eu", EU_NAMESPACE,
                    Map.of("EQ", CIM_100_EQ_PROFILE,
                            "EQ_OP", CIM_100_EQ_OPERATION_PROFILE,
                            "SSH", CIM_100_SSH_PROFILE,
                            "SV", CIM_100_SV_PROFILE,
                            "TP", CIM_100_TP_PROFILE,
                            "EQ_BD", CIM_100_EQ_BD_PROFILE));
        }
    }

    public static boolean isValid(String ns) {
        // Until CIM16 the CIM namespace contained the string "CIM-schema-cim<versionNumber>#"
        // Since CIM100 the namespace seems to follow the pattern "/CIM<versionNumber>#"
        return VALID_CIM_NAMESPACES.contains(ns) || CIM_100_PLUS_NAMESPACE_PATTERN.matcher(ns).matches();
    }

    public static Cim getCim(int cimVersion) {
        switch (cimVersion) {
            case 16:
                return CIM_16;
            case 100:
                return CIM_100;
            default:
                if (cimVersion > 100) {
                    LOG.info("CIM version is above 100 ({}), will be considered 100", cimVersion);
                    return CIM_100;
                }
                throw new PowsyblException("Unsupported CIM version " + cimVersion);
        }
    }

    public static String getProfile(String profileUri) {
        for (Cim cim : CIM_LIST) {
            if (cim.hasProfileUri(profileUri)) {
                return cim.getProfile(profileUri);
            }
        }
        return null;
    }
}
