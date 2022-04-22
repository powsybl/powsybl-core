/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

import com.powsybl.commons.PowsyblException;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class CgmesNamespace {

    private CgmesNamespace() {
    }

    // cim14 is the CIM version corresponding to ENTSO-E Profile 1
    // It is used in this project to explore how to support future CGMES versions
    // We have sample models in cim14 and we use a different set of queries to obtain data

    public static final String CIM_100_NAMESPACE = "http://iec.ch/TC57/CIM100#";
    public static final String CIM_16_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    public static final String CIM_14_NAMESPACE = "http://iec.ch/TC57/2009/CIM-schema-cim14#";

    private static final Set<String> VALID_CIM_NAMESPACES = Set.of(CIM_14_NAMESPACE, CIM_16_NAMESPACE, CIM_100_NAMESPACE);
    private static final Pattern CIM_100_PLUS_NAMESPACE_PATTERN = Pattern.compile(".*/CIM[0-9]+#$");

    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String ENTSOE_NAMESPACE = "http://entsoe.eu/CIM/SchemaExtension/3/1#";
    public static final String EU_NAMESPACE = "http://iec.ch/TC57/CIM100-European#";
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";

    public static final String CIM_16_EQ_PROFILE = "http://entsoe.eu/CIM/EquipmentCore/3/1";
    public static final String CIM_16_EQ_OPERATION_PROFILE = "http://entsoe.eu/CIM/EquipmentOperation/3/1";
    public static final String CIM_16_TP_PROFILE = "http://entsoe.eu/CIM/Topology/4/1";
    public static final String CIM_16_SV_PROFILE = "http://entsoe.eu/CIM/StateVariables/4/1";
    public static final String CIM_16_SSH_PROFILE = "http://entsoe.eu/CIM/SteadyStateHypothesis/1/1";

    public static final String CGMES_EQ_3_OR_GREATER_PREFIX = "http://iec.ch/TC57/ns/CIM/CoreEquipment-EU/";
    public static final String CIM_100_EQ_PROFILE = "http://iec.ch/TC57/ns/CIM/CoreEquipment-EU/3.0";
    public static final String CIM_100_EQ_OPERATION_PROFILE = "http://iec.ch/TC57/ns/CIM/Operation-EU/3.0";
    public static final String CIM_100_TP_PROFILE = "http://iec.ch/TC57/ns/CIM/Topology-EU/3.0";
    public static final String CIM_100_SV_PROFILE = "http://iec.ch/TC57/ns/CIM/StateVariables-EU/3.0";
    public static final String CIM_100_SSH_PROFILE = "http://iec.ch/TC57/ns/CIM/SteadyStateHypothesis-EU/3.0";

    public static final Cim CIM_14 = new Cim14();
    public static final Cim CIM_16 = new Cim16();
    public static final Cim CIM_100 = new Cim100();

    public static boolean isValid(String ns) {
        // Until CIM16 the CIM namespace contained the string "CIM-schema-cim<versionNumber>#"
        // Since CIM100 the namespace seems to follow the pattern "/CIM<versionNumber>#"
        return VALID_CIM_NAMESPACES.contains(ns) || CIM_100_PLUS_NAMESPACE_PATTERN.matcher(ns).matches();
    }

    public interface Cim {
        int getVersion();

        String getNamespace();

        boolean hasProfiles();

        String getProfile(String profile);

        String getEuPrefix();

        String getEuNamespace();

        String getLimitValueAttributeName();

        String getLimitTypeAttributeName();

        String getLimitKindClassName();

        boolean writeLimitInfiniteDuration();

        boolean writeGeneratingUnitInitialP();
    }

    private static final class Cim14 implements Cim {
        @Override
        public int getVersion() {
            return 14;
        }

        @Override
        public String getNamespace() {
            return CIM_14_NAMESPACE;
        }

        @Override
        public boolean hasProfiles() {
            return false;
        }

        @Override
        public String getProfile(String profile) {
            throw new AssertionError("Unsupported CIM version 14");
        }

        @Override
        public String getEuPrefix() {
            throw new PowsyblException("Undefined eu prefix for version 14");
        }

        @Override
        public String getEuNamespace() {
            throw new PowsyblException("Undefined eu prefix for version 14");
        }

        @Override
        public String getLimitValueAttributeName() {
            throw new PowsyblException("Undefined eu prefix for version 14");
        }

        @Override
        public String getLimitTypeAttributeName() {
            throw new PowsyblException("Undefined eu prefix for version 14");
        }

        @Override
        public String getLimitKindClassName() {
            throw new PowsyblException("Undefined eu prefix for version 14");
        }

        @Override
        public boolean writeLimitInfiniteDuration() {
            return false;
        }

        @Override
        public boolean writeGeneratingUnitInitialP() {
            return false;
        }

        private Cim14() {
        }
    }

    private static final class Cim16 implements Cim {

        private final Map<String, String> profiles = Map.of("EQ", CIM_16_EQ_PROFILE, "EQ_OP", CIM_16_EQ_OPERATION_PROFILE, "SSH", CIM_16_SSH_PROFILE, "SV", CIM_16_SV_PROFILE, "TP", CIM_16_TP_PROFILE);

        @Override
        public int getVersion() {
            return 16;
        }

        @Override
        public String getNamespace() {
            return CIM_16_NAMESPACE;
        }

        @Override
        public boolean hasProfiles() {
            return true;
        }

        @Override
        public String getProfile(String profile) {
            return profiles.get(profile);
        }

        @Override
        public String getEuPrefix() {
            return "entsoe";
        }

        @Override
        public String getEuNamespace() {
            return ENTSOE_NAMESPACE;
        }

        @Override
        public String getLimitValueAttributeName() {
            return "value";
        }

        @Override
        public String getLimitTypeAttributeName() {
            return "OperationalLimitType.limitType";
        }

        @Override
        public String getLimitKindClassName() {
            return "LimitTypeKind";
        }

        @Override
        public boolean writeLimitInfiniteDuration() {
            return false;
        }

        @Override
        public boolean writeGeneratingUnitInitialP() {
            return true;
        }

        private Cim16() {
        }
    }

    private static final class Cim100 implements Cim {

        private final Map<String, String> profiles = Map.of("EQ", CIM_100_EQ_PROFILE, "EQ_OP", CIM_100_EQ_OPERATION_PROFILE, "SSH", CIM_100_SSH_PROFILE, "SV", CIM_100_SV_PROFILE, "TP", CIM_100_TP_PROFILE);

        @Override
        public int getVersion() {
            return 100;
        }

        @Override
        public String getNamespace() {
            return CIM_100_NAMESPACE;
        }

        @Override
        public boolean hasProfiles() {
            return true;
        }

        @Override
        public String getProfile(String profile) {
            return profiles.get(profile);
        }

        @Override
        public String getEuPrefix() {
            return "eu";
        }

        @Override
        public String getEuNamespace() {
            return EU_NAMESPACE;
        }

        @Override
        public String getLimitValueAttributeName() {
            return "normalValue";
        }

        @Override
        public String getLimitTypeAttributeName() {
            return "OperationalLimitType.kind";
        }

        @Override
        public String getLimitKindClassName() {
            return "LimitKind";
        }

        @Override
        public boolean writeLimitInfiniteDuration() {
            return true;
        }

        @Override
        public boolean writeGeneratingUnitInitialP() {
            return false;
        }

        private Cim100() {
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
                    return CIM_100;
                }
                throw new PowsyblException("Unsupported CIM version " + cimVersion);
        }
    }
}
