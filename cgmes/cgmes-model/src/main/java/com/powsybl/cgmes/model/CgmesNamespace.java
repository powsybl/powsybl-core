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

    private static final Logger LOG = LoggerFactory.getLogger(CgmesNamespace.class);

    private CgmesNamespace() {
    }

    // cim14 is the CIM version corresponding to ENTSO-E Profile 1
    // It is used in this project to explore how to support future CGMES versions
    // We have sample models in cim14 and we use a different set of queries to obtain data

    public static final String CIM_100_NAMESPACE = "http://iec.ch/TC57/CIM100#";
    public static final String CIM_16_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    public static final String CIM_14_NAMESPACE = "http://iec.ch/TC57/2009/CIM-schema-cim14#";

    private static final Set<String> VALID_CIM_NAMESPACES = Set.of(CIM_14_NAMESPACE, CIM_16_NAMESPACE, CIM_100_NAMESPACE);
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

    public static final Cim CIM_14 = new Cim14();
    public static final Cim CIM_16 = new Cim16();
    public static final Cim CIM_100 = new Cim100();

    public static final List<Cim> CIM_LIST = List.of(CIM_14, CIM_16, CIM_100);

    public static boolean isValid(String ns) {
        // Until CIM16 the CIM namespace contained the string "CIM-schema-cim<versionNumber>#"
        // Since CIM100 the namespace seems to follow the pattern "/CIM<versionNumber>#"
        return VALID_CIM_NAMESPACES.contains(ns) || CIM_100_PLUS_NAMESPACE_PATTERN.matcher(ns).matches();
    }

    public interface Cim {
        int getVersion();

        String getNamespace();

        boolean hasProfiles();

        boolean hasProfileUri(String profileUri);

        String getProfileUri(String profile);

        String getProfile(String profileUri);

        String getEuPrefix();

        String getEuNamespace();

        String getLimitValueAttributeName();

        String getLimitTypeAttributeName();

        String getLimitKindClassName();

        boolean writeLimitInfiniteDuration();

        boolean writeConnectivityNodes();

        boolean writeTculControlMode();
    }

    private abstract static class AbstractCim implements Cim {
        private final int version;
        private final String namespace;

        @Override
        public int getVersion() {
            return version;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        private AbstractCim(int version, String namespace) {
            this.version = version;
            this.namespace = namespace;
        }
    }

    private static final class Cim14 extends AbstractCim {

        @Override
        public boolean hasProfiles() {
            return false;
        }

        @Override
        public boolean hasProfileUri(String profileUri) {
            return false;
        }

        @Override
        public String getProfileUri(String profile) {
            throw new IllegalStateException("Unsupported CIM version 14");
        }

        @Override
        public String getProfile(String profileUri) {
            throw new IllegalStateException("Unsupported CIM version 14");
        }

        @Override
        public String getEuPrefix() {
            throw new PowsyblException("Undefined EU prefix for version 14");
        }

        @Override
        public String getEuNamespace() {
            throw new PowsyblException("Undefined EU namespace for version 14");
        }

        @Override
        public String getLimitValueAttributeName() {
            throw new PowsyblException("Undefined limit value attribute name for version 14");
        }

        @Override
        public String getLimitTypeAttributeName() {
            throw new PowsyblException("Undefined limit type attribute name for version 14");
        }

        @Override
        public String getLimitKindClassName() {
            throw new PowsyblException("Undefined limit kind class name for version 14");
        }

        @Override
        public boolean writeLimitInfiniteDuration() {
            return false;
        }

        @Override
        public boolean writeConnectivityNodes() {
            return false;
        }

        @Override
        public boolean writeTculControlMode() {
            return true;
        }

        private Cim14() {
            super(14, CIM_14_NAMESPACE);
        }
    }

    private abstract static class AbstractCim16AndAbove extends AbstractCim {

        private final String euPrefix;
        private final String euNamespace;
        private final String limitValueAttributeName;
        private final String limitTypeAttributeName;
        private final String limitKindClassName;
        private final BiMap<String, String> profiles = HashBiMap.create();

        @Override
        public String getEuPrefix() {
            return euPrefix;
        }

        @Override
        public String getEuNamespace() {
            return euNamespace;
        }

        @Override
        public String getLimitValueAttributeName() {
            return limitValueAttributeName;
        }

        @Override
        public String getLimitTypeAttributeName() {
            return limitTypeAttributeName;
        }

        @Override
        public String getLimitKindClassName() {
            return limitKindClassName;
        }

        @Override
        public boolean hasProfiles() {
            return true;
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

        private AbstractCim16AndAbove(int version, String namespace, String euPrefix, String euNamespace,
                                      String limitValueAttributeName, String limitTypeAttributeName,
                                      String limitKindClassName, Map<String, String> profiles) {
            super(version, namespace);
            this.euPrefix = euPrefix;
            this.euNamespace = euNamespace;
            this.limitValueAttributeName = limitValueAttributeName;
            this.limitTypeAttributeName = limitTypeAttributeName;
            this.limitKindClassName = limitKindClassName;
            this.profiles.putAll(profiles);
        }
    }

    private static final class Cim16 extends AbstractCim16AndAbove {

        @Override
        public boolean writeLimitInfiniteDuration() {
            return false;
        }

        @Override
        public boolean writeConnectivityNodes() {
            return false;
        }

        @Override
        public boolean writeTculControlMode() {
            return true;
        }

        private Cim16() {
            super(16, CIM_16_NAMESPACE, "entsoe", ENTSOE_NAMESPACE,
                    "value",
                    "OperationalLimitType.limitType", "LimitTypeKind",
                    Map.of("EQ", CIM_16_EQ_PROFILE, "EQ_OP",
                    CIM_16_EQ_OPERATION_PROFILE, "SSH", CIM_16_SSH_PROFILE, "SV",
                    CIM_16_SV_PROFILE, "TP", CIM_16_TP_PROFILE,
                    "EQ_BD", CIM_16_EQ_BD_PROFILE, "TP_BD", CIM_16_TP_BD_PROFILE));
        }
    }

    private static final class Cim100 extends AbstractCim16AndAbove {

        @Override
        public boolean writeLimitInfiniteDuration() {
            return true;
        }

        @Override
        public boolean writeConnectivityNodes() {
            return true;
        }

        @Override
        public boolean writeTculControlMode() {
            return false;
        }

        private Cim100() {
            super(100, CIM_100_NAMESPACE, "eu", EU_NAMESPACE,
                    "normalValue",
                    "OperationalLimitType.kind", "LimitKind",
                    Map.of("EQ", CIM_100_EQ_PROFILE, "EQ_OP", CIM_100_EQ_OPERATION_PROFILE,
                    "SSH", CIM_100_SSH_PROFILE, "SV", CIM_100_SV_PROFILE, "TP", CIM_100_TP_PROFILE,
                    "EQ_BD", CIM_100_EQ_BD_PROFILE));
        }
    }

    public static Cim getCim(int cimVersion) {
        switch (cimVersion) {
            case 14:
                return CIM_14;
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
