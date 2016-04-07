/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.psse_imp_exp;

import java.util.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class PsseRegister {

    String BusNum;
    String Model;
    String Id;

    PsseRegisterType type;

    LinkedHashMap<String,Float> parameters=new LinkedHashMap<>();

    LinkedHashSet<String> pins=new LinkedHashSet<>();

    public PsseRegister(String busNum, String model, String id, PsseRegisterType type, LinkedHashMap<String,Float> parameters, LinkedHashSet<String> pins) {
        BusNum = busNum;
        Model = model;
        Id = id;
        this.type=type;
        this.parameters=parameters;
        this.pins=pins;
    }

    public String getBusNum() {
        return BusNum;
    }

    public void setBusNum(String busNum) {
        BusNum = busNum;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    @Override
    public String toString() {
        String parVals="";
        for (String s : parameters.keySet()) {
            parVals=parVals+" ["+s+"="+parameters.get(s)+"]";
        }
        String pinNames="";
        for (String s : pins) {
            pinNames=pinNames+s+" ";
        }
        return "eu.itesla_project.iidm.ddb.psse_imp_exp.PsseRegister{" +
                "BusNum='" + BusNum + '\'' +
                ", Model='" + Model + '\'' +
                " (Type=" + type + ')' +
                ", Id='" + Id + '\'' +
                ", parameters=" + parVals +
                ", pins=" + pinNames +
                '}';
    }
}
