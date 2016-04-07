/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaMainExporter;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Create a Modelica Detailed Transformer Record from IIDM Transformer
 * @author Silvia Machado <machados@aia.es>
 */
public class DetailedTransformerRecord extends BranchRecord {
	
    public DetailedTransformerRecord (TwoWindingsTransformer transformer, float SNREF) {
        super(transformer);
        this.transformer = transformer;
        super.setDEFAULT_BRANCH_TYPE(DEFAULT_DETAILED_TRAFO_TYPE);
        
        super.setDEFAULT_BRANCH_PREFIX(StaticData.PREF_TRAFO);
        
        this.setParameters(SNREF);
    }
    
    @Override
    public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
    	modContext.dictionary.add(this.transformer, this.transformer.getId());
		
		Equipments.ConnectionInfo info1 = Equipments.getConnectionInfoInBusBreakerView(this.transformer.getTerminal1());
		Bus b1 = info1.getConnectionBus();
		Equipments.ConnectionInfo info2 = Equipments.getConnectionInfoInBusBreakerView(this.transformer.getTerminal2());
		Bus b2 = info2.getConnectionBus();
		
		if((!Float.isNaN(b1.getV()) && info1.isConnected()) || (!Float.isNaN(b2.getV()) && info2.isConnected())) {    	
    	
	    	if(super.isCorrect()) {    		
				if (super.getModelicaType() != null) this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE);
				else this.addValue(DEFAULT_DETAILED_TRAFO_TYPE + StaticData.WHITE_SPACE);
				this.addValue(super.getModelicaName());
				this.addValue(" (");
				this.addValue(StaticData.NEW_LINE);
				
				if(!super.iidmbranchParameters.isEmpty()) {
					for(int i=0; i<super.iidmbranchParameters.size()-1; i++) {
						this.addValue("\t " + super.iidmbranchParameters.get(i).getName() + " = " + super.iidmbranchParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + super.iidmbranchParameters.get(super.iidmbranchParameters.size()-1).getName() + " = " + super.iidmbranchParameters.get(super.iidmbranchParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				else if(!super.branchParameters.isEmpty()) {
					for(int i=0; i<super.branchParameters.size()-1; i++) {
						this.addValue("\t " + super.branchParameters.get(i).getName() + " = " + super.branchParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + super.branchParameters.get(super.branchParameters.size()-1).getName() + " = " + super.branchParameters.get(super.branchParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				
				this.addValue("\t " + EurostagFixedData.ANNOT);
				
				//Clear data
				iidmbranchParameters = null;
				branchParameters = null;
	    	}
	    	else _log.error(this.getModelicaName() + " not added to grid model.");
		}
		else {
			_log.warn("Detailed transformer " + this.getModelicaName() + " disconnected.");
			this.addValue(StaticData.COMMENT + " Detailed transformer " + this.getModelicaName() + " disconnected.");
		}
    }
    
	/**
	 * Add IIDM parameters to Detailed Transformer Modelica Model
	 */
	@Override
	void setParameters(float SNREF) {
		super.iidmbranchParameters = new ArrayList<IIDMParameter>();

		float t1NomV = this.transformer.getTerminal1().getVoltageLevel().getNominalV();
        float t2NomV = this.transformer.getTerminal2().getVoltageLevel().getNominalV(); 
        float U1nom = Float.isNaN(t1NomV) == false ? t1NomV : 0;
		float U2nom = Float.isNaN(t2NomV) == false ? t2NomV : 0;
        float V1 = Float.isNaN(this.transformer.getRatedU1()) == false ? this.transformer.getRatedU1() : 0; // [kV]
        float V2  = Float.isNaN(this.transformer.getRatedU2()) == false ? this.transformer.getRatedU2() : 0; // [kV]
        float Zbase = (float) Math.pow(U2nom, 2) / SNREF;
        float G = this.transformer.getG() * Zbase; // [p.u.]
        float B = this.transformer.getB() * Zbase; // [p.u.]
        
        RatioTapChanger rtc = this.transformer.getRatioTapChanger();
        PhaseTapChanger ptc = this.transformer.getPhaseTapChanger();
        
        float dx = 0, dr = 0;
        
        if ( rtc != null ) {
            RatioTapChangerStep rtcs = rtc.getCurrentStep();
            V1 /= rtcs.getRho();
            dr += rtcs.getR();
            dx += rtcs.getX();
        }

        PhaseTapChangerStep ptcs = null;
        if ( ptc != null ) {
            ptcs = ptc.getCurrentStep();
            V1 /= ptcs.getRho();
            dr += ptcs.getR();
            dx += ptcs.getX();
        }
        float theta = ptc != null ? ptcs.getAlpha() : 0;

        double rpu2 = (this.transformer.getR()*(1 + dr/100)*SNREF) / Math.pow(U2nom, 2); // [p.u.]
        double xpu2 = (this.transformer.getX()*(1 + dx/100)*SNREF) / Math.pow(U2nom, 2); // [p.u.]
        
        /*
         * El ratio esta calculado de acuerdo al valor obtenido por HELM FLow
         */
        
		float Vend_pu = V1 / U1nom;
		float Vsource_pu = V2 / U2nom;
		float RATIO = Vsource_pu / Vend_pu; // ...transformation ratio [p.u.]
		
		if(ModelicaMainExporter.RATIOS_TO_1) RATIO = 1;

		super.addParameter(this.iidmbranchParameters, EurostagFixedData.r, RATIO); // p.u.
		super.addParameter(this.iidmbranchParameters, EurostagFixedData.B0, B);
		super.addParameter(this.iidmbranchParameters, EurostagFixedData.G0, G);
		super.addParameter(this.iidmbranchParameters, EurostagFixedData.THETA, theta);
		super.addParameter(this.iidmbranchParameters, StaticData.R, rpu2);
		super.addParameter(this.iidmbranchParameters, StaticData.X, xpu2);
	}
	
	@Override
	public DetailedTransformerRecord getClassName() {
		return this;
	}

	private TwoWindingsTransformer	transformer;

	private String				DEFAULT_DETAILED_TRAFO_TYPE		= EurostagModDefaultTypes.DEFAULT_DETAILED_TRAFO_TYPE;
	private String				DEFAULT_DETAILED_TRAFO_PREFIX;
	
	private static final Logger _log							= LoggerFactory.getLogger(DetailedTransformerRecord.class);
}
