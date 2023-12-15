package com.powsybl.cgmes.conversion.naming;

import com.powsybl.cgmes.model.CgmesSubset;

import java.util.Arrays;
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

        public Identifiable(com.powsybl.iidm.network.Identifiable<?> value) {
            this.value = value;
        }

        public String toString() {
            return value.getId();
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
        AC_LINE_SEGMENT("ACLS"),
        BASE_VOLTAGE("BV"),
        BOUNDARY_TERMINAL("BT"),
        CONNECTIVITY_NODE("CN"),
        CONTROL_AREA("CA"),
        CONVERTER_STATION("CS"),
        DCNODE("DCNODE"),
        DC_CONVERTER_UNIT("DCCU"),
        EQUIVALENT_INJECTION("EI"),
        FICTITIOUS("FICT"),
        FULL_MODEL("_FM"),
        GENERATING_UNIT("GU"),
        GEOGRAPHICAL_REGION("GR"),
        LOAD_AREA("LA"),
        LOAD_GROUP("LG"),
        LOAD_RESPONSE_CHARACTERISTICS("LRC"),
        OPERATIONAL_LIMIT_TYPE("OLT"),
        PATL("PATL"),
        PHASE_TAP_CHANGER("PTC"),
        PHASE_TAP_CHANGER_STEP("PTCS"),
        PHASE_TAP_CHANGER_TABLE("PTCT"),
        RATIO_TAP_CHANGER("RTC"),
        RATIO_TAP_CHANGER_STEP("RTCS"),
        RATIO_TAP_CHANGER_TABLE("RTCT"),
        REACTIVE_CAPABIILITY_CURVE_POINT("RCC_CP"),
        REACTIVE_CAPABILITY_CURVE("SM_RCC"),
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
        TRANSFORMER_END("TE"),
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

    static CgmesObjectReference ref(int index) {
        return new Index(index);
    }

    static CgmesObjectReference ref(String key) {
        return new Key(key);
    }

    static CgmesObjectReference ref(com.powsybl.iidm.network.Identifiable<?> identifiable) {
        return new Identifiable(identifiable);
    }

    static CgmesObjectReference ref(CgmesSubset value) {
        return new Subset(value);
    }

    static String combine(CgmesObjectReference... refs) {
        return Arrays.stream(refs).map(CgmesObjectReference::toString).collect(Collectors.joining("_"));
    }

}
