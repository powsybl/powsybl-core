/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test.cim14;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.FakeCgmesModel;
import com.powsybl.cgmes.model.test.TestGridModelResources;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cim14SmallCasesCatalog {

    public TestGridModelResources txMicroBEAdapted() {
        return new TestGridModelResources("tx-from-microBE-adapted", null,
                "cim14/tx-from-microBE-adapted/tx-from-microBE-adapted_EQ.xml",
                "cim14/tx-from-microBE-adapted/tx-from-microBE-adapted_SV.xml",
                "cim14/tx-from-microBE-adapted/tx-from-microBE-adapted_TP.xml");
    }

    public final TestGridModelResources small1() {
        return new TestGridModelResources("smallcase1", expectedSmall1(),
                "cim14/smallcase1/case1_EQ.xml",
                "cim14/smallcase1/case1_SV.xml",
                "cim14/smallcase1/case1_TP.xml");
    }

    public final TestGridModelResources m7buses() {
        return new TestGridModelResources("7buses", expectedM7Buses(),
                "cim14/m7buses/m7buses_EQ.xml",
                "cim14/m7buses/m7buses_SV.xml",
                "cim14/m7buses/m7buses_TP.xml");
    }

    public final TestGridModelResources ieee14() {
        return new TestGridModelResources("ieee14", expectedIeee14(),
                "cim14/ieee14/ieee14bus_EQ.xml",
                "cim14/ieee14/ieee14bus_SV.xml",
                "cim14/ieee14/ieee14bus_TP.xml");
    }

    public final TestGridModelResources nordic32() {
        return new TestGridModelResources("nordic32", expectedNordic32(),
                "cim14/nordic32/Nordic32_EQ.xml",
                "cim14/nordic32/Nordic32_SV.xml",
                "cim14/nordic32/Nordic32_TP.xml");
    }

    private CgmesModel expectedSmall1() {
        return new FakeCgmesModel()
                .modelId("Small1")
                .version("IEC61970CIM14v02")
                .substations("_GEN______SS", "_INF______SS")
                .voltageLevels("_GEN______VL", "_GRID_____VL", "_INF______VL")
                .terminals("_GEN_____-GRID____-1_TW_EX_TE",
                        "_GEN_____-GRID____-1_TW_OR_TE", "_GEN______SM_TE",
                        "_GRID____-INF_____-1_AC_TE_EX", "_GRID____-INF_____-1_AC_TE_OR",
                        "_INF______SM_TE")
                .terminalLimits("_GEN_____-GRID____-1_TW_EX_CL", "_GEN_____-GRID____-1_TW_OR_CL",
                        "_GRID____-INF_____-1_AC_CL")
                .topologicalNodes("_GEN______TN", "_GRID_____TN", "_INF______TN")
                .acLineSegments("_GRID____-INF_____-1_AC")
                .transformers("_GEN_____-GRID____-1_PT")
                .transformerEnds("_GEN_____-GRID____-1_TW_EX", "_GEN_____-GRID____-1_TW_OR")
                .synchronousMachines("_GEN______SM", "_INF______SM");
    }

    private CgmesModel expectedM7Buses() {
        return new FakeCgmesModel()
                .modelId("M7Buses")
                .version("IEC61970CIM14v02")
                .substations("_FP.AND11_SS", "_FS.BIS11_SS", "_FSSV.O11_SS", "_FTILL511_SS",
                        "_FVALDI11_SS", "_FVERGE11_SS")
                .voltageLevels("_FP.AND11_VL", "_FS.BIS11_VL", "_FSSV.O11_VL", "_FTDPRA11_VL",
                        "_FTILL511_VL", "_FVALDI11_VL", "_FVERGE11_VL")
                .terminals("_FP.ANC12_EC_TE", "_FP.AND11-FP.AND12-A_SW_TE_EX",
                        "_FP.AND11-FP.AND12-A_SW_TE_OR", "_FP.AND11-FTDPRA11-1_TW_EX_TE",
                        "_FP.AND11-FTDPRA11-1_TW_OR_TE", "_FP.AND11-FVERGE11-1_AC_TE_EX",
                        "_FP.AND11-FVERGE11-1_AC_TE_OR",
                        "_FP.AND11-FVERGE11-2_AC_TE_EX", "_FP.AND11-FVERGE11-2_AC_TE_OR",
                        "_FS.BIC11_EC_TE", "_FS.BIS11-FS.BIS12-A_SW_TE_EX",
                        "_FS.BIS11-FS.BIS12-A_SW_TE_OR", "_FS.BIS11-FVALDI11-1_AC_TE_EX",
                        "_FS.BIS11-FVALDI11-1_AC_TE_OR",
                        "_FS.BIS11-FVALDI11-2_AC_TE_EX", "_FS.BIS11-FVALDI11-2_AC_TE_OR",
                        "_FSSV.O11-FP.AND11-1_AC_TE_EX", "_FSSV.O11-FP.AND11-1_AC_TE_OR",
                        "_FSSV.O11-FP.AND11-2_AC_TE_EX", "_FSSV.O11-FP.AND11-2_AC_TE_OR",
                        "_FSSV.O11-FSSV.O12-A_SW_TE_EX",
                        "_FSSV.O11-FSSV.O12-A_SW_TE_OR", "_FSSV.O11-FTILL511-1_AC_TE_EX",
                        "_FSSV.O11-FTILL511-1_AC_TE_OR", "_FSSV.O11-FTILL511-2_AC_TE_EX",
                        "_FSSV.O11-FTILL511-2_AC_TE_OR", "_FSSV.T11_SM_TE",
                        "_FTDPRA11-FTDPRA12-A_SW_TE_EX",
                        "_FTDPRA11-FTDPRA12-A_SW_TE_OR", "_FTDPRA11-FVERGE11-1_AC_TE_EX",
                        "_FTDPRA11-FVERGE11-1_AC_TE_OR", "_FTDPRA11-FVERGE11-2_AC_TE_EX",
                        "_FTDPRA11-FVERGE11-2_AC_TE_OR", "_FTDPRC11_EC_TE",
                        "_FTILL511-FS.BIS11-1_AC_TE_EX",
                        "_FTILL511-FS.BIS11-1_AC_TE_OR", "_FTILL511-FS.BIS11-2_AC_TE_EX",
                        "_FTILL511-FS.BIS11-2_AC_TE_OR", "_FTILL511-FTILL512-A_SW_TE_EX",
                        "_FTILL511-FTILL512-A_SW_TE_OR", "_FTILLC51_EC_TE",
                        "_FVALDI11-FTDPRA11-1_AC_TE_EX",
                        "_FVALDI11-FTDPRA11-1_AC_TE_OR", "_FVALDI11-FTDPRA11-2_AC_TE_EX",
                        "_FVALDI11-FTDPRA11-2_AC_TE_OR", "_FVALDI11-FVALDI12-A_SW_TE_EX",
                        "_FVALDI11-FVALDI12-A_SW_TE_OR", "_FVALDT11_SM_TE",
                        "_FVERGE11-FVERGE12-A_SW_TE_EX",
                        "_FVERGE11-FVERGE12-A_SW_TE_OR", "_FVERGT11_SM_TE")
                .terminalLimits("_FP.AND11-FTDPRA11-1_TW_EX_CL", "_FP.AND11-FTDPRA11-1_TW_OR_CL",
                        "_FP.AND11-FVERGE11-1_AC_CL", "_FP.AND11-FVERGE11-2_AC_CL",
                        "_FS.BIS11-FVALDI11-1_AC_CL", "_FS.BIS11-FVALDI11-2_AC_CL",
                        "_FSSV.O11-FP.AND11-1_AC_CL",
                        "_FSSV.O11-FP.AND11-2_AC_CL",
                        "_FSSV.O11-FTILL511-1_AC_CL", "_FSSV.O11-FTILL511-2_AC_CL",
                        "_FTDPRA11-FVERGE11-1_AC_CL", "_FTDPRA11-FVERGE11-2_AC_CL",
                        "_FTILL511-FS.BIS11-1_AC_CL", "_FTILL511-FS.BIS11-2_AC_CL",
                        "_FVALDI11-FTDPRA11-1_AC_CL",
                        "_FVALDI11-FTDPRA11-2_AC_CL")
                .topologicalNodes("_FP.AND11_TN", "_FP.AND12_TN", "_FS.BIS11_TN",
                        "_FS.BIS12_TN", "_FSSV.O11_TN", "_FSSV.O12_TN", "_FTDPRA11_TN",
                        "_FTDPRA12_TN", "_FTILL511_TN", "_FTILL512_TN", "_FVALDI11_TN",
                        "_FVALDI12_TN", "_FVERGE11_TN", "_FVERGE12_TN")
                // Switch identifiers are repeated twice, one record per terminal
                .switches("_FP.AND11-FP.AND12-A_SW",
                        "_FS.BIS11-FS.BIS12-A_SW",
                        "_FSSV.O11-FSSV.O12-A_SW",
                        "_FTDPRA11-FTDPRA12-A_SW",
                        "_FTILL511-FTILL512-A_SW",
                        "_FVALDI11-FVALDI12-A_SW",
                        "_FVERGE11-FVERGE12-A_SW")
                .acLineSegments("_FP.AND11-FVERGE11-1_AC", "_FP.AND11-FVERGE11-2_AC",
                        "_FS.BIS11-FVALDI11-1_AC", "_FS.BIS11-FVALDI11-2_AC",
                        "_FSSV.O11-FP.AND11-1_AC", "_FSSV.O11-FP.AND11-2_AC",
                        "_FSSV.O11-FTILL511-1_AC", "_FSSV.O11-FTILL511-2_AC",
                        "_FTDPRA11-FVERGE11-1_AC",
                        "_FTDPRA11-FVERGE11-2_AC", "_FTILL511-FS.BIS11-1_AC",
                        "_FTILL511-FS.BIS11-2_AC", "_FVALDI11-FTDPRA11-1_AC",
                        "_FVALDI11-FTDPRA11-2_AC")
                .transformers("_FP.AND11-FTDPRA11-1_PT")
                .transformerEnds("_FP.AND11-FTDPRA11-1_TW_EX", "_FP.AND11-FTDPRA11-1_TW_OR")
                .phaseTapChangers("_FP.AND11-FTDPRA11-1_PTC_OR")
                .energyConsumers("_FP.ANC12_EC", "_FS.BIC11_EC", "_FTDPRC11_EC", "_FTILLC51_EC")
                .synchronousMachines("_FSSV.T11_SM", "_FVALDT11_SM", "_FVERGT11_SM");
    }

    private CgmesModel expectedIeee14() {
        return new FakeCgmesModel()
                .modelId("IEEE14")
                .version("IEC61970CIM14v02")
                .substations(
                        "_BUS___10_SS", "_BUS___11_SS", "_BUS___12_SS", "_BUS___13_SS",
                        "_BUS___14_SS", "_BUS____1_SS", "_BUS____2_SS", "_BUS____3_SS",
                        "_BUS____4_SS", "_BUS____5_SS", "_BUS____8_SS")
                .voltageLevels(
                        "_BUS___10_VL", "_BUS___11_VL", "_BUS___12_VL", "_BUS___13_VL",
                        "_BUS___14_VL", "_BUS____1_VL", "_BUS____2_VL", "_BUS____3_VL",
                        "_BUS____4_VL", "_BUS____5_VL", "_BUS____6_VL", "_BUS____7_VL",
                        "_BUS____8_VL", "_BUS____9_VL")
                .terminals(
                        "_BANK___9_SC_TE", "_BUS___10-BUS___11-1_AC_TE_EX",
                        "_BUS___10-BUS___11-1_AC_TE_OR", "_BUS___12-BUS___13-1_AC_TE_EX",
                        "_BUS___12-BUS___13-1_AC_TE_OR", "_BUS___13-BUS___14-1_AC_TE_EX",
                        "_BUS___13-BUS___14-1_AC_TE_OR", "_BUS____1-BUS____2-1_AC_TE_EX",
                        "_BUS____1-BUS____2-1_AC_TE_OR",
                        "_BUS____1-BUS____5-1_AC_TE_EX", "_BUS____1-BUS____5-1_AC_TE_OR",
                        "_BUS____2-BUS____3-1_AC_TE_EX", "_BUS____2-BUS____3-1_AC_TE_OR",
                        "_BUS____2-BUS____4-1_AC_TE_EX",
                        "_BUS____2-BUS____4-1_AC_TE_OR", "_BUS____2-BUS____5-1_AC_TE_EX",
                        "_BUS____2-BUS____5-1_AC_TE_OR", "_BUS____3-BUS____4-1_AC_TE_EX",
                        "_BUS____3-BUS____4-1_AC_TE_OR", "_BUS____4-BUS____5-1_AC_TE_EX",
                        "_BUS____4-BUS____5-1_AC_TE_OR",
                        "_BUS____4-BUS____7-1_TW_EX_TE", "_BUS____4-BUS____7-1_TW_OR_TE",
                        "_BUS____4-BUS____9-1_TW_EX_TE", "_BUS____4-BUS____9-1_TW_OR_TE",
                        "_BUS____5-BUS____6-1_TW_EX_TE", "_BUS____5-BUS____6-1_TW_OR_TE",
                        "_BUS____6-BUS___11-1_AC_TE_EX",
                        "_BUS____6-BUS___11-1_AC_TE_OR", "_BUS____6-BUS___12-1_AC_TE_EX",
                        "_BUS____6-BUS___12-1_AC_TE_OR", "_BUS____6-BUS___13-1_AC_TE_EX",
                        "_BUS____6-BUS___13-1_AC_TE_OR", "_BUS____7-BUS____8-1_AC_TE_EX",
                        "_BUS____7-BUS____8-1_AC_TE_OR",
                        "_BUS____7-BUS____9-1_AC_TE_EX", "_BUS____7-BUS____9-1_AC_TE_OR",
                        "_BUS____9-BUS___10-1_AC_TE_EX", "_BUS____9-BUS___10-1_AC_TE_OR",
                        "_BUS____9-BUS___14-1_AC_TE_EX", "_BUS____9-BUS___14-1_AC_TE_OR",
                        "_GEN____1_SM_TE",
                        "_GEN____2_SM_TE", "_GEN____3_SM_TE", "_GEN____6_SM_TE", "_GEN____8_SM_TE",
                        "_LOAD__10_EC_TE",
                        "_LOAD__11_EC_TE", "_LOAD__12_EC_TE", "_LOAD__13_EC_TE", "_LOAD__14_EC_TE",
                        "_LOAD___2_EC_TE", "_LOAD___3_EC_TE", "_LOAD___4_EC_TE", "_LOAD___5_EC_TE",
                        "_LOAD___6_EC_TE", "_LOAD___9_EC_TE")
                .terminalLimits(
                        "_BUS___10-BUS___11-1_AC_CL", "_BUS___12-BUS___13-1_AC_CL",
                        "_BUS___13-BUS___14-1_AC_CL", "_BUS____1-BUS____2-1_AC_CL",
                        "_BUS____1-BUS____5-1_AC_CL", "_BUS____2-BUS____3-1_AC_CL",
                        "_BUS____2-BUS____4-1_AC_CL", "_BUS____2-BUS____5-1_AC_CL",
                        "_BUS____3-BUS____4-1_AC_CL", "_BUS____4-BUS____5-1_AC_CL",
                        "_BUS____4-BUS____7-1_TW_EX_CL", "_BUS____4-BUS____7-1_TW_OR_CL",
                        "_BUS____4-BUS____9-1_TW_EX_CL", "_BUS____4-BUS____9-1_TW_OR_CL",
                        "_BUS____5-BUS____6-1_TW_EX_CL", "_BUS____5-BUS____6-1_TW_OR_CL",
                        "_BUS____6-BUS___11-1_AC_CL", "_BUS____6-BUS___12-1_AC_CL",
                        "_BUS____6-BUS___13-1_AC_CL", "_BUS____7-BUS____8-1_AC_CL",
                        "_BUS____7-BUS____9-1_AC_CL", "_BUS____9-BUS___10-1_AC_CL",
                        "_BUS____9-BUS___14-1_AC_CL")
                .topologicalNodes(
                        "_BUS___10_TN", "_BUS___11_TN", "_BUS___12_TN", "_BUS___13_TN",
                        "_BUS___14_TN", "_BUS____1_TN", "_BUS____2_TN", "_BUS____3_TN",
                        "_BUS____4_TN", "_BUS____5_TN", "_BUS____6_TN", "_BUS____7_TN",
                        "_BUS____8_TN",
                        "_BUS____9_TN")
                .acLineSegments("_BUS___10-BUS___11-1_AC", "_BUS___12-BUS___13-1_AC",
                        "_BUS___13-BUS___14-1_AC", "_BUS____1-BUS____2-1_AC",
                        "_BUS____1-BUS____5-1_AC", "_BUS____2-BUS____3-1_AC",
                        "_BUS____2-BUS____4-1_AC", "_BUS____2-BUS____5-1_AC",
                        "_BUS____3-BUS____4-1_AC",
                        "_BUS____4-BUS____5-1_AC", "_BUS____6-BUS___11-1_AC",
                        "_BUS____6-BUS___12-1_AC", "_BUS____6-BUS___13-1_AC",
                        "_BUS____7-BUS____8-1_AC", "_BUS____7-BUS____9-1_AC",
                        "_BUS____9-BUS___10-1_AC", "_BUS____9-BUS___14-1_AC")
                .transformers("_BUS____4-BUS____7-1_PT", "_BUS____4-BUS____9-1_PT",
                        "_BUS____5-BUS____6-1_PT")
                .transformerEnds("_BUS____4-BUS____7-1_TW_EX", "_BUS____4-BUS____7-1_TW_OR",
                        "_BUS____4-BUS____9-1_TW_EX", "_BUS____4-BUS____9-1_TW_OR",
                        "_BUS____5-BUS____6-1_TW_EX", "_BUS____5-BUS____6-1_TW_OR")
                .ratioTapChangers("_BUS____4-BUS____7-1_RTC_OR", "_BUS____4-BUS____9-1_RTC_OR",
                        "_BUS____5-BUS____6-1_RTC_OR")
                .energyConsumers("_LOAD__10_EC", "_LOAD__11_EC", "_LOAD__12_EC", "_LOAD__13_EC",
                        "_LOAD__14_EC", "_LOAD___2_EC", "_LOAD___3_EC", "_LOAD___4_EC",
                        "_LOAD___5_EC", "_LOAD___6_EC", "_LOAD___9_EC")
                .shuntCompensators("_BANK___9_SC")
                .synchronousMachines("_GEN____1_SM", "_GEN____2_SM", "_GEN____3_SM", "_GEN____6_SM",
                        "_GEN____8_SM");
    }

    private CgmesModel expectedNordic32() {
        return new FakeCgmesModel()
                .modelId("Nordic32")
                .version("IEC61970CIM14v02")
                .substations(
                        "_N1011____SS", "_N1012____SS", "_N1022____SS", "_N1041____SS",
                        "_N1044____SS", "_N1045____SS", "_N2031____SS", "_N4032____SS",
                        "_N4043____SS", "_N4046____SS", "_N4061____SS", "_NG11_____SS",
                        "_NG13_____SS", "_NG14_____SS", "_NG15_____SS", "_NG16_____SS",
                        "_NG17_____SS", "_NG18_____SS", "_NG19_____SS", "_NG20_____SS",
                        "_NG2______SS", "_NG3______SS", "_NG4______SS", "_NG6______SS",
                        "_NG7______SS", "_NG8______SS")
                .voltageLevels(
                        "_N1011____VL", "_N1012____VL", "_N1013____VL", "_N1014____VL",
                        "_N1021____VL", "_N1022____VL", "_N1041____VL", "_N1042____VL",
                        "_N1043____VL", "_N1044____VL", "_N1045____VL", "_N2031____VL",
                        "_N2032____VL", "_N4011____VL", "_N4012____VL", "_N4021____VL",
                        "_N4022____VL", "_N4031____VL", "_N4032____VL", "_N4041____VL",
                        "_N4042____VL", "_N4043____VL", "_N4044____VL", "_N4045____VL",
                        "_N4046____VL", "_N4047____VL", "_N4051____VL", "_N4061____VL",
                        "_N4062____VL", "_N4063____VL", "_N4071____VL", "_N4072____VL",
                        "_NG10_____VL", "_NG11_____VL", "_NG12_____VL", "_NG13_____VL",
                        "_NG14_____VL", "_NG15_____VL", "_NG16_____VL", "_NG17_____VL",
                        "_NG18_____VL", "_NG19_____VL", "_NG1______VL", "_NG20_____VL",
                        "_NG2______VL", "_NG3______VL", "_NG4______VL", "_NG5______VL",
                        "_NG6______VL", "_NG7______VL", "_NG8______VL", "_NG9______VL")
                .terminals(
                        "_G10______SM_TE", "_G11______SM_TE", "_G12______SM_TE", "_G13______SM_TE",
                        "_G14______SM_TE", "_G15______SM_TE", "_G16______SM_TE", "_G17______SM_TE",
                        "_G18______SM_TE", "_G19______SM_TE", "_G1_______SM_TE", "_G20______SM_TE",
                        "_G2_______SM_TE", "_G3_______SM_TE", "_G4_______SM_TE", "_G5_______SM_TE",
                        "_G6_______SM_TE", "_G7_______SM_TE", "_G8_______SM_TE", "_G9_______SM_TE",
                        "_N1011___-N1013___-1_AC_TE_EX", "_N1011___-N1013___-1_AC_TE_OR",
                        "_N1011___-N1013___-2_AC_TE_EX", "_N1011___-N1013___-2_AC_TE_OR",
                        "_N1011___-N4011___-1_TW_EX_TE", "_N1011___-N4011___-1_TW_OR_TE",
                        "_N1011____EC_TE", "_N1012___-N1014___-1_AC_TE_EX",
                        "_N1012___-N1014___-1_AC_TE_OR", "_N1012___-N1014___-2_AC_TE_EX",
                        "_N1012___-N1014___-2_AC_TE_OR", "_N1012___-N4012___-1_TW_EX_TE",
                        "_N1012___-N4012___-1_TW_OR_TE", "_N1012____EC_TE",
                        "_N1013___-N1014___-1_AC_TE_EX", "_N1013___-N1014___-1_AC_TE_OR",
                        "_N1013___-N1014___-2_AC_TE_EX", "_N1013___-N1014___-2_AC_TE_OR",
                        "_N1013____EC_TE", "_N1021___-N1022___-1_AC_TE_EX",
                        "_N1021___-N1022___-1_AC_TE_OR", "_N1021___-N1022___-2_AC_TE_EX",
                        "_N1021___-N1022___-2_AC_TE_OR", "_N1022___-N4022___-1_TW_EX_TE",
                        "_N1022___-N4022___-1_TW_OR_TE", "_N1022____EC_TE", "_N1022____SC_TE",
                        "_N1041___-N1043___-1_AC_TE_EX", "_N1041___-N1043___-1_AC_TE_OR",
                        "_N1041___-N1043___-2_AC_TE_EX", "_N1041___-N1043___-2_AC_TE_OR",
                        "_N1041___-N1045___-1_AC_TE_EX", "_N1041___-N1045___-1_AC_TE_OR",
                        "_N1041___-N1045___-2_AC_TE_EX", "_N1041___-N1045___-2_AC_TE_OR",
                        "_N1041____EC_TE", "_N1041____SC_TE", "_N1042___-N1044___-1_AC_TE_EX",
                        "_N1042___-N1044___-1_AC_TE_OR", "_N1042___-N1044___-2_AC_TE_EX",
                        "_N1042___-N1044___-2_AC_TE_OR", "_N1042___-N1045___-1_AC_TE_EX",
                        "_N1042___-N1045___-1_AC_TE_OR", "_N1042____EC_TE",
                        "_N1043___-N1044___-1_AC_TE_EX", "_N1043___-N1044___-1_AC_TE_OR",
                        "_N1043___-N1044___-2_AC_TE_EX", "_N1043___-N1044___-2_AC_TE_OR",
                        "_N1043____EC_TE", "_N1043____SC_TE", "_N1044___-N4044___-1_TW_EX_TE",
                        "_N1044___-N4044___-1_TW_OR_TE", "_N1044___-N4044___-2_TW_EX_TE",
                        "_N1044___-N4044___-2_TW_OR_TE", "_N1044____EC_TE", "_N1044____SC_TE",
                        "_N1045___-N4045___-1_TW_EX_TE", "_N1045___-N4045___-1_TW_OR_TE",
                        "_N1045___-N4045___-2_TW_EX_TE", "_N1045___-N4045___-2_TW_OR_TE",
                        "_N1045____EC_TE", "_N1045____SC_TE", "_N2031___-N2032___-1_AC_TE_EX",
                        "_N2031___-N2032___-1_AC_TE_OR", "_N2031___-N2032___-2_AC_TE_EX",
                        "_N2031___-N2032___-2_AC_TE_OR", "_N2031___-N4031___-1_TW_EX_TE",
                        "_N2031___-N4031___-1_TW_OR_TE", "_N2031____EC_TE", "_N2032____EC_TE",
                        "_N4011___-N4012___-1_AC_TE_EX", "_N4011___-N4012___-1_AC_TE_OR",
                        "_N4011___-N4021___-1_AC_TE_EX", "_N4011___-N4021___-1_AC_TE_OR",
                        "_N4011___-N4022___-1_AC_TE_EX", "_N4011___-N4022___-1_AC_TE_OR",
                        "_N4011___-N4071___-1_AC_TE_EX", "_N4011___-N4071___-1_AC_TE_OR",
                        "_N4012___-N4022___-1_AC_TE_EX", "_N4012___-N4022___-1_AC_TE_OR",
                        "_N4012___-N4071___-1_AC_TE_EX", "_N4012___-N4071___-1_AC_TE_OR",
                        "_N4012____SC_TE", "_N4021___-N4032___-1_AC_TE_EX",
                        "_N4021___-N4032___-1_AC_TE_OR", "_N4021___-N4042___-1_AC_TE_EX",
                        "_N4021___-N4042___-1_AC_TE_OR", "_N4022___-N4031___-1_AC_TE_EX",
                        "_N4022___-N4031___-1_AC_TE_OR", "_N4022___-N4031___-2_AC_TE_EX",
                        "_N4022___-N4031___-2_AC_TE_OR", "_N4031___-N4032___-1_AC_TE_EX",
                        "_N4031___-N4032___-1_AC_TE_OR", "_N4031___-N4041___-1_AC_TE_EX",
                        "_N4031___-N4041___-1_AC_TE_OR", "_N4031___-N4041___-2_AC_TE_EX",
                        "_N4031___-N4041___-2_AC_TE_OR", "_N4032___-N4042___-1_AC_TE_EX",
                        "_N4032___-N4042___-1_AC_TE_OR", "_N4032___-N4044___-1_AC_TE_EX",
                        "_N4032___-N4044___-1_AC_TE_OR", "_N4041___-N4044___-1_AC_TE_EX",
                        "_N4041___-N4044___-1_AC_TE_OR", "_N4041___-N4061___-1_AC_TE_EX",
                        "_N4041___-N4061___-1_AC_TE_OR", "_N4041____EC_TE", "_N4041____SC_TE",
                        "_N4042___-N4043___-1_AC_TE_EX", "_N4042___-N4043___-1_AC_TE_OR",
                        "_N4042___-N4044___-1_AC_TE_EX", "_N4042___-N4044___-1_AC_TE_OR",
                        "_N4042____EC_TE", "_N4043___-N4044___-1_AC_TE_EX",
                        "_N4043___-N4044___-1_AC_TE_OR", "_N4043___-N4046___-1_AC_TE_EX",
                        "_N4043___-N4046___-1_AC_TE_OR", "_N4043___-N4047___-1_AC_TE_EX",
                        "_N4043___-N4047___-1_AC_TE_OR", "_N4043____EC_TE", "_N4043____SC_TE",
                        "_N4044___-N4045___-1_AC_TE_EX", "_N4044___-N4045___-1_AC_TE_OR",
                        "_N4044___-N4045___-2_AC_TE_EX", "_N4044___-N4045___-2_AC_TE_OR",
                        "_N4045___-N4051___-1_AC_TE_EX", "_N4045___-N4051___-1_AC_TE_OR",
                        "_N4045___-N4051___-2_AC_TE_EX", "_N4045___-N4051___-2_AC_TE_OR",
                        "_N4045___-N4062___-1_AC_TE_EX", "_N4045___-N4062___-1_AC_TE_OR",
                        "_N4046___-N4047___-1_AC_TE_EX", "_N4046___-N4047___-1_AC_TE_OR",
                        "_N4046____EC_TE", "_N4046____SC_TE", "_N4047____EC_TE", "_N4051____EC_TE",
                        "_N4051____SC_TE", "_N4061___-N4062___-1_AC_TE_EX",
                        "_N4061___-N4062___-1_AC_TE_OR", "_N4061____EC_TE",
                        "_N4062___-N4063___-1_AC_TE_EX", "_N4062___-N4063___-1_AC_TE_OR",
                        "_N4062___-N4063___-2_AC_TE_EX", "_N4062___-N4063___-2_AC_TE_OR",
                        "_N4062____EC_TE", "_N4063____EC_TE", "_N4071___-N4072___-1_AC_TE_EX",
                        "_N4071___-N4072___-1_AC_TE_OR", "_N4071___-N4072___-2_AC_TE_EX",
                        "_N4071___-N4072___-2_AC_TE_OR", "_N4071____EC_TE", "_N4071____SC_TE",
                        "_N4072____EC_TE", "_NG10____-N4012___-1_TW_EX_TE",
                        "_NG10____-N4012___-1_TW_OR_TE", "_NG11____-N4021___-1_TW_EX_TE",
                        "_NG11____-N4021___-1_TW_OR_TE", "_NG12____-N4031___-1_TW_EX_TE",
                        "_NG12____-N4031___-1_TW_OR_TE", "_NG13____-N4041___-1_TW_EX_TE",
                        "_NG13____-N4041___-1_TW_OR_TE", "_NG14____-N4042___-1_TW_EX_TE",
                        "_NG14____-N4042___-1_TW_OR_TE", "_NG15____-N4047___-1_TW_EX_TE",
                        "_NG15____-N4047___-1_TW_OR_TE", "_NG16____-N4051___-1_TW_EX_TE",
                        "_NG16____-N4051___-1_TW_OR_TE", "_NG17____-N4062___-1_TW_EX_TE",
                        "_NG17____-N4062___-1_TW_OR_TE", "_NG18____-N4063___-1_TW_EX_TE",
                        "_NG18____-N4063___-1_TW_OR_TE", "_NG19____-N4071___-1_TW_EX_TE",
                        "_NG19____-N4071___-1_TW_OR_TE", "_NG1_____-N1012___-1_TW_EX_TE",
                        "_NG1_____-N1012___-1_TW_OR_TE", "_NG20____-N4072___-1_TW_EX_TE",
                        "_NG20____-N4072___-1_TW_OR_TE", "_NG2_____-N1013___-1_TW_EX_TE",
                        "_NG2_____-N1013___-1_TW_OR_TE", "_NG3_____-N1014___-1_TW_EX_TE",
                        "_NG3_____-N1014___-1_TW_OR_TE", "_NG4_____-N1021___-1_TW_EX_TE",
                        "_NG4_____-N1021___-1_TW_OR_TE", "_NG5_____-N1022___-1_TW_EX_TE",
                        "_NG5_____-N1022___-1_TW_OR_TE", "_NG6_____-N1042___-1_TW_EX_TE",
                        "_NG6_____-N1042___-1_TW_OR_TE", "_NG7_____-N1043___-1_TW_EX_TE",
                        "_NG7_____-N1043___-1_TW_OR_TE", "_NG8_____-N2032___-1_TW_EX_TE",
                        "_NG8_____-N2032___-1_TW_OR_TE", "_NG9_____-N4011___-1_TW_EX_TE",
                        "_NG9_____-N4011___-1_TW_OR_TE")
                .terminalLimits(
                        "_N1011___-N1013___-1_AC_CL", "_N1011___-N1013___-2_AC_CL",
                        "_N1011___-N4011___-1_TW_EX_CL", "_N1011___-N4011___-1_TW_OR_CL",
                        "_N1012___-N1014___-1_AC_CL", "_N1012___-N1014___-2_AC_CL",
                        "_N1012___-N4012___-1_TW_EX_CL", "_N1012___-N4012___-1_TW_OR_CL",
                        "_N1013___-N1014___-1_AC_CL", "_N1013___-N1014___-2_AC_CL",
                        "_N1021___-N1022___-1_AC_CL", "_N1021___-N1022___-2_AC_CL",
                        "_N1022___-N4022___-1_TW_EX_CL", "_N1022___-N4022___-1_TW_OR_CL",
                        "_N1041___-N1043___-1_AC_CL", "_N1041___-N1043___-2_AC_CL",
                        "_N1041___-N1045___-1_AC_CL", "_N1041___-N1045___-2_AC_CL",
                        "_N1042___-N1044___-1_AC_CL", "_N1042___-N1044___-2_AC_CL",
                        "_N1042___-N1045___-1_AC_CL", "_N1043___-N1044___-1_AC_CL",
                        "_N1043___-N1044___-2_AC_CL", "_N1044___-N4044___-1_TW_EX_CL",
                        "_N1044___-N4044___-1_TW_OR_CL", "_N1044___-N4044___-2_TW_EX_CL",
                        "_N1044___-N4044___-2_TW_OR_CL", "_N1045___-N4045___-1_TW_EX_CL",
                        "_N1045___-N4045___-1_TW_OR_CL", "_N1045___-N4045___-2_TW_EX_CL",
                        "_N1045___-N4045___-2_TW_OR_CL", "_N2031___-N2032___-1_AC_CL",
                        "_N2031___-N2032___-2_AC_CL", "_N2031___-N4031___-1_TW_EX_CL",
                        "_N2031___-N4031___-1_TW_OR_CL", "_N4011___-N4012___-1_AC_CL",
                        "_N4011___-N4021___-1_AC_CL", "_N4011___-N4022___-1_AC_CL",
                        "_N4011___-N4071___-1_AC_CL", "_N4012___-N4022___-1_AC_CL",
                        "_N4012___-N4071___-1_AC_CL", "_N4021___-N4032___-1_AC_CL",
                        "_N4021___-N4042___-1_AC_CL", "_N4022___-N4031___-1_AC_CL",
                        "_N4022___-N4031___-2_AC_CL", "_N4031___-N4032___-1_AC_CL",
                        "_N4031___-N4041___-1_AC_CL", "_N4031___-N4041___-2_AC_CL",
                        "_N4032___-N4042___-1_AC_CL", "_N4032___-N4044___-1_AC_CL",
                        "_N4041___-N4044___-1_AC_CL", "_N4041___-N4061___-1_AC_CL",
                        "_N4042___-N4043___-1_AC_CL", "_N4042___-N4044___-1_AC_CL",
                        "_N4043___-N4044___-1_AC_CL", "_N4043___-N4046___-1_AC_CL",
                        "_N4043___-N4047___-1_AC_CL", "_N4044___-N4045___-1_AC_CL",
                        "_N4044___-N4045___-2_AC_CL", "_N4045___-N4051___-1_AC_CL",
                        "_N4045___-N4051___-2_AC_CL", "_N4045___-N4062___-1_AC_CL",
                        "_N4046___-N4047___-1_AC_CL", "_N4061___-N4062___-1_AC_CL",
                        "_N4062___-N4063___-1_AC_CL", "_N4062___-N4063___-2_AC_CL",
                        "_N4071___-N4072___-1_AC_CL", "_N4071___-N4072___-2_AC_CL",
                        "_NG10____-N4012___-1_TW_EX_CL", "_NG10____-N4012___-1_TW_OR_CL",
                        "_NG11____-N4021___-1_TW_EX_CL", "_NG11____-N4021___-1_TW_OR_CL",
                        "_NG12____-N4031___-1_TW_EX_CL", "_NG12____-N4031___-1_TW_OR_CL",
                        "_NG13____-N4041___-1_TW_EX_CL", "_NG13____-N4041___-1_TW_OR_CL",
                        "_NG14____-N4042___-1_TW_EX_CL", "_NG14____-N4042___-1_TW_OR_CL",
                        "_NG15____-N4047___-1_TW_EX_CL", "_NG15____-N4047___-1_TW_OR_CL",
                        "_NG16____-N4051___-1_TW_EX_CL", "_NG16____-N4051___-1_TW_OR_CL",
                        "_NG17____-N4062___-1_TW_EX_CL", "_NG17____-N4062___-1_TW_OR_CL",
                        "_NG18____-N4063___-1_TW_EX_CL", "_NG18____-N4063___-1_TW_OR_CL",
                        "_NG19____-N4071___-1_TW_EX_CL", "_NG19____-N4071___-1_TW_OR_CL",
                        "_NG1_____-N1012___-1_TW_EX_CL", "_NG1_____-N1012___-1_TW_OR_CL",
                        "_NG20____-N4072___-1_TW_EX_CL", "_NG20____-N4072___-1_TW_OR_CL",
                        "_NG2_____-N1013___-1_TW_EX_CL", "_NG2_____-N1013___-1_TW_OR_CL",
                        "_NG3_____-N1014___-1_TW_EX_CL", "_NG3_____-N1014___-1_TW_OR_CL",
                        "_NG4_____-N1021___-1_TW_EX_CL", "_NG4_____-N1021___-1_TW_OR_CL",
                        "_NG5_____-N1022___-1_TW_EX_CL", "_NG5_____-N1022___-1_TW_OR_CL",
                        "_NG6_____-N1042___-1_TW_EX_CL", "_NG6_____-N1042___-1_TW_OR_CL",
                        "_NG7_____-N1043___-1_TW_EX_CL", "_NG7_____-N1043___-1_TW_OR_CL",
                        "_NG8_____-N2032___-1_TW_EX_CL", "_NG8_____-N2032___-1_TW_OR_CL",
                        "_NG9_____-N4011___-1_TW_EX_CL", "_NG9_____-N4011___-1_TW_OR_CL")
                .topologicalNodes(
                        "_N1011____TN", "_N1012____TN", "_N1013____TN", "_N1014____TN",
                        "_N1021____TN", "_N1022____TN", "_N1041____TN", "_N1042____TN",
                        "_N1043____TN", "_N1044____TN", "_N1045____TN", "_N2031____TN",
                        "_N2032____TN", "_N4011____TN", "_N4012____TN", "_N4021____TN",
                        "_N4022____TN", "_N4031____TN", "_N4032____TN", "_N4041____TN",
                        "_N4042____TN", "_N4043____TN", "_N4044____TN", "_N4045____TN",
                        "_N4046____TN", "_N4047____TN", "_N4051____TN", "_N4061____TN",
                        "_N4062____TN", "_N4063____TN", "_N4071____TN", "_N4072____TN",
                        "_NG10_____TN", "_NG11_____TN", "_NG12_____TN", "_NG13_____TN",
                        "_NG14_____TN", "_NG15_____TN", "_NG16_____TN", "_NG17_____TN",
                        "_NG18_____TN", "_NG19_____TN", "_NG1______TN", "_NG20_____TN",
                        "_NG2______TN", "_NG3______TN", "_NG4______TN", "_NG5______TN",
                        "_NG6______TN", "_NG7______TN", "_NG8______TN", "_NG9______TN")
                .acLineSegments(
                        "_N1011___-N1013___-1_AC", "_N1011___-N1013___-2_AC",
                        "_N1012___-N1014___-1_AC", "_N1012___-N1014___-2_AC",
                        "_N1013___-N1014___-1_AC", "_N1013___-N1014___-2_AC",
                        "_N1021___-N1022___-1_AC", "_N1021___-N1022___-2_AC",
                        "_N1041___-N1043___-1_AC", "_N1041___-N1043___-2_AC",
                        "_N1041___-N1045___-1_AC", "_N1041___-N1045___-2_AC",
                        "_N1042___-N1044___-1_AC", "_N1042___-N1044___-2_AC",
                        "_N1042___-N1045___-1_AC", "_N1043___-N1044___-1_AC",
                        "_N1043___-N1044___-2_AC", "_N2031___-N2032___-1_AC",
                        "_N2031___-N2032___-2_AC", "_N4011___-N4012___-1_AC",
                        "_N4011___-N4021___-1_AC", "_N4011___-N4022___-1_AC",
                        "_N4011___-N4071___-1_AC", "_N4012___-N4022___-1_AC",
                        "_N4012___-N4071___-1_AC", "_N4021___-N4032___-1_AC",
                        "_N4021___-N4042___-1_AC", "_N4022___-N4031___-1_AC",
                        "_N4022___-N4031___-2_AC", "_N4031___-N4032___-1_AC",
                        "_N4031___-N4041___-1_AC", "_N4031___-N4041___-2_AC",
                        "_N4032___-N4042___-1_AC", "_N4032___-N4044___-1_AC",
                        "_N4041___-N4044___-1_AC", "_N4041___-N4061___-1_AC",
                        "_N4042___-N4043___-1_AC", "_N4042___-N4044___-1_AC",
                        "_N4043___-N4044___-1_AC", "_N4043___-N4046___-1_AC",
                        "_N4043___-N4047___-1_AC", "_N4044___-N4045___-1_AC",
                        "_N4044___-N4045___-2_AC", "_N4045___-N4051___-1_AC",
                        "_N4045___-N4051___-2_AC", "_N4045___-N4062___-1_AC",
                        "_N4046___-N4047___-1_AC", "_N4061___-N4062___-1_AC",
                        "_N4062___-N4063___-1_AC", "_N4062___-N4063___-2_AC",
                        "_N4071___-N4072___-1_AC", "_N4071___-N4072___-2_AC")
                .transformers(
                        "_N1011___-N4011___-1_PT", "_N1012___-N4012___-1_PT",
                        "_N1022___-N4022___-1_PT", "_N1044___-N4044___-1_PT",
                        "_N1044___-N4044___-2_PT", "_N1045___-N4045___-1_PT",
                        "_N1045___-N4045___-2_PT", "_N2031___-N4031___-1_PT",
                        "_NG10____-N4012___-1_PT", "_NG11____-N4021___-1_PT",
                        "_NG12____-N4031___-1_PT", "_NG13____-N4041___-1_PT",
                        "_NG14____-N4042___-1_PT", "_NG15____-N4047___-1_PT",
                        "_NG16____-N4051___-1_PT", "_NG17____-N4062___-1_PT",
                        "_NG18____-N4063___-1_PT", "_NG19____-N4071___-1_PT",
                        "_NG1_____-N1012___-1_PT", "_NG20____-N4072___-1_PT",
                        "_NG2_____-N1013___-1_PT", "_NG3_____-N1014___-1_PT",
                        "_NG4_____-N1021___-1_PT", "_NG5_____-N1022___-1_PT",
                        "_NG6_____-N1042___-1_PT", "_NG7_____-N1043___-1_PT",
                        "_NG8_____-N2032___-1_PT", "_NG9_____-N4011___-1_PT")
                .transformerEnds(
                        "_N1011___-N4011___-1_TW_EX", "_N1011___-N4011___-1_TW_OR",
                        "_N1012___-N4012___-1_TW_EX", "_N1012___-N4012___-1_TW_OR",
                        "_N1022___-N4022___-1_TW_EX", "_N1022___-N4022___-1_TW_OR",
                        "_N1044___-N4044___-1_TW_EX", "_N1044___-N4044___-1_TW_OR",
                        "_N1044___-N4044___-2_TW_EX", "_N1044___-N4044___-2_TW_OR",
                        "_N1045___-N4045___-1_TW_EX", "_N1045___-N4045___-1_TW_OR",
                        "_N1045___-N4045___-2_TW_EX", "_N1045___-N4045___-2_TW_OR",
                        "_N2031___-N4031___-1_TW_EX", "_N2031___-N4031___-1_TW_OR",
                        "_NG10____-N4012___-1_TW_EX", "_NG10____-N4012___-1_TW_OR",
                        "_NG11____-N4021___-1_TW_EX", "_NG11____-N4021___-1_TW_OR",
                        "_NG12____-N4031___-1_TW_EX", "_NG12____-N4031___-1_TW_OR",
                        "_NG13____-N4041___-1_TW_EX", "_NG13____-N4041___-1_TW_OR",
                        "_NG14____-N4042___-1_TW_EX", "_NG14____-N4042___-1_TW_OR",
                        "_NG15____-N4047___-1_TW_EX", "_NG15____-N4047___-1_TW_OR",
                        "_NG16____-N4051___-1_TW_EX", "_NG16____-N4051___-1_TW_OR",
                        "_NG17____-N4062___-1_TW_EX", "_NG17____-N4062___-1_TW_OR",
                        "_NG18____-N4063___-1_TW_EX", "_NG18____-N4063___-1_TW_OR",
                        "_NG19____-N4071___-1_TW_EX", "_NG19____-N4071___-1_TW_OR",
                        "_NG1_____-N1012___-1_TW_EX", "_NG1_____-N1012___-1_TW_OR",
                        "_NG20____-N4072___-1_TW_EX", "_NG20____-N4072___-1_TW_OR",
                        "_NG2_____-N1013___-1_TW_EX", "_NG2_____-N1013___-1_TW_OR",
                        "_NG3_____-N1014___-1_TW_EX", "_NG3_____-N1014___-1_TW_OR",
                        "_NG4_____-N1021___-1_TW_EX", "_NG4_____-N1021___-1_TW_OR",
                        "_NG5_____-N1022___-1_TW_EX", "_NG5_____-N1022___-1_TW_OR",
                        "_NG6_____-N1042___-1_TW_EX", "_NG6_____-N1042___-1_TW_OR",
                        "_NG7_____-N1043___-1_TW_EX", "_NG7_____-N1043___-1_TW_OR",
                        "_NG8_____-N2032___-1_TW_EX", "_NG8_____-N2032___-1_TW_OR",
                        "_NG9_____-N4011___-1_TW_EX", "_NG9_____-N4011___-1_TW_OR")
                .energyConsumers(
                        "_N1011____EC", "_N1012____EC", "_N1013____EC", "_N1022____EC",
                        "_N1041____EC", "_N1042____EC", "_N1043____EC", "_N1044____EC",
                        "_N1045____EC", "_N2031____EC", "_N2032____EC", "_N4041____EC",
                        "_N4042____EC", "_N4043____EC", "_N4046____EC", "_N4047____EC",
                        "_N4051____EC", "_N4061____EC", "_N4062____EC", "_N4063____EC",
                        "_N4071____EC", "_N4072____EC")
                .shuntCompensators(
                        "_N1022____SC", "_N1041____SC", "_N1043____SC", "_N1044____SC",
                        "_N1045____SC", "_N4012____SC", "_N4041____SC", "_N4043____SC",
                        "_N4046____SC", "_N4051____SC", "_N4071____SC")
                .synchronousMachines(
                        "_G10______SM", "_G11______SM", "_G12______SM", "_G13______SM",
                        "_G14______SM", "_G15______SM", "_G16______SM", "_G17______SM",
                        "_G18______SM", "_G19______SM", "_G1_______SM", "_G20______SM",
                        "_G2_______SM", "_G3_______SM", "_G4_______SM", "_G5_______SM",
                        "_G6_______SM", "_G7_______SM", "_G8_______SM", "_G9_______SM");
    }
}
