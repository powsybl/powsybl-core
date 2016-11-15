/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.actions_contingencies.xml;

import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.contingency.ContingencyImpl;
import eu.itesla_project.contingency.GeneratorContingency;
import eu.itesla_project.contingency.LineContingency;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.*;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Action;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.ActionPlan;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Constraint;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Contingency;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.LogicalExpression;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.VoltageLevel;
import eu.itesla_project.iidm.actions_contingencies.xml.mapping.Zone;
import eu.itesla_project.modules.contingencies.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TieLine;
import eu.itesla_project.modules.contingencies.impl.ActionImpl;
import eu.itesla_project.modules.contingencies.impl.ActionPlanImpl;
import eu.itesla_project.modules.contingencies.impl.ActionsContingenciesAssociationImpl;
import eu.itesla_project.modules.contingencies.impl.ConstraintImpl;
import eu.itesla_project.modules.contingencies.impl.LogicalExpressionImpl;
import eu.itesla_project.modules.contingencies.impl.OptionImpl;
import eu.itesla_project.modules.contingencies.impl.VoltageLevelImpl;
import eu.itesla_project.modules.contingencies.impl.ZoneImpl;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class XmlFileContingenciesAndActionsDatabaseClient implements ContingenciesAndActionsDatabaseClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlFileContingenciesAndActionsDatabaseClient.class);

	private ActionsContingencies actionContingencies;
	private Map<Number, String> zonesMapping = new HashMap<Number, String>();

	public XmlFileContingenciesAndActionsDatabaseClient(Path file)
			throws JAXBException, SAXException {

		JAXBContext jaxbContext = JAXBContext
				.newInstance(ActionsContingencies.class);
		Unmarshaller jaxbMarshaller = jaxbContext.createUnmarshaller();
		
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
		URL res=XmlFileContingenciesAndActionsDatabaseClient.class.getClassLoader().getResource("xsd/actions.xsd");
		Schema schema = sf.newSchema(res); 
		jaxbMarshaller.setSchema(schema);
		
		actionContingencies = (ActionsContingencies) jaxbMarshaller
				.unmarshal(file.toFile());
		
		
	}




	@Override
	public List<Scenario> getScenarios() 
	{
		
		 return Collections.emptyList();
		/*
		List<Scenario> scenarios = new ArrayList<Scenario>();
		List<String> actionIds = new ArrayList<String>();
		for (Contingency c : actionContingencies.getContingencies()
				.getContingency()) {
			List<Action> actions = getActionsByContingencyId(c.getName());

			for (Action a : actions) {
				ActionPlan ap = this.getActionPlan(a.getId());
				if (ap != null) {
					List<Option> ol = ap.getOption();
					for (Option o : ol) {
						List<Action> oids = o.getAction();
						for (Action ai : oids)
							actionIds.add(ai.getId());
					}
				} else {
					ElementaryAction e = this.getElementaryAction(a.getId());
					if (e != null)
						actionIds.add(a.getId());

				}
			}

			Scenario sc = new ScenarioImpl(c.getName(), actionIds);
			scenarios.add(sc);
		}
		return scenarios;
		*/
	}



	/** 
	 * @param action's ID, Network
	 * @return eu.itesla_project.modules.contingencies.Action 
	 * */
	public eu.itesla_project.modules.contingencies.Action getAction(String id, Network network) {
		if ( zonesMapping.isEmpty() )
			getZones();
		if (id != null) {
			List<ElementaryAction> elactions = actionContingencies.getElementaryActions().getElementaryAction();
			for (ElementaryAction ele : elactions) 
			{
				if (ele.getName().equals(id)) 
				{
					List<ActionElement> elements = getActionElements(ele,network);
					
					List<String> zones = new ArrayList<String>(); 
					// it seems not to work
					//for ( Zone z : ele.getZones().getZone())
					//	zones.add(z.getName());
					// it replaces the code above
					Zones eleZones = ele.getZones();
					if ( eleZones != null ) {
						for ( BigInteger z: eleZones.getNum() )
							zones.add(zonesMapping.get(z));
					}
					if ( elements.size() > 0 )
						return new ActionImpl(id, ele.isPreventiveType(), ele.isCurativeType(), elements, zones, ele.getStartTime());
					else
						return null;
				}

			}
		}
		return null;
	}
	

	
	/** 
	 * @param  Network
	 * @return List<eu.itesla_project.modules.contingencies.Action>
	 * */
	public List<eu.itesla_project.modules.contingencies.Action> getActions(Network network) 
	{
		LOGGER.info("Getting actions for network {}", network.getId());
		if ( zonesMapping.isEmpty() )
			getZones();
		
		List<eu.itesla_project.modules.contingencies.Action> actions = new ArrayList<>();
		
		try {
			List<ElementaryAction> elactions = actionContingencies.getElementaryActions().getElementaryAction();
			
			for (ElementaryAction ele : elactions) {

				String name = ele.getName();
				List<ActionElement> elements = getActionElements(ele, network);
				
				List<String> zones = new ArrayList<String>(); 
				// it seems not to work
				//for ( Zone z : ele.getZones().getZone())
				//	zones.add(z.getName());
				// it replaces the code above
				Zones eleZones = ele.getZones();
				if ( eleZones != null ) {
					for ( BigInteger z: eleZones.getNum())
						zones.add(zonesMapping.get(z));
				}
				if ( elements.size() > 0 )
					actions.add(new ActionImpl(name, ele.isPreventiveType(), ele.isCurativeType(), elements, zones, ele.getStartTime()));

			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return actions;
	}
	
	

	
	
	
	
	/** 
	 * @param Network
	 * @return List<eu.itesla_project.contingency.Contingency>
	 * */
	@Override
	public List<eu.itesla_project.contingency.Contingency> getContingencies(Network network)
	{
		LOGGER.info("Getting contingencies for network {}", network.getId());
		if ( zonesMapping.isEmpty() )
			getZones();
		List<eu.itesla_project.contingency.Contingency> contingencies = new ArrayList<>();
		
		try {
			// pre-index tie lines
			Map<String, String> tieLines = new HashMap<>();
	        for (Line l : network.getLines()) {
	            if (l.isTieLine()) {
	                TieLine tl = (TieLine) l;
	                tieLines.put(tl.getHalf1().getId(), tl.getId());
	                tieLines.put(tl.getHalf2().getId(), tl.getId());
	            }
	        }
			for (Contingency cont : actionContingencies.getContingencies().getContingency()) {
				String contingency = cont.getName();
				LOGGER.info("contingency: {}", contingency);
				List<ContingencyElement> elements = new ArrayList<>();
				for (Equipment eq : cont.getEquipments().getEquipment()) {					
					String id = eq.getId();
					if (network.getLine(id) != null) {
						LOGGER.info("contingency: {} - element LineContingency, id: {}", contingency, id);
						elements.add(new LineContingency(id));
					} else if (network.getGenerator(id) != null) {
						LOGGER.info("contingency: {} - element GeneratorContingency, id: {}", contingency, id);
						elements.add(new GeneratorContingency(id));
					} else if (tieLines.containsKey(id)) {
						LOGGER.info("contingency: {} - element LineContingency, tieLines id: {}", contingency, tieLines.get(id));
                        elements.add(new LineContingency(tieLines.get(id)));
					} else {
						LOGGER.warn("Contingency element '{}' of contingency {} not found in network {}, skipping it", id, contingency, network.getId());
					}
				}					
				List<String> zones = new ArrayList<String>();
				// it seems not to work
				//for ( Zone z: cont.getZones().getZone())
				//	zones.add(z.getName());
				// it replaces the code above
				Zones contZones = cont.getZones();
				if ( contZones != null ) {
					for ( BigInteger z: contZones.getNum())
						zones.add(zonesMapping.get(z));
				}
				if ( elements.size() > 0 )
					contingencies.add(new ContingencyImpl(contingency, elements));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return contingencies;

	}

	/** 
	 * @param Contingency's name, Network
	 * @return List<eu.itesla_project.contingency.Contingency>
	 * */
	public eu.itesla_project.contingency.Contingency getContingency(String name, Network network) {
		if (name != null) {
			for (eu.itesla_project.contingency.Contingency c : getContingencies(network))
				if (c.getId().equals(name))
					return c;

		}
		return null;
	}
	
	
	@Override
	public Set<eu.itesla_project.modules.contingencies.Zone> getZones() 
	{
		Set<eu.itesla_project.modules.contingencies.Zone> res = new HashSet<eu.itesla_project.modules.contingencies.Zone>();
		Zones zones	=  actionContingencies.getZones();
		if ( zones != null ) {
		for ( Zone z: zones.getZone() )
			{
				List<eu.itesla_project.modules.contingencies.VoltageLevel> vls = new ArrayList<eu.itesla_project.modules.contingencies.VoltageLevel>();
				for(VoltageLevel vl : z.getVoltageLevels().getVoltageLevel())
					vls.add( new VoltageLevelImpl(vl.getID(), vl.getLevel()));
				eu.itesla_project.modules.contingencies.Zone zone=  new ZoneImpl(z.getName(), z.getNumber(), vls, z.getDescription());
				res.add(zone);
				zonesMapping.put(zone.getNumber(), zone.getName());
			}
		}
		return res;
	}

	@Override
	/**
	 * @param zone Name
	 */
	public eu.itesla_project.modules.contingencies.Zone getZone(String id)
	{
		Zone z = XmlActionsContingenciesUtils.getZone(actionContingencies, id);
		
		if (z == null) 
		{
			LOGGER.warn("Zones element '{}' with id " + id + " not found");
			return null;
		} 
		else
		{
			List<eu.itesla_project.modules.contingencies.VoltageLevel> vls = new ArrayList<eu.itesla_project.modules.contingencies.VoltageLevel>();
			for(VoltageLevel vl : z.getVoltageLevels().getVoltageLevel())
				vls.add( new VoltageLevelImpl(vl.getID(), vl.getLevel()));
			
			return new ZoneImpl(z.getName(), z.getNumber(), vls, z.getDescription());
		}
		
	}
	
	
	
	/**
	 * @param  Network
	 * @return Network's zones
	 */
	// it returns the zone containing at least a voltage level of the network, linking all the voltage levels to the zone
	//@Override
	public Set<eu.itesla_project.modules.contingencies.Zone> getZones_old( Network network)
	{
		Set<eu.itesla_project.modules.contingencies.Zone> netZones = new HashSet<eu.itesla_project.modules.contingencies.Zone>();
		
		Set<Zone> zones= XmlActionsContingenciesUtils.getZone( actionContingencies, network);
		
		for (Zone z: zones)		
		{
			List<eu.itesla_project.modules.contingencies.VoltageLevel> vls = new ArrayList<eu.itesla_project.modules.contingencies.VoltageLevel>();
			for(VoltageLevel vl : z.getVoltageLevels().getVoltageLevel())
				vls.add( new VoltageLevelImpl(vl.getID(), vl.getLevel()));

			netZones.add(new ZoneImpl(z.getName(), z.getNumber(), vls, z.getDescription()));		
		}
		return netZones;
	}
	
	// it returns the zone containing at least a voltage level of the network, linking to the zone only the voltage levels of the network 
	@Override
	public Set<eu.itesla_project.modules.contingencies.Zone> getZones(Network network) 
	{
		LOGGER.info("Getting zones for network {}", network.getId());
		Set<eu.itesla_project.modules.contingencies.Zone> res = new HashSet<eu.itesla_project.modules.contingencies.Zone>();
		Zones zones	= actionContingencies.getZones();
		if ( zones != null ) {
			for (Zone z: zones.getZone() )
			{
				List<eu.itesla_project.modules.contingencies.VoltageLevel> vls = new ArrayList<eu.itesla_project.modules.contingencies.VoltageLevel>();
				for(VoltageLevel vl : z.getVoltageLevels().getVoltageLevel()) {
					if ( network.getVoltageLevel(vl.getID()) != null )
						vls.add( new VoltageLevelImpl(vl.getID(), vl.getLevel()));
					else
						LOGGER.warn("Voltage level {} of zone {} does not belong to network {}, skipping it", vl.getID(), z.getName(), network.getId());
				}
				if ( vls.size() > 0 ) {
					eu.itesla_project.modules.contingencies.Zone zone=  new ZoneImpl(z.getName(), z.getNumber(), vls, z.getDescription());
					res.add(zone);
				}
			}
		}
		return res;
	}
	
	
	
	@Override
	public Set<eu.itesla_project.modules.contingencies.ActionPlan> getActionPlans( Network network)
	{
		LOGGER.info("Getting action plans for network {}", network.getId());
		Set<eu.itesla_project.modules.contingencies.ActionPlan> netActionPlans = new HashSet<eu.itesla_project.modules.contingencies.ActionPlan>();		
		List<eu.itesla_project.modules.contingencies.ActionPlan>  actionPlans = this.getActionPlans();
		
		if (actionPlans == null) 
		{
			LOGGER.warn("ActionPlans elements not found");
			return null;
		} 
		
		List<eu.itesla_project.modules.contingencies.Action>  netActions = this.getActions(network);
		List<String> actionsId = new ArrayList<String>();
		for (eu.itesla_project.modules.contingencies.Action netAct : netActions) 
			actionsId.add(netAct.getId());
		
		for (eu.itesla_project.modules.contingencies.ActionPlan ap : actionPlans)
		{ 
			
			for (Map.Entry<BigInteger, ActionPlanOption> entryOpt : ap.getPriorityOption().entrySet()) 
			{   
				
				for (Map.Entry<BigInteger, String>  entryAct: entryOpt.getValue().getActions().entrySet())
				{
					String actId=entryAct.getValue();
					if (actionsId.size()>0 && actionsId.contains(actId))
					{
						netActionPlans.add(ap);
						break;
					}
				}
				
			}
		}
		
		return netActionPlans;
	}
	
	/**
	 * return Action Plan by name
	 */
	public eu.itesla_project.modules.contingencies.ActionPlan  getActionPlan(String id) 
	{
		if ( zonesMapping.isEmpty() )
			getZones();
		
		if (id != null && actionContingencies.getActionPlans() !=  null ) 
		{
			List<ActionPlan> actPlans = actionContingencies.getActionPlans().getActionPlan();

			for (ActionPlan plan : actPlans) 
			{ 
				if (plan.getName().equals(id))	
				{
				
					Map<BigInteger, ActionPlanOption> priorityOptions = new TreeMap<BigInteger, ActionPlanOption>();
					
					for (eu.itesla_project.iidm.actions_contingencies.xml.mapping.Option op : plan.getOption())
					{
						//Map <number,actionId)
						Map<BigInteger, String> actionMap =  new TreeMap<BigInteger,String>();		
						for (eu.itesla_project.iidm.actions_contingencies.xml.mapping.Action ac: op.getAction()) 
							actionMap.put(ac.getNum(),  ac.getId());
						
							
						OptionImpl opImpl = new OptionImpl(op.getPriority(), convertExpression(op.getLogicalExpression(), actionMap),  actionMap) ;
						priorityOptions.put(op.getPriority(),opImpl);
					}
								
					List<String> zonesName= new ArrayList<String>();
					// it seems not to work
					//for (Zone z: plan.getZones().getZone())
					//	zonesName.add(z.getName());
					// it replaces the code above
					Zones planZones = plan.getZones();
					if ( planZones != null ) {
						for ( BigInteger z: planZones.getNum())
							zonesName.add(zonesMapping.get(z));
					}
					return new ActionPlanImpl(plan.getName(), plan.getDescription().getInfo(), zonesName , priorityOptions);
				}					
			}
		}
		return null;
	}	
	
	/*
	 * 
	 * @return all action plans defined into xml
	 */
	@Override
	public List<eu.itesla_project.modules.contingencies.ActionPlan> getActionPlans() 
	{
			if ( zonesMapping.isEmpty() )
				getZones();
		
			List<eu.itesla_project.modules.contingencies.ActionPlan> actPlanList = new ArrayList<eu.itesla_project.modules.contingencies.ActionPlan>();
			List<ActionPlan> actPlans = actionContingencies.getActionPlans().getActionPlan();
			if (actPlans == null) 
			{
				LOGGER.warn("ActionPlans elements not found");
				return null;
			} 
			else 
				for (ActionPlan plan : actPlans) 
				{
					Map<BigInteger, ActionPlanOption> priorityOptions = new TreeMap<BigInteger, ActionPlanOption>();
						
					for (eu.itesla_project.iidm.actions_contingencies.xml.mapping.Option op : plan.getOption())
					{
						//Map <number,actionId)
						Map<BigInteger, String> sequenceActions =  new TreeMap<BigInteger,String>();						
						for (eu.itesla_project.iidm.actions_contingencies.xml.mapping.Action ac: op.getAction()) 
							sequenceActions.put(ac.getNum(),  ac.getId());
						
						//System.out.println("Actionplan "+plan.getName());
						//TO DO GESTIRE LOGICAL EXPRESSION
						LogicalExpression exp =op.getLogicalExpression();
						
						eu.itesla_project.modules.contingencies.LogicalExpression le = convertExpression(exp, sequenceActions);
						
						OptionImpl opImpl = new OptionImpl(op.getPriority(), le,  sequenceActions) ;
						priorityOptions.put(op.getPriority(),opImpl);
					}
					
					List<String> zonesName= new ArrayList<String>();
					// it seems not to work
					//for (Zone z: plan.getZones().getZone())
					//	zonesName.add(z.getName());
					// it replaces the code above
					Zones planZones = plan.getZones();
					if ( planZones != null ) {
						for ( BigInteger z: planZones.getNum())
							zonesName.add(zonesMapping.get(z));
					}
					actPlanList.add(new ActionPlanImpl(plan.getName(), plan.getDescription().getInfo(),  zonesName, priorityOptions));
									
				}
				
			return actPlanList;
			
			
		
	}
	
	
	private eu.itesla_project.modules.contingencies.LogicalExpression convertExpression(
			LogicalExpression exp, Map<BigInteger, String> sequenceActions) {
		LogicalExpressionImpl le = new LogicalExpressionImpl();
		
		if(exp.getAnd() != null)
		{
			if(exp.getAnd().getOperand().size()==2)
				le.setOperator( new BinaryOperator(OperatorType.AND,toOperator(exp.getAnd().getOperand().get(0), sequenceActions),toOperator(exp.getAnd().getOperand().get(1), sequenceActions)) );
			else
				throw new RuntimeException("Operand mismatch");

		}
		else if(exp.getOr() != null)
		{
			if(exp.getOr().getOperand().size()==2)
				le.setOperator( new BinaryOperator(OperatorType.OR,toOperator(exp.getOr().getOperand().get(0), sequenceActions),toOperator(exp.getOr().getOperand().get(1), sequenceActions)) );
			else
				throw new RuntimeException("Operand mismatch");	
		}
		else if(exp.getThen() != null)
		{
			if(exp.getThen().getOperand().size()==2)
				le.setOperator( new BinaryOperator(OperatorType.THEN,toOperator(exp.getThen().getOperand().get(0), sequenceActions),toOperator(exp.getThen().getOperand().get(1), sequenceActions)) );
			else
				throw new RuntimeException("Operand mismatch");
		}
		else if(exp.getOperand() != null)
		{
			//System.out.println("Check operand : "+exp.getOperand());
			le.setOperator(toOperator(exp.getOperand(), sequenceActions));	
		}

		return le;
	}

	
	private List<Object> getFilteredContent(Operand op)		
    {
    		
    	if(op.getContent().size() >1 )	
        {	
    		ArrayList<Object> filtered = new ArrayList<Object>();
    		for(Object o : op.getContent())
    		{	
    			if(o instanceof String)	
    				continue;
    			filtered.add(o);	
    		}
        	return filtered;   	
        }    	
    	else
    		return op.getContent();
    }
	

	private boolean isConstant(Operand op){
		return (getFilteredContent(op).size()==1 && getFilteredContent(op).get(0) instanceof String);
			
	}
	
	
	

	private eu.itesla_project.modules.contingencies.Operator toOperator(
			Operand op, Map<BigInteger, String> sequenceActions) 
	{
		List<Object> content=getFilteredContent(op);
		
		if( isConstant(op))
		{		
			//String v=(String)op.getContent().get(0);
			// put in the logical expression the id of the action, instead of the number
			String v=sequenceActions.get(new BigInteger((String)content.get(0)));
			//System.out.println("Unary op "+content.get(0));
			return new UnaryOperator(v);			
		}
	
		for(Object o : content)
		{
			if(o instanceof String)
			{
				throw new RuntimeException("Operand mismatch: "+o);
			}
			else if(o instanceof And)
			{
				//System.out.println("And ");
				And aa = (And)o;
				if(aa.getOperand().size()==2)
					return new BinaryOperator(OperatorType.AND,toOperator(aa.getOperand().get(0), sequenceActions),toOperator(aa.getOperand().get(1), sequenceActions));
				else
					throw new RuntimeException("Operand mismatch");
			}
			else if(o instanceof Or)
			{
				//System.out.println("Or ");
				Or aa = (Or)o;
				
				if(aa.getOperand().size()==2)
					return new BinaryOperator(OperatorType.OR,toOperator(aa.getOperand().get(0), sequenceActions),toOperator(aa.getOperand().get(1), sequenceActions));
				else
					throw new RuntimeException("Operand mismatch");

			}
			else if(o instanceof Then)
			{
				//System.out.println("then ");
				Then aa = (Then)o;
				
				if(aa.getOperand().size()==2)
					return new BinaryOperator(OperatorType.THEN,toOperator(aa.getOperand().get(0), sequenceActions),toOperator(aa.getOperand().get(1), sequenceActions));
				else
					throw new RuntimeException("Operand mismatch");
			}
		}
		return null;
	}


	@Override
	public Collection<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> getActionsCtgAssociations()
	{
		
		List<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> accociationList = new ArrayList<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation>();
		ActionCtgAssociations xmlActionContAssociation = actionContingencies.getActionCtgAssociations();
		List<Association> xmlAssociations= xmlActionContAssociation.getAssociation();
		if (xmlAssociations == null) 
		{
			LOGGER.warn(" Action Contingencies associations not found");
			return null;
		} 
		else 
		{
			
			
			for (Association association : xmlAssociations) 
			{
				List<Contingency> 	xmlContingencies	=association.getContingency();
				List<Constraint> 	xmlConstraints		=association.getConstraint();
				List<Action> 		xmlActions 			=association.getAction();
				
				//ActionsContingenciesAssociationImpl(List<String>  _contingencies , List<Constraint>  _constraints, List<String> _actions)
				List<String> ctgIds = new ArrayList<String>();
				for (Contingency c: xmlContingencies) 
				{
					ctgIds.add(c.getId());
				}

				List<eu.itesla_project.modules.contingencies.Constraint> constraints = new ArrayList<eu.itesla_project.modules.contingencies.Constraint>();
				for (Constraint c: xmlConstraints) 
				{
						
					constraints.add(new ConstraintImpl(c.getEquipment(), c.getValue(), XmlActionsContingenciesUtils.getConstraintType(c.getType())));
				}
				
				List<String> actionIds = new ArrayList<String>();
				for (Action a: xmlActions) 
				{
					actionIds.add(a.getId());
					
				}
				
				accociationList.add(new ActionsContingenciesAssociationImpl(ctgIds , constraints, actionIds) );
							
			}		
			
			}
		return accociationList;
	}

	@Override
	public Collection<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> getActionsCtgAssociationsByContingency(String contingencyId) 
	{
		List<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> accociationList = new ArrayList<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation>();
		ActionCtgAssociations xmlActionContAssociation = actionContingencies.getActionCtgAssociations();
		List<Association> xmlAssociations= xmlActionContAssociation.getAssociation();
		if (xmlAssociations == null) 
		{
			LOGGER.warn(" Actions Contingencies associations not found");
			return null;
		} 
		else 
		{
			for (Association association : xmlAssociations) 
			{
				List<Contingency> 	xmlContingencies	=association.getContingency();
				
				if (xmlContingencies.contains(contingencyId))
				{ 
					List<Constraint> 	xmlConstraints		=association.getConstraint();
					List<Action> 		xmlActions 			=association.getAction();
					
					List<String> ctgIds = new ArrayList<String>();
					for (Contingency c: xmlContingencies) 
					{
						ctgIds.add(c.getId());
					}
	
					List<eu.itesla_project.modules.contingencies.Constraint> constraints = new ArrayList<eu.itesla_project.modules.contingencies.Constraint>();
					for (Constraint c: xmlConstraints) 
					{
							
						constraints.add(new ConstraintImpl(c.getEquipment(), c.getValue(), XmlActionsContingenciesUtils.getConstraintType(c.getType())));
					}
					
					List<String> actionIds = new ArrayList<String>();
					for (Action a: xmlActions) 
					{
						actionIds.add(a.getId());
						
					}
					
					accociationList.add(new ActionsContingenciesAssociationImpl(ctgIds , constraints, actionIds) );
				}
			}		
		}
	
		return accociationList;
	}
	
	
	/** 
	 * @param contingencyId
	 * @return List<String> action 
	 * 
	 */
	public List<String> getActionsByContingency(String contingencyId) 
	{
		List<String> actions = new ArrayList<String>();		

		ActionCtgAssociations actCont = actionContingencies.getActionCtgAssociations();
		
		List<Association> associations = actCont.getAssociation();
		for (Association association : associations) 
		{
			List<Contingency> contingencies = association.getContingency();
			for (Contingency c : contingencies) 
			{
				if (c.getId().equals(contingencyId)) 
				{
					List<Action> acs = association.getAction();
					for (Action a: acs )
						actions.add(a.getId());
				}
			}
		}
		return actions;
	}
	
	
	/** 
	 * @param all network association 
	 * @return List<Association> 
	 * 
	 */
	/*
	@Override
	public List<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> getActionsCtgAssociation(Network network) 
	{
		List<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> actionCtgAssociation = new ArrayList<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation>();
		
		ActionCtgAssociations xmlActionContAssociation = actionContingencies.getActionCtgAssociations();
		List<Association> xmlAssociations= xmlActionContAssociation.getAssociation();
		if (xmlAssociations == null) 
		{
			LOGGER.warn(" Action Contingencies associations not found");
			return null;
		} 
		for (Association association : xmlAssociations) 
		{
			List<eu.itesla_project.modules.contingencies.Constraint> constraints 	= new ArrayList<eu.itesla_project.modules.contingencies.Constraint> ();
			List<String> contingenciesId	= new ArrayList<String>();
			List<String> actionsId 			= new ArrayList<String>();
			//get only network action
			
			List<Contingency> 	xmlContingencies	=association.getContingency();
			List<Action> 		xmlactions 			= association.getAction();
			List<Constraint> 	xmlConstraints		=association.getConstraint();
			
			for (Action a : xmlactions)		
			{
				System.out.println( "action  "+a.getId());
				if (XmlActionsContingenciesUtils.containsAction( a.getId(), this.getActions(network))) 
					actionsId.add(a.getId());
					
			}
			
			//get only network contingencies	 
			
			for (Contingency ctg : xmlContingencies) 
			{
				System.out.println( "contingency:  "+ctg.getName());
				if (ctg.getEquipments() != null) 
				{
					for (eu.itesla_project.iidm.actions_contingencies.xml.mapping.Equipment  equip :ctg.getEquipments().getEquipment())
					{
						System.out.println(  "equip " + equip +" " +network.getIdentifiable(equip.getId()));
						if (network.getIdentifiable(equip.getId()) != null)
						{ 
						   contingenciesId.add(ctg.getId());
						   break;
						}
					}
				}else System.out.println(  " no equips found " );
					
			}
				
			//get only network constraints
			
			for (Constraint c : xmlConstraints) 
			{
				 String equip=c.getEquipment();
				 if ( network.getIdentifiable(equip) != null)
					 constraints.add(new ConstraintImpl(c.getEquipment(), c.getValue(), c.getType()));
			}
			
			actionCtgAssociation.add(new ActionsContingenciesAssociationImpl(contingenciesId ,constraints, actionsId));
		}
		
		return actionCtgAssociation;
	}*/
	
	@Override
	public List<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> getActionsCtgAssociations(Network network)
	{
		LOGGER.info("Getting actions/contingencies associations for network {}", network.getId());
		List<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation> accociationList = new ArrayList<eu.itesla_project.modules.contingencies.ActionsContingenciesAssociation>();
		ActionCtgAssociations xmlActionContAssociation = actionContingencies.getActionCtgAssociations();
		List<Association> xmlAssociations= xmlActionContAssociation.getAssociation();
		if (xmlAssociations == null) 
		{
			LOGGER.warn(" Action Contingencies associations not found");
			return null;
		} 
		else 
		{
			
			List<eu.itesla_project.modules.contingencies.Action> networkActions = getActions(network);
			List<eu.itesla_project.modules.contingencies.ActionPlan> networkActionPlans = new ArrayList<>(getActionPlans(network));
			// pre-index tie lines
			Map<String, String> tieLines = new HashMap<>();
	        for (Line l : network.getLines()) {
	            if (l.isTieLine()) {
	                TieLine tl = (TieLine) l;
	                tieLines.put(tl.getHalf1().getId(), tl.getId());
	                tieLines.put(tl.getHalf2().getId(), tl.getId());
	            }
	        }
			for (Association association : xmlAssociations) 
			{
				List<Contingency> 	xmlContingencies	=association.getContingency();
				List<Constraint> 	xmlConstraints		=association.getConstraint();
				List<Action> 		xmlActions 			=association.getAction();
				
				//ActionsContingenciesAssociationImpl(List<String>  _contingencies , List<Constraint>  _constraints, List<String> _actions)
				List<String> ctgIds = new ArrayList<String>();
				for (Contingency c: xmlContingencies) 
				{
					boolean found = false;
					for(Contingency ctg : actionContingencies.getContingencies().getContingency())
					{
						if(ctg.getName().equals(c.getId()))
						{
							found = true;
							if (ctg.getEquipments()!=null){
								for ( Equipment eq:ctg.getEquipments().getEquipment())
								{
									if (network.getIdentifiable(eq.getId())!= null){
										ctgIds.add(c.getId());
										break;	
									} else if (tieLines.containsKey(eq.getId())) {
										ctgIds.add(c.getId());
										break;
									} else
										LOGGER.warn("Equipment {} referred in contingency (in association) does not belong to network {}, skipping it", eq.getId(), network.getId());
								}
							}
							break;
						}
					}
					if ( !found )
						LOGGER.warn("Contingency {} referred in actions/contingencies associations not in the DB: skipping it", c.getId());
					
				}

				List<eu.itesla_project.modules.contingencies.Constraint> constraints = new ArrayList<eu.itesla_project.modules.contingencies.Constraint>();
				for (Constraint con: xmlConstraints) 
				{
					if (network.getIdentifiable(con.getEquipment())!= null)	
						constraints.add(new ConstraintImpl(con.getEquipment(), con.getValue(), XmlActionsContingenciesUtils.getConstraintType(con.getType())));
				 	else if (tieLines.containsKey(con.getEquipment()))
				 		constraints.add(new ConstraintImpl(tieLines.get(con.getEquipment()), con.getValue(), XmlActionsContingenciesUtils.getConstraintType(con.getType())));
					else
						LOGGER.warn("Equipment {} referred in constraints does not belong to network {}, skipping it", con.getEquipment(), network.getId());
				}
				
				List<String> actionIds = new ArrayList<String>();
				for (Action a: xmlActions) 
				{
					boolean found = false;
					for (eu.itesla_project.modules.contingencies.Action action : networkActions) {
						if ( action.getId().equals(a.getId())) {
							found = true;
							actionIds.add(a.getId());
							break;
						}
					}
					for (eu.itesla_project.modules.contingencies.ActionPlan actionPlan : networkActionPlans) {
						if ( actionPlan.getName().equals(a.getId())) {
							found = true;
							actionIds.add(a.getId());
							break;
						}
					}
					if ( !found )
						LOGGER.warn("Action/Action Plan {} referred in actions/contingencies associations not in the DB: skipping it", a.getId());
				}
				
				accociationList.add(new ActionsContingenciesAssociationImpl(ctgIds , constraints, actionIds) );
							
			}		
			
			}
		return accociationList;
	}
	
	
	private List<ActionElement> getActionElements(ElementaryAction ele,	 Network network) {
		if ( zonesMapping.isEmpty() )
			getZones();
		
		// pre-index tie lines
        Map<String, String> tieLines = new HashMap<>();
        for (Line l : network.getLines()) {
            if (l.isTieLine()) {
                TieLine tl = (TieLine) l;
                tieLines.put(tl.getHalf1().getId(), tl.getId());
                tieLines.put(tl.getHalf2().getId(), tl.getId());
            }
        }
		List<ActionElement> elements = new ArrayList<>();
		for (LineOperation lo : ele.getLineOperation()) {
			String lineId = lo.getId();
//			String substationId = lo.getSubstation();
//			if ( network.getLine(lineId) != null && network.getSubstation(substationId) != null )
//				elements.add(new LineTrippingAction(lineId, lo.getSubstation(), lo.getImplementationTime(), lo.getAchievmentIndex()));
//			else if ( tieLines.containsKey(lineId) && network.getSubstation(substationId) != null )
//				elements.add(new LineTrippingAction(tieLines.get(lineId), lo.getSubstation(), lo.getImplementationTime(), lo.getAchievmentIndex()));
//			else
//				LOGGER.warn("LineOperation : line id " + lineId + " or substation id " + substationId + " not found");
			if (network.getLine(lineId) != null)
				elements.add(new LineTrippingAction(lineId, lo.getImplementationTime(), lo.getAchievmentIndex()));
			else if (tieLines.containsKey(lineId))
				elements.add(new LineTrippingAction(tieLines.get(lineId), lo.getImplementationTime(), lo.getAchievmentIndex()));
			else
				LOGGER.warn("LineOperation : Line id not found: " + lineId);
		}

		for (GenerationOperation go : ele.getGenerationOperation()) {
			String genId = go.getId();
			if (network.getGenerator(genId) != null) {
				if (go.getAction().equals("stop")
						|| go.getAction().equals("stopPumping"))
					elements.add(new GeneratorStopAction(genId, go.getImplementationTime(), go.getAchievmentIndex()));
				else if (go.getAction().equals("start")
						|| go.getAction().equals("startPumping"))
					elements.add(new GeneratorStartAction(genId, go.getImplementationTime(), go.getAchievmentIndex()));
			}
			else
				LOGGER.warn("GenerationOperation : generator id not found: " + genId);
		}

		for (SwitchOperation sw : ele.getSwitchOperation()) {

			String switchId = sw.getId();
			String vlId = null;

			// it seems not to work
//			for (Zone z : ele.getZones().getZone()) // search associated zones
//			{
//				List<VoltageLevel> levels = z.getVoltageLevels()
//						.getVoltageLevel();
//				for (VoltageLevel l : levels) {
//					String lid = l.getID();
//					eu.itesla_project.iidm.network.VoltageLevel vl = network
//							.getVoltageLevel(lid);
//					if (vl != null
//							&& vl.getBusBreakerView().getSwitch(switchId) != null) {
//						vlId = vl.getId();
//						break;
//					}
//				}
//			}
			// it replaces the code above
			Zones eleZones = ele.getZones();
			if ( eleZones != null ) {
				for(BigInteger zoneNum : eleZones.getNum()) {
					String zoneId = zonesMapping.get(zoneNum);
					eu.itesla_project.modules.contingencies.Zone z = getZone(zoneId);
					Collection<eu.itesla_project.modules.contingencies.VoltageLevel> levels = z.getVoltageLevels();
					for (eu.itesla_project.modules.contingencies.VoltageLevel l : levels) {
						String lid = l.getId();
						eu.itesla_project.iidm.network.VoltageLevel vl = network
								.getVoltageLevel(lid);
						if (vl != null
								&& vl.getBusBreakerView().getSwitch(switchId) != null) {
							vlId = vl.getId();
							break;
						}
					}
				}
			}

			if (vlId == null) // search all network
			{
				LOGGER.info("No match found for "+switchId +" among the switches of the switch zones voltage levels. Search continues on all network switches... " );
				Iterator<eu.itesla_project.iidm.network.VoltageLevel> it = network
						.getVoltageLevels().iterator();
				while (it.hasNext()) {
					eu.itesla_project.iidm.network.VoltageLevel vl = it.next();
					if (vl.getBusBreakerView().getSwitch(switchId) != null) {
						vlId = vl.getId();
						break;
					}

				}
			}

			if (vlId != null) {
				if (sw.getAction().equals("opening"))
					elements.add(new SwitchOpeningAction(vlId, switchId, sw.getImplementationTime(), sw.getAchievmentIndex()));
				else if (sw.getAction().equals("closing"))
					elements.add(new SwitchClosingAction(vlId, switchId, sw.getImplementationTime(), sw.getAchievmentIndex()));
			} else
				LOGGER.warn("No match found for " + switchId + " among all network switches. The switch is eliminated from the action element list" );

		}

		for (PstOperation pst : ele.getPstOperation()) {
			String transformerId = pst.getId();
			if ( network.getTwoWindingsTransformer(transformerId) != null ) {
				if (pst.getAction().equals("shunt"))
					elements.add(new ShuntAction(pst.getId(), pst.getImplementationTime(), pst.getAchievmentIndex()));
				else if (pst.getAction().equals("tapChange"))
					elements.add(new TapChangeAction(pst.getId(), pst.getImplementationTime(), pst.getAchievmentIndex()));
				else if (pst.getAction().equals("opening")) {
//					String substationId = pst.getSubstation();
//					if ( network.getSubstation(substationId) != null )
//						elements.add(new TransformerOpeningAction(pst.getId(), pst.getSubstation(), pst.getImplementationTime(), pst.getAchievmentIndex()));
//					else
//						LOGGER.warn("PstOperation : substation id " + substationId + " not found");
					elements.add(new TransformerOpeningAction(pst.getId(), pst.getImplementationTime(), pst.getAchievmentIndex()));
				}
				else
					LOGGER.warn("pst operation not supported : " + pst.getAction());
			} else
				LOGGER.warn("PstOperation : transformer id " + transformerId + " not found");
		}

		for (Redispatching redispatching : ele.getRedispatching()) {
			elements.add(new GenerationRedispatching(redispatching.getGenerator(), redispatching.getAchievmentIndex(), redispatching.getImplementationTime()));
		}

		return elements;
	}



	

	
	
}
