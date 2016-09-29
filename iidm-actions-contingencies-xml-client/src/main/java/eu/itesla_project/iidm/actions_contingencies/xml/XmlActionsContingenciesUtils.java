/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.actions_contingencies.xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.actions_contingencies.xml.mapping.ActionsContingencies;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Contingencies;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Contingency;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.ElementaryAction;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Zone;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Zones;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.modules.contingencies.ConstraintType;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class XmlActionsContingenciesUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlActionsContingenciesUtils.class);
	
	/**
	 * 
	 * @return Zone by number	 
	 */
	
	public static Zone getZone(ActionsContingencies actionContingencies , BigInteger number)
	{
		Zones zones= actionContingencies.getZones();
		
		for(Zone z :zones.getZone()) 
		{
				BigInteger zoneNumber=z.getNumber();
				if (zoneNumber.equals(number))
					return z;
		}
		
		return null;
	}

	
	/** 
	 * @return Zone by name
	 * 
	 */
	public static Zone getZone(ActionsContingencies actionContingencies, String name) 
	{
		
		Zones zones= actionContingencies.getZones();
		if ( zones != null ) {
			for(Zone z :zones.getZone()) 
			{
				String zoneName=z.getName();
				if (zoneName.equals(name))
					return z;
			}
		}
		return null;
	}
	
	public static Set<Zone> getZone( ActionsContingencies actionContingencies, Network network)
	{
		Set<Zone>  zones = new HashSet<Zone>();
		
		Zones xmlZones					= actionContingencies.getZones();
		Iterable<VoltageLevel> net_vlt	= network.getVoltageLevels();
		
		for (VoltageLevel  vl: net_vlt) 
		{
			String networkVoltagelevelId = vl.getId();
			for(Zone z :xmlZones.getZone()) 
			{
				
				if (z.getVoltageLevels() != null) 
					for ( eu.itesla_project.iidm.actions_contingencies.xml.mapping.VoltageLevel  zvl: z.getVoltageLevels().getVoltageLevel())
					{
						
						if (networkVoltagelevelId.equals(zvl.getID()) )
						{   
							zones.add(z);
							break;
						}
					}
				
			}
		}
		return zones;
	}
	
	
	/** 
	 * @return Contingencies by zone's number. 
	 * 
	 */
	public static List<Contingency> getContingenciesByZone ( ActionsContingencies actionContingencies, BigInteger number)
	{
		List<Contingency> contingencyList = new ArrayList<Contingency>();
		Contingencies contingencies =actionContingencies.getContingencies();
		
		for (Contingency c :contingencies.getContingency()){
			if (c.getZones().getNum().contains(number))
				contingencyList.add(c);
		}
		
		return (contingencyList.size() >0) ? contingencyList: null;
		
	}
	

	/** 
	 * @return Contingencies by zone's name. 
	 * 
	 */
	public static List<Contingency> getContingencies ( ActionsContingencies actionContingencies, String name)
	{
		List<Contingency> contingencyList = new ArrayList<Contingency>();		
		Contingencies contingencies =actionContingencies.getContingencies();
		List<Zone> zones=actionContingencies.getZones().getZone();
		
		for (Contingency c :contingencies.getContingency())
		{
			if (c.getZones() != null) 
			{
				
				for (BigInteger znum :c.getZones().getNum()) 
				{
					Zone zoneInfo=getZone( actionContingencies , znum);
					if (zoneInfo.getName().equals(name))	
						contingencyList.add(c);
				}
				
			} else System.out.println(" contignecy  "+ c.getName() + " c.getZones()  is null");
		}
		
		return (contingencyList.size()>0) ? contingencyList: null;
		
	}
	
	
	
	/** 
	 * @return ElementaryActions by zone's id. 
	 * 
	 */
	public static List<ElementaryAction> getElementaryActionsByZone (ActionsContingencies actionContingencies, BigInteger number)
	{
		List<ElementaryAction> eactionsList = new ArrayList<ElementaryAction>();
		
		if 	(actionContingencies.getElementaryActions()!= null)
		{	
			List<ElementaryAction> eactions = actionContingencies.getElementaryActions().getElementaryAction();
			for (ElementaryAction eaction :eactions)
			{
				if (eaction.getZones()!=null && eaction.getZones().getNum().contains(number))	
					eactionsList.add(eaction);				
				
			}
			
			return (eactionsList.size()>0) ? eactionsList: null;
		
		} else	return null;
		
	}
	
	
	/** 
	 * @return ElementaryActions by zone's number. 
	 * 
	 */
	public static List<ElementaryAction> getElementaryActionsByZone (ActionsContingencies actionContingencies, String name)
	{
		List<ElementaryAction> eactionsList = new ArrayList<ElementaryAction>();
		
		if 	(actionContingencies.getElementaryActions()!= null)
		{	
			List<ElementaryAction> eactions = actionContingencies.getElementaryActions().getElementaryAction();
			for (ElementaryAction eaction :eactions)
			{
				if (eaction.getZones()!=null) 
				{
					for (Zone z :eaction.getZones().getZone())
						if (z.getName().equals(name))	eactionsList.add(eaction);
				
				}
			}
			return (eactionsList.size()>0) ? eactionsList: null;
		
		} else	return null;
		
	}


	public static boolean containsAction(String id, List<eu.itesla_project.modules.contingencies.Action> actions)
	{
		
		for (eu.itesla_project.modules.contingencies.Action act : actions) 
			if ( act.getId().equals(id) )	return true;
		
		return false;
		
	}
	
	
	public  static boolean containsContingency(String id, List<eu.itesla_project.contingency.Contingency> contingency)
	{
		System.out.println(" Contingency: "+ id);
		for (eu.itesla_project.contingency.Contingency ctg : contingency)
			if ( ctg.equals(id) )	return true;			
		return false;		
	}


	public static ConstraintType getConstraintType(String type) {
		if(type.equals("overload"))
			return ConstraintType.BRANCH_OVERLOAD;
		else
			return ConstraintType.valueOf(type);
	}
	
	
	

}
