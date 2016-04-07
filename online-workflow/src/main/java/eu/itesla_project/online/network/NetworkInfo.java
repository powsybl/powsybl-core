/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.network;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.VoltageLevel;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class NetworkInfo {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Network network;
	

	
	enum EquipmentTypes {
	    
		GEN, 
	    LOAD, 
	    LINE,
	    SHUNT, 
	    SUBSTATION, 
	    TWOWINDTRASF,  
	    THREEWINDTRASF, 
	    VOLTAGELEVEL, 
	}
	
	
	public NetworkInfo(Network net) {
	
		this.network = net;
	}
	
	
	public EquipmentTypes  equipmentType(String equipment) {
		
		if (network.getGenerator(equipment) != null) 
			return EquipmentTypes.GEN;
		
		if (network.getLoad(equipment) != null) 
			return EquipmentTypes.LOAD;
		
		if (network.getLine(equipment) != null)
			return EquipmentTypes.LINE;
		
		if (network.getTwoWindingsTransformer(equipment)!= null) 
			return EquipmentTypes.TWOWINDTRASF;
		
		if (network.getThreeWindingsTransformer(equipment)!= null) 
			return EquipmentTypes.THREEWINDTRASF;
		
		if ( network.getVoltageLevel(equipment) != null)
			return EquipmentTypes.VOLTAGELEVEL;
		
		if ( network.getShunt(equipment) != null)
			return EquipmentTypes.SHUNT;
		
		if (network.getSubstation(equipment) != null)
			return EquipmentTypes.SUBSTATION;
		
		logger.warn(" Type not found for equipment:" +equipment +" return NULL");
		return null;
	}
	
	
	
	public  Set<String> getEquipmentTsos(String equipment)  {
		
		
		EquipmentTypes 	eqType= equipmentType(equipment); 
		logger.debug(" eqType: "+eqType);
		Set<String> tsos = new HashSet<String>();
		
		if (eqType == null) 
			return null;
		
		switch (eqType) {
			case GEN:
				logger.debug(" equipment == generator");
				if (network.getGenerator(equipment).getTerminal().getVoltageLevel().getSubstation().getTso() != null)
					tsos.add(network.getGenerator(equipment).getTerminal().getVoltageLevel().getSubstation().getTso());
				break;
			case LOAD:	
				
				logger.debug("  equipment == load");
				if (network.getLoad(equipment).getTerminal().getVoltageLevel().getSubstation().getTso() != null)
					tsos.add(network.getLoad(equipment).getTerminal().getVoltageLevel().getSubstation().getTso());				
				break;
			case TWOWINDTRASF:
				
				logger.debug(" equipment  2 Windings transformer");
				if (network.getTwoWindingsTransformer(equipment).getSubstation().getTso() != null)
					tsos.add(network.getTwoWindingsTransformer(equipment).getSubstation().getTso());
				break;
				
			case THREEWINDTRASF:
				
				logger.debug("  equipment 3 Windings transformer");
				if (network.getThreeWindingsTransformer(equipment).getSubstation().getTso() != null)
					tsos.add(network.getThreeWindingsTransformer(equipment).getSubstation().getTso());
				break;
			case LINE:
				
				logger.debug("  equipment == line");
				VoltageLevel v1= network.getLine(equipment).getTerminal1().getVoltageLevel();
				if (v1.getSubstation().getTso() !=null)
					tsos.add(v1.getSubstation().getTso());
						
				VoltageLevel v2= network.getLine(equipment).getTerminal2().getVoltageLevel();
				if (v2.getSubstation().getTso() !=null)
					tsos.add(v2.getSubstation().getTso());
				break;
			case SUBSTATION:
				logger.debug("  equipment == SUBSTATION");
					if (network.getSubstation(equipment).getTso() != null)
						tsos.add(network.getSubstation(equipment).getTso());
					break;
			default:	
				logger.debug("  equipment = Voltage Level");
				if (network.getVoltageLevel(equipment).getSubstation().getTso() != null)
					tsos.add(network.getVoltageLevel(equipment).getSubstation().getTso());
				break;
			}		
		if (tsos!= null)	
			logger.debug(" RETURN tsos: " +tsos.toString() + " for Network: "+network.getName()  + " Equipment: "+equipment);
		return tsos;
	}
	
	
	public Double getP( String equipment) {
		
		
		logger.debug("  network "+network.getName()  + " equipment: "+equipment);
		Float returnValue = null;
		EquipmentTypes 	eqType= equipmentType(equipment); 
		if (eqType != null)
		switch (eqType) {
			case GEN:
				logger.debug("  equipment == generator");
				returnValue= network.getGenerator(equipment).getTerminal().getP();				
				break;
			case LOAD:	
				logger.debug("  equipment == load");
				returnValue= network.getLoad(equipment).getP0();
				break;
			case LINE:
				logger.debug(" equipment == line");
				if ( network.getLine(equipment).getTerminal1() != null) 
					returnValue =network.getLine(equipment).getTerminal1().getP();
				if ( network.getLine(equipment).getTerminal2() != null)
					returnValue =network.getLine(equipment).getTerminal2().getP();
				break;
		}
		
		if (returnValue!= null)	
			logger.debug(" RETURN P: " +returnValue  + " for Network: "+network.getName()  + " Equipment: "+equipment);
		return  (returnValue!=null)? new Double (returnValue): null;
		
	}
	
	
	public Double getQ( String equipment) {
		
		logger.debug("  network "+network.getName()  + " equipment: "+equipment);
		
		Float returnValue = null;	
		EquipmentTypes 	eqType= equipmentType(equipment); 
		if (eqType != null)
		switch (eqType) {
			case GEN:
				logger.debug(" equipment == generator");
				returnValue= network.getGenerator(equipment).getTerminal().getQ();	
				break;
			case LOAD:	
				logger.debug("  equipment == load");
				returnValue= network.getLoad(equipment).getQ0();
				break;
			case LINE:	
				logger.debug("  equipment == line");
				if ( network.getLine(equipment).getTerminal1() != null)
					returnValue =network.getLine(equipment).getTerminal1().getQ();
				if ( network.getLine(equipment).getTerminal2() != null)
					returnValue =network.getLine(equipment).getTerminal2().getQ();
				break;
		}
		if (returnValue!= null)	
			logger.debug(" RETURN Q: " +returnValue + " for Network: "+network.getName()  + " Equipment: "+equipment);
		
		return  (returnValue!=null)? new Double (returnValue): null;
	}
	
}

