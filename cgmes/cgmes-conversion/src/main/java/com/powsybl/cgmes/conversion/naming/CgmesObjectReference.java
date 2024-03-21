package com.powsybl.cgmes.conversion.naming;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface CgmesObjectReference {

    class Index implements CgmesObjectReference {
        private final int value;

        public Index(int value) {
            this.value = value;
        }

        public String toString() {
            return "" + value;
        }
    }

    class Key implements CgmesObjectReference {
        private final String value;

        public Key(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    class Identifiable implements CgmesObjectReference {
        private final com.powsybl.iidm.network.Identifiable<?> value;
        private final boolean addType;

        private static final EnumMap<IdentifiableType, String> TYPE_SUFFIXES = new EnumMap<>(Map.ofEntries(
                Map.entry(IdentifiableType.NETWORK, "N"),
                Map.entry(IdentifiableType.SUBSTATION, "S"),
                Map.entry(IdentifiableType.VOLTAGE_LEVEL, "VL"),
                Map.entry(IdentifiableType.HVDC_LINE, "DCLS"),
                Map.entry(IdentifiableType.BUS, "TN"),
                Map.entry(IdentifiableType.SWITCH, "SW"),
                Map.entry(IdentifiableType.BUSBAR_SECTION, "BS"),
                Map.entry(IdentifiableType.LINE, "ACLS"),
                Map.entry(IdentifiableType.TIE_LINE, "ACLS"),
                Map.entry(IdentifiableType.TWO_WINDINGS_TRANSFORMER, "PT"),
                Map.entry(IdentifiableType.THREE_WINDINGS_TRANSFORMER, "PT"),
                Map.entry(IdentifiableType.GENERATOR, "SM"),
                Map.entry(IdentifiableType.BATTERY, "SM"),
                // There is no single suffix for LOAD identifiables,
                // They can be mapped to ConformLoad, NonConformLoad, EnergyConsumer, ...
                Map.entry(IdentifiableType.SHUNT_COMPENSATOR, "SC"),
                Map.entry(IdentifiableType.DANGLING_LINE, "ACLS"),
                Map.entry(IdentifiableType.STATIC_VAR_COMPENSATOR, "SVC"),
                Map.entry(IdentifiableType.HVDC_CONVERTER_STATION, "DCCS")
        ));

        public Identifiable(com.powsybl.iidm.network.Identifiable<?> value, boolean addType) {
            this.value = value;
            this.addType = addType;
        }

        public String typeSuffix() {
            IdentifiableType type = value.getType();
            if (TYPE_SUFFIXES.containsKey(type)) {
                return TYPE_SUFFIXES.get(type);
            } else if (type == IdentifiableType.LOAD) {
                String className = CgmesExportUtil.loadClassName((Load) value);
                return switch (className) {
                    case CgmesNames.ASYNCHRONOUS_MACHINE -> "AM";
                    case CgmesNames.ENERGY_SOURCE -> "ES";
                    case CgmesNames.ENERGY_CONSUMER -> "EC";
                    case CgmesNames.CONFORM_LOAD -> "CL";
                    case CgmesNames.NONCONFORM_LOAD -> "NCL";
                    case CgmesNames.STATION_SUPPLY -> "SS";
                    case CgmesNames.SV_INJECTION -> "SVI";
                    default -> throw new PowsyblException("Unexpected class name for Load: " + className);
                };
            } else {
                throw new PowsyblException("Unexpected IdentifiableType as CGMES object reference " + value.getType());
            }
        }

        public String toString() {
            String id = value.getId().replace("urn:uuid:", "");
            return addType ? id + "_" + typeSuffix() : id;
        }
    }

    class Subset implements CgmesObjectReference {
        private final CgmesSubset value;

        public Subset(CgmesSubset value) {
            this.value = value;
        }

        public String toString() {
            return value.toString();
        }
    }

    enum Part implements CgmesObjectReference {
        ACDC_CONVERTER_DC_TERMINAL("ACDCCDCT"),
        BASE_VOLTAGE("BV"),
        BOUNDARY_TERMINAL("BT"),
        CONNECTIVITY_NODE("CN"),
        CONTROL_AREA("CA"),
        CONVERTER_STATION("CS"),
        DCNODE("DCNODE"),
        DC_CONVERTER_UNIT("DCCU"),
        DC_TOPOLOGICAL_NODE("DCTN"),
        EQUIVALENT_INJECTION("EI"),
        FICTITIOUS("FICT"),
        FULL_MODEL("_FM"),
        WIND_GENERATING_UNIT("WGU"),
        NUCLEAR_GENERATING_UNIT("NGU"),
        SOLAR_GENERATING_UNIT("SGU"),
        THERMAL_GENERATING_UNIT("TGU"),
        HYDRO_GENERATING_UNIT("HGU"),
        HYDRO_POWER_PLANT("HPP"),
        FOSSIL_FUEL("FF"),
        GENERATING_UNIT("GU"),
        GEOGRAPHICAL_REGION("GR"),
        LOAD_AREA("LA"),
        LOAD_GROUP("LG"),
        LOAD_RESPONSE_CHARACTERISTICS("LRC"),
        OPERATIONAL_LIMIT_TYPE("OLT"),
        OPERATIONAL_LIMIT_SET("OLS"),
        OPERATIONAL_LIMIT_VALUE("OLV"),
        PATL("PATL"),
        PHASE_TAP_CHANGER("PTC"),
        PHASE_TAP_CHANGER_STEP("PTCS"),
        PHASE_TAP_CHANGER_TABLE("PTC_T"),
        RATIO_TAP_CHANGER("RTC"),
        RATIO_TAP_CHANGER_STEP("RTCS"),
        RATIO_TAP_CHANGER_TABLE("RTC_T"),
        REACTIVE_CAPABIILITY_CURVE_POINT("RCC_CP"),
        REACTIVE_CAPABILITY_CURVE("RCC"),
        REGULATING_CONTROL("RC"),
        SHUNT_COMPENSATOR("SC"),
        SUBSTATION("S"),
        SUB_GEOGRAPHICAL_REGION("SGR"),
        SUB_LOAD_AREA("SLA"),
        TATL("TATL"),
        TERMINAL("T"),
        TIE_FLOW("TF"),
        TOPOLOGICAL_ISLAND("TI"),
        TOPOLOGICAL_NODE("TN"),
        TRANSFORMER_END("TW"),
        VOLTAGE_LEVEL("VL");

        private final String suffix;

        Part(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public String toString() {
            return suffix;
        }
    }

    class Combo implements CgmesObjectReference {
        private final CgmesObjectReference a;
        private final CgmesObjectReference b;

        public Combo(CgmesObjectReference a, CgmesObjectReference b) {
            this.a = a;
            this.b = b;
        }

        public String toString() {
            // Join the two string representations without introducing a separator
            return a.toString() + b.toString();
        }
    }

    static CgmesObjectReference ref(int index) {
        return new Index(index);
    }

    static CgmesObjectReference ref(String key) {
        return new Key(key);
    }

    static CgmesObjectReference refTyped(com.powsybl.iidm.network.Identifiable<?> identifiable) {
        return new Identifiable(identifiable, true);
    }

    static CgmesObjectReference ref(com.powsybl.iidm.network.Identifiable<?> identifiable) {
        return new Identifiable(identifiable, false);
    }

    static CgmesObjectReference combo(CgmesObjectReference a, CgmesObjectReference b) {
        return new Combo(a, b);
    }

    static CgmesObjectReference ref(CgmesSubset value) {
        return new Subset(value);
    }

    static Part refGeneratingUnit(Generator generator) {
        return switch (generator.getEnergySource()) {
            case HYDRO -> Part.HYDRO_GENERATING_UNIT;
            case NUCLEAR -> Part.NUCLEAR_GENERATING_UNIT;
            case WIND -> Part.WIND_GENERATING_UNIT;
            case THERMAL -> Part.THERMAL_GENERATING_UNIT;
            case SOLAR -> Part.SOLAR_GENERATING_UNIT;
            case OTHER -> Part.GENERATING_UNIT;
        };
    }

    static String combine(CgmesObjectReference... refs) {
        return Arrays.stream(refs).map(CgmesObjectReference::toString).collect(Collectors.joining("_"));
    }

}
