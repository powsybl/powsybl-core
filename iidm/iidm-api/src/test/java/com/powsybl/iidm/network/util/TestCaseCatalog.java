package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.util.AbstractTestCase.BranchTestCase;
import com.powsybl.iidm.network.util.AbstractTestCase.DanglingLineTestCase;
import com.powsybl.iidm.network.util.AbstractTestCase.Flow;
import com.powsybl.iidm.network.util.AbstractTestCase.ThreeWindingsTransformerTestCase;
import com.powsybl.iidm.network.util.AbstractTestCase.Voltage;

public final class TestCaseCatalog {

    private TestCaseCatalog() {
    }

    static DanglingLineTestCase danglingLineEnd2Disconnected() {
        DanglingLineTestCase t = new DanglingLineTestCase();
        t.config.paramsCgmes = false;
        t.config.splitMagnetizingAdmittance = false;
        t.label = "Dangling Line";

        t.end1.ratedU = 380;
        t.end2.ratedU = 380;
        t.end1.r = 5;
        t.end1.x = 50.0;
        t.end1.g = 0;
        t.end1.b = 0.0001;
        t.end2.g = 0;
        t.end2.b = 0.0001;

        t.end1.connected = true;
        t.end2.connected = false;
        t.end1.voltage.u = 380;
        t.end1.voltage.theta = 0;
        t.end2.voltage = Voltage.UNKNOWN;
        t.end2.expectedFlow = Flow.UNKNOWN;
        return t;
    }

    // ENTSO-E CAS 2.0 Load Flow Explicit

    static BranchTestCase entsoeCAS2LoadFlowExplicitLine() {
        BranchTestCase t = new BranchTestCase();
        t.label = "FFNOD0L41__FNO";
        t.end1.ratedU = 380;
        t.end2.ratedU = 380;
        t.end1.r = 4.1956;
        t.end1.x = 12.73;
        // Voltage and angle for bus 1 have been taken from Excel documentation
        // with much more precision that the one found in SV data files
        t.end1.voltage = entsoeCAS2LoadFLowExplicitVoltage(1);
        t.end2.voltage = entsoeCAS2LoadFLowExplicitVoltage(2);
        t.end1.expectedFlow.p = -534.9869;
        t.end1.expectedFlow.q = 153.1046;
        t.end2.expectedFlow.p = 543.2755;
        t.end2.expectedFlow.q = -127.9559;
        return t;
    }

    static BranchTestCase entsoeCAS2LoadFlowExplicitPhaseShiftTransformer() {
        BranchTestCase t = new BranchTestCase();
        t.label = "FNOD041__FNOD021__1_PT";
        t.end1.ratedU = 380;
        t.end2.ratedU = 380;
        t.end1.r = 4.1956;
        t.end1.x = 12.73;
        t.end1.voltage = entsoeCAS2LoadFLowExplicitVoltage(1);
        t.end2.voltage = entsoeCAS2LoadFLowExplicitVoltage(2);
        t.end1.expectedFlow.p = 202.9869;
        t.end1.expectedFlow.q = -75.1046;
        t.end2.expectedFlow.p = -201.7355;
        t.end2.expectedFlow.q = 78.9091;
        t.end2.tap.rho = 0.997829;
        t.end2.tap.alpha = Math.toRadians(-3.77605);
        return t;
    };

    private static Voltage entsoeCAS2LoadFLowExplicitVoltage(int end) {
        Voltage v = new Voltage();
        // Voltage and angle for bus 1 have been taken from Excel documentation
        // with much more precision that the one found in SV data files
        if (end == 1) {
            v.u = 395.906724888442;
            v.theta = Math.toRadians(-2.717121983205);
        } else if (end == 2) {
            v.u = 397.1;
            v.theta = Math.toRadians(0);
        }
        return v;
    }

    static Flow entsoeCAS2LoadFlowExplicitLoad() {
        Flow f = new Flow();
        f.label = "Load";
        f.p = 332.0;
        f.q = -78.0;
        return f;
    }

    static Flow entsoeCAS2LoadFlowExplicitGenerator() {
        Flow f = new Flow();
        f.label = "Generator";
        f.p = -341.54;
        f.q = 49.0468;
        return f;
    }

    // ENTSO-E CAS 1.1.3 MicroGrid

    enum EntsoeMicroGridVariant {
        BC, BC_NO_TRANSFORMER_REGULATION, BC_AREA_CONTROL_ON, T1, T2, T3, T4
    }

    // ENTSO-E CAS 1.1.3 MicroGrid
    // Check flows in 2-winding transformer BE-TR2_3

    static BranchTestCase entsoeCAS1MicroGrid2wTx(EntsoeMicroGridVariant variant) {
        BranchTestCase t = new BranchTestCase();
        t.config.paramsCgmes = true;
        String id = "_e482b89a-fa84-4ea9-8e70-a83d44790957";
        String name = "BE-TR2_3";

        t.label = String.format("%s variant %s (%s)", name, variant, id);
        t.end1.ratedU = 110.34375;
        t.end2.ratedU = 10.5;
        t.end1.r = 0.104711;
        t.end1.x = 5.843419;
        // Power flow solutions for MicroGrid cases have been computed assuming the
        // magnetizing branch admittance is split between the two sides
        // of the transmission impedance
        // Parameters are given only at end1 but flag for splitting is activated
        double g = 0.0000173295;
        double b = -0.0000830339;
        t.end1.g = g;
        t.end1.b = b;
        t.end2.g = 0;
        t.end2.b = 0;
        t.config.splitMagnetizingAdmittance = true;

        // Ratio tap changer is located at side 2
        int neutralStep = 17;
        double stepVoltageIncrement = 0.8;
        int step;

        switch (variant) {
            case BC:
                // BE-Busbar_6
                t.end1.voltage.u = 115.500000;
                t.end1.voltage.theta = Math.toRadians(-9.391330);
                t.end1.expectedFlow.p = -89.685711;
                t.end1.expectedFlow.q = 57.132424;
                // BE-Busbar_4
                t.end2.voltage.u = 10.820805;
                t.end2.voltage.theta = Math.toRadians(-7.057180);
                t.end2.expectedFlow.p = 90.0;
                t.end2.expectedFlow.q = -51.115627;
                step = 18;
                t.end2.tap.forStep(step, neutralStep, stepVoltageIncrement);
            case BC_AREA_CONTROL_ON:
                t.end1.voltage.u = 115.5;
                t.end1.voltage.theta = Math.toRadians(-8.17249);
                t.end1.expectedFlow.p = -107.00052;
                t.end1.expectedFlow.q = 60.993156;
                t.end2.voltage.u = 10.80736;
                t.end2.voltage.theta = Math.toRadians(-5.38775);
                t.end2.expectedFlow.p = 107.344844;
                t.end2.expectedFlow.q = -53.286282;
                step = 18;
                t.end2.tap.forStep(step, neutralStep, stepVoltageIncrement);
                break;
            case BC_NO_TRANSFORMER_REGULATION:
                t.end1.voltage.u = 115.500000;
                t.end1.voltage.theta = Math.toRadians(-6.02643);
                t.end1.expectedFlow.p = -89.691355;
                t.end1.expectedFlow.q = 49.372793;
                t.end2.voltage.u = 10.513693;
                t.end2.voltage.theta = Math.toRadians(-3.70379);
                t.end2.expectedFlow.p = 90.0;
                t.end2.expectedFlow.q = -43.710113;
                step = 14;
                t.end2.tap.forStep(step, neutralStep, stepVoltageIncrement);
                break;
            case T1:
                t.end1.voltage.u = 115.5;
                t.end1.voltage.theta = Math.toRadians(-9.39998);
                t.end1.expectedFlow.p = -89.685701;
                t.end1.expectedFlow.q = 57.14414;
                t.end2.voltage.u = 10.820749;
                t.end2.voltage.theta = Math.toRadians(-7.06581);
                t.end2.expectedFlow.p = 90;
                t.end2.expectedFlow.q = -51.126769;
                step = 18;
                t.end2.tap.forStep(step, neutralStep, stepVoltageIncrement);
                break;
            case T2:
                t.end1.voltage.u = 115.5;
                t.end1.voltage.theta = Math.toRadians(-7.05928);
                t.end1.expectedFlow.p = -89.691679;
                t.end1.expectedFlow.q = 48.883609;
                t.end2.voltage.u = 10.774578;
                t.end2.voltage.theta = Math.toRadians(-4.73736);
                t.end2.expectedFlow.p = 90;
                t.end2.expectedFlow.q = -43.241488;
                step = 17;
                t.end2.tap.forStep(step, neutralStep, stepVoltageIncrement);
                break;
            case T3:
                t.end1.voltage.u = 115.5;
                t.end1.voltage.theta = Math.toRadians(-8.60659);
                t.end1.expectedFlow.p = -89.615174;
                t.end1.expectedFlow.q = 114.169896;
                t.end2.voltage.u = 10.795499;
                t.end2.voltage.theta = Math.toRadians(-6.18611);
                t.end2.expectedFlow.p = 90;
                t.end2.expectedFlow.q = -103.93313;
                step = 21;
                t.end2.tap.forStep(step, neutralStep, stepVoltageIncrement);
                break;
            case T4:
                t.end1.voltage.u = 115.5;
                t.end1.voltage.theta = Math.toRadians(-22.0298);
                t.end1.expectedFlow.p = -89.647583;
                t.end1.expectedFlow.q = 92.807736;
                t.end2.voltage.u = 10.816961;
                t.end2.voltage.theta = Math.toRadians(-19.6421);
                t.end2.expectedFlow.p = 90;
                t.end2.expectedFlow.q = -84.484905;
                step = 20;
                t.end2.tap.forStep(step, neutralStep, stepVoltageIncrement);
                break;
            default:
                t = null;
                break;
        }
        return t;
    }

    // ENTSO-E CAS 1.1.3 MicroGrid
    // Check flows in 3-windings transformer BE-TR3_1

    static BranchTestCase entsoeCAS1MicroGrid3wTxW380(EntsoeMicroGridVariant variant) {
        BranchTestCase t = new BranchTestCase();
        t.config.paramsCgmes = true;
        t.label = "380";
        t.end1.ratedU = 400;
        t.end2.ratedU = 1;
        t.end1.r = 0.898462;
        t.end1.x = 17.204128;
        t.end1.b = 0.0000024375;
        t.config.splitMagnetizingAdmittance = false;
        t.end2.expectedFlow.p = Double.NaN;
        t.end2.expectedFlow.q = Double.NaN;
        switch (variant) {
            case BC:
                t.end1.voltage.u = 412.989001;
                t.end1.voltage.theta = Math.toRadians(-6.78071);
                t.end1.expectedFlow.p = 99.218431;
                t.end1.expectedFlow.q = 3.304328;
                break;
            case BC_NO_TRANSFORMER_REGULATION:
                t.end1.voltage.u = 413.367538;
                t.end1.voltage.theta = Math.toRadians(-6.96199);
                t.end1.expectedFlow.p = -38.7065;
                t.end1.expectedFlow.q = 13.850241;
                break;
            case BC_AREA_CONTROL_ON:
                t.end1.voltage.u = 412.953536;
                t.end1.voltage.theta = Math.toRadians(-6.23401);
                t.end1.expectedFlow.p = 61.652507;
                t.end1.expectedFlow.q = 5.431494;
                break;
            case T1:
                t.end1.voltage.u = 412.989258;
                t.end1.voltage.theta = Math.toRadians(-6.78901);
                t.end1.expectedFlow.p = 99.586268;
                t.end1.expectedFlow.q = 3.250355;
                break;
            case T2:
                t.end1.voltage.u = 412.633073;
                t.end1.voltage.theta = Math.toRadians(-5.82972);
                t.end1.expectedFlow.p = -2.463349;
                t.end1.expectedFlow.q = 4.837149;
                break;
            case T3:
                t.end1.voltage.u = 413.589856;
                t.end1.voltage.theta = Math.toRadians(-6.64052);
                t.end1.expectedFlow.p = 67.610584;
                t.end1.expectedFlow.q = -11.251975;
                break;
            case T4:
                t.end1.voltage.u = 414.114413;
                t.end1.voltage.theta = Math.toRadians(-21.5265);
                t.end1.expectedFlow.p = -37.513383;
                t.end1.expectedFlow.q = 28.348302;
                break;
        }
        return t;
    }

    static BranchTestCase entsoeCAS1MicroGrid3wTxW225(EntsoeMicroGridVariant variant) {
        BranchTestCase t = new BranchTestCase();
        t.config.paramsCgmes = true;
        t.config.splitMagnetizingAdmittance = false;
        t.label = "225";
        t.end1.ratedU = 220;
        t.end2.ratedU = 1;
        t.end1.r = 0.323908;
        t.end1.x = 5.949086;
        t.end1.b = 0.0;
        // Tap changer is at step 17 that is the neutralStep
        // ratio tap changer has lowStep = 1, highStep = 33,
        // stepVoltageIncrement = 0.625, neutralU = 220, neutralStep = 17
        t.end1.tap.rho = 1.0;
        t.end2.expectedFlow.p = Double.NaN;
        t.end2.expectedFlow.q = Double.NaN;
        switch (variant) {
            case BC:
                t.end1.voltage.u = 224.315268;
                t.end1.voltage.theta = Math.toRadians(-8.77012);
                t.end1.expectedFlow.p = -216.19819;
                t.end1.expectedFlow.q = -85.36818;
                break;
            case BC_NO_TRANSFORMER_REGULATION:
                t.end1.voltage.u = 224.386792;
                t.end1.voltage.theta = Math.toRadians(-7.22458);
                t.end1.expectedFlow.p = -78.584994;
                t.end1.expectedFlow.q = -97.109252;
                break;
            case BC_AREA_CONTROL_ON:
                t.end1.voltage.u = 224.309142;
                t.end1.voltage.theta = Math.toRadians(-7.86995);
                t.end1.expectedFlow.p = -195.95349;
                t.end1.expectedFlow.q = -86.033369;
                break;
            case T1:
                t.end1.voltage.u = 224.315838;
                t.end1.voltage.theta = Math.toRadians(-8.77964);
                t.end1.expectedFlow.p = -216.06472;
                t.end1.expectedFlow.q = -85.396168;
                break;
            case T2:
                t.end1.voltage.u = 224.114164;
                t.end1.voltage.theta = Math.toRadians(-6.54843);
                t.end1.expectedFlow.p = -114.74994;
                t.end1.expectedFlow.q = -95.746507;
                break;
            case T3:
                t.end1.voltage.u = 226.03389;
                t.end1.voltage.theta = Math.toRadians(-8.23977);
                t.end1.expectedFlow.p = -184.84088;
                t.end1.expectedFlow.q = -49.665543;
                break;
            case T4:
                t.end1.voltage.u = 224.156562;
                t.end1.voltage.theta = Math.toRadians(-21.7962);
                t.end1.expectedFlow.p = -79.771949;
                t.end1.expectedFlow.q = -108.56893;
                break;
        }
        return t;
    }

    static BranchTestCase entsoeCAS1MicroGrid3wTxW21(EntsoeMicroGridVariant variant) {
        BranchTestCase t = new BranchTestCase();
        t.config.paramsCgmes = true;
        t.config.splitMagnetizingAdmittance = false;
        t.label = "21";
        t.end1.ratedU = 21;
        t.end2.ratedU = 1;
        t.end1.r = 0.013332;
        t.end1.x = 0.059978;
        t.end1.b = 0.0;
        t.end2.expectedFlow.p = Double.NaN;
        t.end2.expectedFlow.q = Double.NaN;
        switch (variant) {
            case BC:
                t.end1.voltage.u = 21.987;
                t.end1.voltage.theta = Math.toRadians(-6.6508);
                t.end1.expectedFlow.p = 118.0;
                t.end1.expectedFlow.q = 92.612077;
                break;
            case BC_NO_TRANSFORMER_REGULATION:
                t.end1.voltage.u = 21.987;
                t.end1.voltage.theta = Math.toRadians(-6.02499);
                t.end1.expectedFlow.p = 118.0;
                t.end1.expectedFlow.q = 88.343914;
                break;
            case BC_AREA_CONTROL_ON:
                t.end1.voltage.u = 21.987;
                t.end1.voltage.theta = Math.toRadians(-5.75689);
                t.end1.expectedFlow.p = 135.34484;
                t.end1.expectedFlow.q = 90.056974;
                break;
            case T1:
                t.end1.voltage.u = 21.987;
                t.end1.voltage.theta = Math.toRadians(-6.665);
                t.end1.expectedFlow.p = 117.495810;
                t.end1.expectedFlow.q = 92.681978;
                break;
            case T2:
                t.end1.voltage.u = 21.987;
                t.end1.voltage.theta = Math.toRadians(-5.11757);
                t.end1.expectedFlow.p = 118.0;
                t.end1.expectedFlow.q = 96.822572;
                break;
            case T3:
                t.end1.voltage.u = 21.987;
                t.end1.voltage.theta = Math.toRadians(-6.29357);
                t.end1.expectedFlow.p = 118.0;
                t.end1.expectedFlow.q = 68.339383;
                break;
            case T4:
                t.end1.voltage.u = 21.987;
                t.end1.voltage.theta = Math.toRadians(-20.5883);
                t.end1.expectedFlow.p = 118.0;
                t.end1.expectedFlow.q = 85.603401;
                break;
        }
        return t;
    }

    static AbstractTestCase entsoeCAS1MicroGrid3wTx(EntsoeMicroGridVariant variant) {
        BranchTestCase w380 = TestCaseCatalog.entsoeCAS1MicroGrid3wTxW380(variant);
        BranchTestCase w225 = TestCaseCatalog.entsoeCAS1MicroGrid3wTxW225(variant);
        BranchTestCase w21 = TestCaseCatalog.entsoeCAS1MicroGrid3wTxW21(variant);
        String name = "BE-TR3_1";
        String label = String.format("ENTSO-E CAS1 MicroGrid %s variant %s", name, variant);
        return new ThreeWindingsTransformerTestCase(label, w380, w225, w21);
    }
}
