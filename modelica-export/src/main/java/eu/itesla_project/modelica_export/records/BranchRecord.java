/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Equipments;
import eu.itesla_project.iidm.network.TwoTerminalsConnectable;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class of Branch record
 * @author Silvia Machado <machados@aia.es>
 */
public abstract class BranchRecord extends ModelicaRecord {

	BranchRecord(TwoTerminalsConnectable twoTerminalsConnectable) {
        this.twoTerminalsConnectable = twoTerminalsConnectable;
    }

    @Override
    public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
        Equipments.ConnectionInfo terminal1Info = Equipments.getConnectionInfoInBusBreakerView(twoTerminalsConnectable.getTerminal1());
        Equipments.ConnectionInfo terminal2Info = Equipments.getConnectionInfoInBusBreakerView(twoTerminalsConnectable.getTerminal2());

        bus1 = terminal1Info.getConnectionBus();
        bus2 = terminal2Info.getConnectionBus();
        
        //System.out.println("Trafo: " + this.twoTerminalsConnectable.getId() + ". Terminal 1: " + bus1.getId() + ". Terminal 2: " + bus2.getId());

        nodeName1 = parseName(bus1.getId());
        nodeName2 = parseName(bus2.getId());

        //Parallel element        
        //while(modContext.dictionary.isModelicaNameDefined(DEFAULT_BRANCH_PREFIX + nodeName1 + "_" + nodeName2 + "_" + parallelIndex) ||
        //		modContext.dictionary.isModelicaNameDefined(DEFAULT_BRANCH_PREFIX + nodeName2 + "_" + nodeName1 + "_" + parallelIndex)) {
        //	parallelIndex++;
		//}
        
        //String modelicaName = DEFAULT_BRANCH_PREFIX + nodeName1 + "_" + nodeName2 + "_" + parallelIndex;
		String branchName = parseName(twoTerminalsConnectable.getId()); //CIM ID
        String modelicaName = DEFAULT_BRANCH_PREFIX + branchName; //CIM ID
        modelicaName = WordUtils.uncapitalize(modelicaName.substring(0,1)) + modelicaName.substring(1);
        
//        modelicaName = parseName(modelicaName); //Añadido de cara al conversor de PSSE
        modContext.dictionary.add(twoTerminalsConnectable, modelicaName);
        super.setModelicaName(modelicaName);
        
		ModelTemplate model = null;
		String ddbid = StaticData.MTC_PREFIX_NAME + super.mtcMapper.get(DEFAULT_BRANCH_PREFIX.substring(0,1).toUpperCase() + DEFAULT_BRANCH_PREFIX);
		
		ModelTemplateContainer mtc = ddbManager.findModelTemplateContainer(ddbid);
		
		if(mtc == null) {
//			_log.warn("EUROSTAG Model Template Container does not exist. Searching Default MODELICA Model Template Container in DDB.");
			mtc = ddbManager.findModelTemplateContainer(StaticData.MTC_PREFIX_NAME + DEFAULT_BRANCH_TYPE);
		}		
		
		if(mtc != null) {
			for(ModelTemplate mt : mtc.getModelTemplates()) {
				if(mt.getTypeName().equalsIgnoreCase(DEFAULT_BRANCH_TYPE)) model = mt;
			}
			
			if(model != null)
			{
//				String data = new String(model.getData("mo"));
//				super.setModelData(data);
				super.setModelicaType(model.getTypeName());
				
//				List<DefaultParameters> defSetParams = model.getDefaultParameters();
//				List<Parameter> defParameters = defSetParams.get(0).getParameters();
//				for(Parameter param : defParameters) branchParameters.add(param);
			}
			else {
				super.setCorrect(false);
				_log.error("MODELICA Model Template does not exist in DDB.");
			}
		}
		else {
			super.setCorrect(false);
//			_log.error("MODELICA Model Template Container does not exist in DDB.");
		}
    }
    
	public String getDEFAULT_BRANCH_TYPE() {
		return DEFAULT_BRANCH_TYPE;
	}

	public void setDEFAULT_BRANCH_TYPE(String Default_Branch_Type) {
		DEFAULT_BRANCH_TYPE = Default_Branch_Type;
	}
	
    public String getDEFAULT_BRANCH_PREFIX() {
		return DEFAULT_BRANCH_PREFIX;
	}

	public void setDEFAULT_BRANCH_PREFIX(String Default_Branch_Prefix) {
		DEFAULT_BRANCH_PREFIX = WordUtils.uncapitalize(Default_Branch_Prefix.substring(0,1)) + Default_Branch_Prefix.substring(1);
	}
	
	@Override
	public String parseName(String name) {
	   	String parsedName = name.trim();

    	if(parsedName.contains("-")) {
        	if(!parsedName.startsWith("_")) parsedName = "_" + parsedName;
        	parsedName = parsedName.replaceAll("-", "_");
    	}
       	parsedName = parsedName.replaceAll("\\s", "_");
       	parsedName = parsedName.replaceAll(StaticData.DOT, "_");
       	
       	if(parsedName.substring(0, 1).matches("[0-9]")) parsedName = "l_" + parsedName;
       	while(parsedName.endsWith("_")) parsedName = parsedName.substring(0, parsedName.length()-1);
       	
        return parsedName;
	}
	
	abstract void setParameters(float SNREF);
	
//	abstract void addParameter(String name, Object value);
	
    private   TwoTerminalsConnectable	twoTerminalsConnectable;
    protected String					nodeName1;
    protected String					nodeName2;
    protected Bus						bus1						= null;
    protected Bus 						bus2						= null;
    protected Integer					parallelIndex				= 1;
    
    protected List<Parameter>			branchParameters			= new ArrayList<Parameter>();
    protected List<IIDMParameter>		iidmbranchParameters		= new ArrayList<IIDMParameter>();
    
    public String						DEFAULT_BRANCH_TYPE;
	public String						DEFAULT_BRANCH_PREFIX;
	
	private static final Logger			_log						= LoggerFactory.getLogger(BranchRecord.class);
}
