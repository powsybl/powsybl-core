/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.psse_imp_exp;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class PsseRegisterFactory {

    static Logger log = LoggerFactory.getLogger(PsseRegisterFactory.class);

    static Map<String, PsseRegisterType> modelsTypesDict = new HashMap<>();
    static Map<String, String[]> modelsParsDict = new HashMap<String, String[]>();
    static Map<String, String[]> modelsPinsDict = new HashMap<String, String[]>();
    static LinkedHashSet<String> modelsPinsToBeIgnored= new LinkedHashSet<>();

    static {
        modelsTypesDict.put("GENROU",PsseRegisterType.GENERATOR);
        modelsTypesDict.put("GENSAL",PsseRegisterType.GENERATOR);
        modelsTypesDict.put("HYGOV",PsseRegisterType.TURBINE_GOVERNOR);
        modelsTypesDict.put("IEEET2", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("IEESGO",PsseRegisterType.TURBINE_GOVERNOR);
        modelsTypesDict.put("SCRX",PsseRegisterType.EXCITATION_SYSTEM);
        modelsTypesDict.put("SEXS",PsseRegisterType.EXCITATION_SYSTEM);
        modelsTypesDict.put("STAB2A",PsseRegisterType.STABILIZER);

        modelsTypesDict.put("TGOV1", PsseRegisterType.TURBINE_GOVERNOR );
        modelsTypesDict.put("ESST1A", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("PSS2A", PsseRegisterType.STABILIZER );
        modelsTypesDict.put("IEEEST", PsseRegisterType.STABILIZER );
        modelsTypesDict.put("ESDC1A", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("ESDC2A", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("ESAC1A", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("IEEEX1", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("GAST", PsseRegisterType.TURBINE_GOVERNOR );
        //PENDING
        modelsTypesDict.put("WT4G1",PsseRegisterType.GENERIC_WIND_GENERATOR );

        //TODO to be verified
        /*
        modelsTypesDict.put("ESST4B", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("ESAC2A", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("EXST1", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("IEEET1", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("ST5B", PsseRegisterType.EXCITATION_SYSTEM );
        modelsTypesDict.put("GGOV1",PsseRegisterType.TURBINE_GOVERNOR );
        modelsTypesDict.put("IEEEG1", PsseRegisterType.TURBINE_GOVERNOR );
        modelsTypesDict.put("PSS2B", PsseRegisterType.STABILIZER );
        modelsTypesDict.put("WT3G1", PsseRegisterType.GENERIC_WIND_GENERATOR );
        */

        //parameters names, list per component
        modelsParsDict.put("GENROU", new String[]{"Tpd0", "Tppd0", "Tpq0", "Tppq0", "H", "D", "Xd", "Xq", "Xpd", "Xpq", "Xppd,Xppq", "Xl", "S10", "S12"});
        modelsParsDict.put("GENSAL", new String[]{"Tpd0", "Tppd0", "Tppq0", "H", "D", "Xd", "Xq", "Xpd", "Xppd,Xppq", "Xl", "S10", "S12"});
        modelsParsDict.put("HYGOV", new String[]{"R", "r", "T_r", "T_f", "T_g", "VELM", "G_MAX", "G_MIN", "T_w", "A_t", "D_turb", "q_NL"});
        modelsParsDict.put("IEEET2", new String[]{"T_R", "K_A", "T_A", "V_RMAX", "V_RMIN", "K_E", "T_E", "K_F", "T_F1", "T_F2", "E_1", "S_EE_1", "E_2", "S_EE_2"});
        modelsParsDict.put("IEESGO", new String[]{"T_1", "T_2", "T_3", "T_4", "T_5", "T_6", "K_1", "K_2", "K_3", "P_MAX", "P_MIN"});
        modelsParsDict.put("SCRX", new String[]{"T_AT_B", "T_B", "K", "T_E", "E_MIN", "E_MAX", "C_SWITCH", "r_cr_fd"});
        modelsParsDict.put("SEXS", new String[]{"T_AT_B", "T_B", "K", "T_E", "E_MIN", "E_MAX"});
        modelsParsDict.put("STAB2A", new String[]{"K_2", "T_2", "K_3", "T_3", "K_4", "K_5","T_5","H_LIM"});

        modelsParsDict.put("TGOV1", new String[]{"R", "T_1", "V_MAX", "V_MIN", "T_2", "T_3", "D_t"});
        modelsParsDict.put("ESST1A", new String[]{"IM", "IM1", "T_R", "V_IMAX", "V_IMIN", "T_C", "T_B", "T_C1", "T_B1", "K_A", "T_A", "V_AMAX", "V_AMIN", "V_RMAX", "V_RMIN", "K_C", "K_F", "T_F", "K_LR", "I_LR"});
        modelsParsDict.put("PSS2A", new String[]{"ICS1", "REMBUS1", "ICS2", "REMBUS2", "M", "N", "T_w1", "T_w2", "T_6", "T_w3", "T_w4", "T_7", "K_S2", "K_S3", "T_8", "T_9", "K_S1", "T_1", "T_2", "T_3", "T_4", "V_STMAX", "V_STMIN"});
        modelsParsDict.put("IEEEST", new String[]{"IM", "IM1", "A_1", "A_2", "A_3", "A_4", "A_5", "A_6", "T_1", "T_2", "T_3", "T_4", "T_5", "T_6", "K_S", "L_SMAX", "L_SMIN", "V_CU", "V_CL"});
        modelsParsDict.put("ESDC2A", new String[]{"T_R", "K_A", "T_A", "T_B", "T_C", "V_RMAX", "V_RMIN", "K_E", "T_E", "K_F", "T_F1", "Switch", "E_1", "S_EE_1", "E_2", "S_EE_2"});
        modelsParsDict.put("ESDC1A", new String[]{"T_R", "K_A", "T_A", "T_B", "T_C", "V_RMAX", "V_RMIN", "K_E", "T_E", "K_F", "T_F1", "Switch", "E_1", "S_EE_1", "E_2", "S_EE_2"});
        modelsParsDict.put("ESAC1A", new String[]{"T_R", "T_B", "T_C", "K_A", "T_A", "V_AMAX", "V_AMIN", "T_E", "K_F", "T_F", "K_C", "K_D", "K_E", "E_1", "S_EE_1", "E_2", "S_EE_2", "V_RMAX", "V_RMIN"});
        modelsParsDict.put("IEEEX1", new String[]{"T_R", "K_A", "T_A", "T_B", "T_C", "V_RMAX", "V_RMIN", "K_E", "T_E", "K_F", "T_F1", "Switch", "E_1", "S_EE_1", "E_2", "S_EE_2"});
        modelsParsDict.put("GAST", new String[]{"R", "T_1", "T_2", "T_3", "AT", "K_T", "V_MAX", "V_MIN", "D_turb"});
        modelsParsDict.put("WT4G1", new String[]{"T_IQCmd", "T_IPCmd", "V_LVPL1", "V_LVPL2", "G_LVPL", "V_HVRCR", "CUR_HVRCR", "RIp_LVPL", "T_LVPL"});
        //TODO to be verified
        /*
        modelsParsDict.put("ESST4B", new String[]{"TR", "KPR", "KIR", "VRMAX", "VRMIN", "TA", "KPM", "KIM", "VMMAX", "VMMIN", "KG", "KP", "KI", "VBMAX", "KC", "XL", "THETAP"});
        modelsParsDict.put("ESAC2A", new String[]{"TR", "TB", "TC", "KA", "TA", "VAMAX", "VAMIN", "KB", "VRMAX", "VRMIN", "TE", "VFEMAX", "KH", "KF", "TF", "KC", "KD", "KE", "E1", "SE_E1", "E2", "SE_E2"});
        modelsParsDict.put("EXST1", new String[]{"TR", "VIMAX", "VIMIN", "TC", "TB", "KA", "TA", "VRMAX", "VRMIN", "KC", "KF", "TF"});
        modelsParsDict.put("IEEET1", new String[]{"TR", "KA", "TA", "VRMAX", "VRMIN", "KE", "TE", "KF", "TF", "Switch", "E1", "SE_E1", "E2", "SE_E2"});
        modelsParsDict.put("ST5B", new String[]{"TR", "TC1", "TB1", "TC2", "TB2", "KR", "VRMAX", "VRMIN", "T1", "KC", "TUC1", "TUB1", "TUC2", "TUB2", "TOC1", "TOB1", "TOC2", "TOB2"});
        modelsParsDict.put("GGOV1", new String[]{"R", "Tpelec", "maxerr", "minerr", "Kpgov", "Kigov", "Kdgov", "Tdgov", "vmax", "vmin", "Tact", "Kturb", "Wfnl", "Tb", "Tc", "Teng", "Tfload", "Kpload", "Kiload", "Ldref", "Dm", "Ropen", "Rclose", "Kimw", "Aset", "Ka", "Ta", "Trate", "db", "Tsa", "Tsb", "Rup", "Rdown"});
        modelsParsDict.put("IEEEG1", new String[]{"K", "T1", "T2", "T3", "Uo", "Uc", "PMAX", "PMIN", "T4", "K1", "K2", "T5", "K3", "K4", "T6", "K5", "K6", "T7", "K7", "K8"});
        //TODO this is not correct: in the .dyr example there are 19 parameters instead of the declared 17 found in models.pdf... why?
        modelsParsDict.put("PSS2B", new String[]{"Tw1", "Tw2", "T6", "Tw3", "Tw4", "T7", "KS2", "KS3", "T8", "T9", "KS1", "T1", "T2", "T3", "T4", "T10", "T11", "VS1MAX", "VS1MIN", "VS2MAX", "VS2MIN", "VSTMAX", "VSTMIN"});
        modelsParsDict.put("WT3G1", new String[]{"Xeq", "Kpll", "Kipll", "Pllmax", "Prated"});
        */


        //pins names, list per component
        modelsPinsDict.put("GENROU", new String[]{"p", "PMECH", "PMECH0", "EFD", "SPEED", "ANGLE", "ETERM", "PELEC", "ISORCE", "EFD0", "XADIFD"});
        modelsPinsDict.put("GENSAL", new String[]{"p", "PMECH", "PMECH0", "EFD", "SPEED", "ANGLE", "ETERM", "PELEC", "ISORCE", "EFD0", "XADIFD"});
        modelsPinsDict.put("HYGOV", new String[]{"SPEED", "PMECH", "PMECH0"});
        modelsPinsDict.put("IEEET2", new String[]{"ECOMP", "VOTHSG", "VOEL", "VUEL", "EFD0", "EFD"});
        modelsPinsDict.put("IEESGO", new String[]{"SPEED", "PMECH", "PMECH0"});
        modelsPinsDict.put("SCRX", new String[]{"ECOMP", "VOTHSG", "VOEL", "VUEL", "ETERM", "XADIFD", "EFD", "EFD0"});
        modelsPinsDict.put("SEXS", new String[]{"ECOMP", "VOTHSG", "VOEL", "VUEL", "EFD", "EFD0"});
        modelsPinsDict.put("STAB2A", new String[]{"PELEC", "VOTHSG"});


        modelsPinsDict.put("TGOV1", new String[]{"SPEED", "Reference", "PMECH", "PMECH0"});
        modelsPinsDict.put("ESST1A", new String[]{"XADIFD", "ECOMP", "EFD", "EFD0", "VOTHSG", "VOTHSG2", "VUEL1", "VUEL2", "VUEL3", "ILR", "VOEL", "VT"});
        modelsPinsDict.put("PSS2A", new String[]{"VSI1", "VSI2", "VOTHSG"});
        modelsPinsDict.put("IEEEST", new String[]{"V_S", "V_CT", "VOTHSG", "Vs"});
        modelsPinsDict.put("ESDC2A", new String[]{"ECOMP", "VOTHSG", "VOEL", "VUEL", "EFD", "EFD0"});
        modelsPinsDict.put("ESDC1A", new String[]{"ECOMP", "VOTHSG", "VOEL", "VUEL", "EFD", "EFD0"});
        modelsPinsDict.put("ESAC1A", new String[]{"XADIFD", "ECOMP", "VOTHSG", "VOEL", "VUEL", "EFD", "EFD0"});
        modelsPinsDict.put("IEEEX1", new String[]{"VOTHSG", "VOEL", "VUEL", "ECOMP", "EFD0", "EFD"});
        modelsPinsDict.put("GAST", new String[]{"SPEED", "VAR_L", "LoadLim", "PMECH"});
        modelsPinsDict.put("WT4G1", new String[]{"I_qcmd", "I_pcmd", "I_qcmd0", "I_pcmd0", "Vtt", "Iy", "V", "P", "Q", "IyL", "IxL"});
        //these pins must not be connected, so add them to the list of the ones that will be skipped in the matchings
        modelsPinsToBeIgnored.addAll(Arrays.asList("VOEL", "VUEL", "ANGLE", "ISORCE"));


        checkConsistency(modelsParsDict);
    }

    private static boolean checkConsistency(Map<String, String[]> modelParametersDictionary) {
        boolean isConsistent=true;
        for (String s : modelParametersDictionary.keySet()) {
            HashMap<String,String> tempMap=new HashMap<>();
            //check that actual params list size is matching with the declared
            for (int i = 0; i < modelParametersDictionary.get(s).length; i++) {
                if (tempMap.containsKey(modelParametersDictionary.get(s)[i])) {
                    isConsistent=false;
                    throw new RuntimeException("model " + s +" contains multiple occurrence of parameter " + modelParametersDictionary.get(s)[i]);
                } else {
                    tempMap.put(modelParametersDictionary.get(s)[i], modelParametersDictionary.get(s)[i]);
                }
            }
        }
        return true;
    }

    public static PsseRegister createRegister(String busNum, String model, String id, List<Float> parameters) {
        if (modelsParsDict.containsKey(model)) {
            int expectedParamsNum= modelsParsDict.get(model).length;
            int actualParamsNum=parameters.size();
            if (expectedParamsNum != actualParamsNum)
                throw new RuntimeException("wrong number of parameters for model "+model+": expected " + expectedParamsNum + ", found " + actualParamsNum);
            LinkedHashMap<String,Float> regPars=new LinkedHashMap<>();
            for (int i=0; i < modelsParsDict.get(model).length; i++) {
                regPars.put(modelsParsDict.get(model)[i],parameters.get(i));
            }
            LinkedHashSet<String> regPins=new LinkedHashSet<>();
            for (int i=0; i < modelsPinsDict.get(model).length; i++) {
                regPins.add(modelsPinsDict.get(model)[i]);
            }
            return new PsseRegister(busNum, model,id, modelsTypesDict.get(model), regPars, regPins );
        } else {
            log.warn("{} not in dictionary: skipping.",model);
        }
        return null;
    }

    public static LinkedHashSet<String> intersectPinsSets(PsseRegister psseReg1, PsseRegister psseReg2) {
        LinkedHashSet<String> retSet=new LinkedHashSet<>();
        return Sets.difference(Sets.intersection(psseReg1.pins,psseReg2.pins), modelsPinsToBeIgnored).copyInto(retSet);
    }

}
