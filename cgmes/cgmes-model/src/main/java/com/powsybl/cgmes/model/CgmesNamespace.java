/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

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

    private static final Map<Integer, String> CIM_NAMESPACES = Map.of(
            14, CIM_14_NAMESPACE,
            16, CIM_16_NAMESPACE,
            100, CIM_100_NAMESPACE);

    private static final Set<String> VALID_CIM_NAMESPACES = Set.of(CIM_14_NAMESPACE, CIM_16_NAMESPACE, CIM_100_NAMESPACE);
    private static final Pattern CIM_100_PLUS_NAMESPACE_PATTERN = Pattern.compile(".*/CIM[0-9]+#$");

    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String ENTSOE_NAMESPACE = "http://entsoe.eu/CIM/SchemaExtension/3/1#";
    public static final String EU_NAMESPACE = "http://iec.ch/TC57/CIM100-European#";
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";

    private static final String CIM_16_EQ_PROFILE = "http://entsoe.eu/CIM/EquipmentCore/3/1";
    private static final String CIM_16_EQ_OPERATION_PROFILE = "http://entsoe.eu/CIM/EquipmentOperation/3/1";
    private static final String CIM_16_TP_PROFILE = "http://entsoe.eu/CIM/Topology/4/1";
    private static final String CIM_16_SV_PROFILE = "http://entsoe.eu/CIM/StateVariables/4/1";
    private static final String CIM_16_SSH_PROFILE = "http://entsoe.eu/CIM/SteadyStateHypothesis/1/1";

    private static final String CIM_100_EQ_PROFILE = "http://iec.ch/TC57/ns/CIM/CoreEquipment-EU/3.0";
    private static final String CIM_100_EQ_OPERATION_PROFILE = "http://iec.ch/TC57/ns/CIM/Operation-EU/3.0";
    private static final String CIM_100_TP_PROFILE = "http://iec.ch/TC57/ns/CIM/Topology-EU/3.0";
    private static final String CIM_100_SV_PROFILE = "http://iec.ch/TC57/ns/CIM/StateVariables-EU/3.0";
    private static final String CIM_100_SSH_PROFILE = "http://iec.ch/TC57/ns/CIM/SteadyStateHypothesis-EU/3.0";

    private static final Map<Integer, Map<String, String>> PROFILES = Map.of(
            16, Map.of("EQ", CIM_16_EQ_PROFILE, "EQ_OP", CIM_16_EQ_OPERATION_PROFILE, "SSH", CIM_16_SSH_PROFILE, "SV", CIM_16_SV_PROFILE, "TP", CIM_16_TP_PROFILE),
            100, Map.of("EQ", CIM_100_EQ_PROFILE, "EQ_OP", CIM_100_EQ_OPERATION_PROFILE, "SSH", CIM_100_SSH_PROFILE, "SV", CIM_100_SV_PROFILE, "TP", CIM_100_TP_PROFILE)
    );

    public static boolean isValid(String ns) {
        // Until CIM16 the CIM namespace contained the string "CIM-schema-cim<versionNumber>#"
        // Since CIM100 the namespace seems to follow the pattern "/CIM<versionNumber>#"
        return VALID_CIM_NAMESPACES.contains(ns) || CIM_100_PLUS_NAMESPACE_PATTERN.matcher(ns).matches();
    }

    public static String getCim(int cimVersion) {
        if (CIM_NAMESPACES.containsKey(cimVersion)) {
            return CIM_NAMESPACES.get(cimVersion);
        }
        throw new AssertionError("Unsupported CIM version " + cimVersion);
    }

    public static String getEu(int cimVersion) {
        if (cimVersion == 16) {
            return ENTSOE_NAMESPACE;
        } else if (cimVersion >= 100) {
            return EU_NAMESPACE;
        }
        return "err-eu-namespace";
    }

    public static String getEuPrefix(int cimVersion) {
        if (cimVersion == 16) {
            return "entsoe";
        } else if (cimVersion >= 100) {
            return "eu";
        }
        return "err-eu-prefix";
    }

    public static String getLimitValueAttributeName(int cimVersion) {
        if (cimVersion == 16) {
            return "value";
        } else if (cimVersion >= 100) {
            return "normalValue";
        }
        return "err-limit-value-attr-name";
    }

    public static String getLimitTypeAttributeName(int cimVersion) {
        if (cimVersion == 16) {
            return "OperationalLimitType.limitType";
        } else if (cimVersion >= 100) {
            return  "OperationalLimitType.kind";
        }
        return "err-limit-type-attr-name";
    }

    public static String getLimitKindClassName(int cimVersion) {
        if (cimVersion == 16) {
            return "LimitTypeKind";
        } else if (cimVersion >= 100) {
            return  "LimitKind";
        }
        return "err-limit-kind-class-name";
    }

    public static boolean isWriteLimitInfiniteDuration(int cimVersion) {
        if (cimVersion == 16) {
            return false;
        } else {
            return cimVersion >= 100;
        }
    }

    public static boolean isWriteGeneratingUnitInitialP(int cimVersion) {
        if (cimVersion >= 100) {
            return false;
        } else {
            return cimVersion == 16;
        }
    }

    public static boolean hasProfiles(int cimVersion) {
        return PROFILES.containsKey(cimVersion);
    }

    public static String getProfile(int cimVersion, String profile) {
        if (PROFILES.containsKey(cimVersion)) {
            return PROFILES.get(cimVersion).get(profile);
        }
        throw new AssertionError("Unsupported CIM version " + cimVersion);
    }
}
