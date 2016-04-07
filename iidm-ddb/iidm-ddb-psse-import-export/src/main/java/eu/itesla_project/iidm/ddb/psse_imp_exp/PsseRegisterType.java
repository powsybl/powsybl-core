/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.psse_imp_exp;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public enum PsseRegisterType {
    GENERATOR,
    COMPENSATOR,
    STABILIZER,
    MINIMUM_EXCITATION_LIMITER,
    MAXIMUM_EXCITATION_LIMITER,
    EXCITATION_SYSTEM,
    TURBINE_GOVERNOR,
    TURBINE_LOAD_CONTROLLER,
    LOAD_CHARACTERISTICS,
    LOAD_RELAY,
    LINE_RELAY,
    AUXILIARY_SIGNAL,
    TWO_TERMINAL_DC_LINE,
    MULTITERMINAL_DC_LINE,
    VSC_DC_LINE,
    FACTS_DEVICE,
    GENERIC_WIND_GENERATOR,
    GENERIC_WIND_ELECTRICAL,
    GENERIC_WIND_MECHANICAL,
    GENERIC_WIND_PITCH_CONTROL,
    GENERIC_WIND_AERODYNAMIC,
    SWITCHED_SHUNT,
    CONEC_AND_CONET;


    public static boolean isEquipment(PsseRegister reg) {
        boolean retVal = false;
        switch (reg.type) {
            case GENERATOR:
            case GENERIC_WIND_GENERATOR:
                retVal = true;
                break;
            default:
                retVal = false;
                break;
        }
        return retVal;
    }

    public static boolean isInternal(PsseRegister reg) {
        return (!isEquipment(reg));
    }

}
