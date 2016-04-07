/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.service;


import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.jws.WebMethod; 
import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.OutInterceptors;
import org.jboss.ejb3.annotation.SecurityDomain;

import eu.itesla_project.iidm.ddb.model.ConnectionSchema;
import eu.itesla_project.iidm.ddb.model.DefaultParameters;
import eu.itesla_project.iidm.ddb.model.Equipment;
import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.ParameterBoolean;
import eu.itesla_project.iidm.ddb.model.ParameterFloat;
import eu.itesla_project.iidm.ddb.model.ParameterInteger;
import eu.itesla_project.iidm.ddb.model.ParameterString;
import eu.itesla_project.iidm.ddb.model.ParameterTable;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Stateless
@SecurityDomain("other")
@Remote(DDBManager.class)
@WebService
@EndpointProperties(value = {
        @EndpointProperty(key =  "ws-security.validate.token",value="false")
})
@InInterceptors(interceptors = {
	  "org.apache.cxf.interceptor.LoggingInInterceptor",
	  "eu.itesla_project.iidm.ddb.util.WsSecurityInterceptor",
      "org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingPolicyInterceptor",
      "org.apache.cxf.interceptor.security.SimpleAuthorizingInterceptor"}
)
@OutInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingOutInterceptor")
@XmlSeeAlso({ 
	eu.itesla_project.iidm.ddb.model.ParameterBoolean.class, 
	eu.itesla_project.iidm.ddb.model.ParameterString.class, 
	eu.itesla_project.iidm.ddb.model.ParameterInteger.class, 
	eu.itesla_project.iidm.ddb.model.ParameterFloat.class,
	eu.itesla_project.iidm.ddb.model.ParameterTable.class })
@RolesAllowed({"user"})
public class DDBManagerBean implements DDBManager {
	@Inject
	private Logger log;

	@Inject
	private EntityManager em;
	

	@Resource
	SessionContext sctxLookup;

	// @Inject
	// private Event<Equipment> equipmentE;
	
	protected Principal getCallerPrincipal() {
//      InitialContext ic = new InitialContext();
//      SessionContext sctxLookup = (SessionContext) ic.lookup("java:comp/EJBContext");
		
//      System.out.println("look up EJBContext by standard name: " + sctxLookup);
//      System.out.println("                     Principal name: " + sctxLookup.getCallerPrincipal().getName());
		Principal cPrincipal=sctxLookup.getCallerPrincipal();
		return cPrincipal;
}

	@WebMethod(operationName = "saveModelTemplateContainer")
	public ModelTemplateContainer save(ModelTemplateContainer p) {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting ModelTemplateContainer: " + p.getDdbId() + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		return p;
	}

	@WebMethod(operationName = "saveParametersContainer")
	public ParametersContainer save(ParametersContainer p)  {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting ParametersContainer: " + p.getDdbId() + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		return p;
	}

	@WebMethod(operationName = "saveParameters")
	public Parameters save(Parameters p)  {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting Parameters: " + p.getId() + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		return p;
	}
	
	@WebMethod(operationName = "saveDefaultParameters")
	public DefaultParameters save(DefaultParameters p)  {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting Default parameters: " + p.getId() + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		return p;
	}

	
	@Override
	public Parameter save(Parameter p) {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting Parameter: " + p.getId() + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		return p;
	}



	
	@WebMethod(operationName = "saveEquipment")
	public Equipment save(Equipment p)  {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting Equipment: " + p.getCimId()  + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		// em.flush();
		return p;
	}

	@WebMethod(operationName = "saveInternal")
	public Internal save(Internal p) {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting Internal: " + p.getNativeId() + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		return p;
	}
	

	public List<Equipment> findEquipmentsAll() {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<Equipment> query = em.createQuery(
				"SELECT m FROM Equipment m order by m.id", Equipment.class);
		return query.getResultList();
	}
	
	public int findEquipmentAllCount() {
		Principal cPrincipal= getCallerPrincipal();
		Query query = em.createQuery(
				"SELECT count(m) FROM Equipment m");
		return ((Long)query.getSingleResult()).intValue();
	}


	public List<Equipment> findEquipmentsAllMaxResults(int firstResult, int maxResult) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<Equipment> query = em.createQuery(
				"SELECT m FROM Equipment m order by m.id", Equipment.class);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResult);
		
		return query.getResultList();
	}

	
	public Equipment findEquipment(String cimId) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<Equipment> query = em.createQuery(
				"SELECT m FROM Equipment m WHERE m.cimId = :arg1",
				Equipment.class);
		query.setParameter("arg1", cimId);
		List<Equipment> res = query.getResultList();
		return (res.size() > 0 ? res.get(0) : null);
	}

	public List<Internal> findInternalsAll() {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<Internal> query = em.createQuery("SELECT m FROM Internal m order by m.id",
				Internal.class);
		return query.getResultList();
	}
	
	public int findInternalsAllCount() {
		Principal cPrincipal= getCallerPrincipal();
		Query query = em.createQuery(
				"SELECT count(m) FROM Internal m");
		return ((Long)query.getSingleResult()).intValue();
	}

	public List<Internal> findInternalsAllMaxResults(int firstResult, int maxResult) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<Internal> query = em.createQuery("SELECT m FROM Internal m order by m.id",
				Internal.class);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResult);
		return query.getResultList();
	}

	public Internal findInternal(String nativeId) {
		Principal cPrincipal= getCallerPrincipal();
		if ((nativeId == null)) {
			throw new RuntimeException("nativeId must be not null");
		}
	    TypedQuery<Internal> query = em.createQuery(
				"SELECT m FROM Internal m WHERE m.nativeId = :arg1",
				Internal.class);
		query.setParameter("arg1", nativeId);
		List<Internal> res = query.getResultList();
		return (res.size() > 0 ? res.get(0) : null);
	}

	public List<ModelTemplateContainer> findModelTemplateContainerAll() {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ModelTemplateContainer> query = em.createQuery(
				"SELECT m FROM ModelTemplateContainer m order by m.id",
				ModelTemplateContainer.class);
		return query.getResultList();
	}
	
	public int findModelTemplateContainerAllCount() {
		Principal cPrincipal= getCallerPrincipal();
		Query query = em.createQuery(
				"SELECT count(m) FROM ModelTemplateContainer m");
		return ((Long)query.getSingleResult()).intValue();
	}

	public List<ModelTemplateContainer> findModelTemplateContainerAllMaxResults(int firstResult, int maxResult) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ModelTemplateContainer> query = em.createQuery("SELECT m FROM ModelTemplateContainer m order by m.id",
				ModelTemplateContainer.class);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResult);
		return query.getResultList();
	}

	
	public ModelTemplateContainer findModelTemplateContainer(String ddbId) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ModelTemplateContainer> query = em.createQuery(
				"SELECT m FROM ModelTemplateContainer m WHERE m.ddbId = :arg1",
				ModelTemplateContainer.class);
		query.setParameter("arg1", ddbId);
		List<ModelTemplateContainer> res = query.getResultList();
		return (res.size() > 0 ? res.get(0) : null);
	}

	public ParametersContainer findParametersContainer(String ddbId) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ParametersContainer> query = em.createQuery(
				"SELECT m FROM ParametersContainer m WHERE m.ddbId = :arg1",
				ParametersContainer.class);
		query.setParameter("arg1", ddbId);
		List<ParametersContainer> res = query.getResultList();
		return (res.size() > 0 ? res.get(0) : null);
	}

	public List<ParametersContainer> findParametersContainerAll() {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ParametersContainer> query = em.createQuery(
				"SELECT m FROM ParametersContainer m order by m.id",
				ParametersContainer.class);
		List<ParametersContainer> res = query.getResultList();
		return res;
	}
	
	public int findParametersContainerAllCount() {
		Principal cPrincipal= getCallerPrincipal();
		Query query = em.createQuery(
				"SELECT count(m) FROM ParametersContainer m");
		return ((Long)query.getSingleResult()).intValue();
	}

	public List<ParametersContainer> findParametersContainerAllMaxResults(int firstResult, int maxResult) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ParametersContainer> query = em.createQuery("SELECT m FROM ParametersContainer m order by m.id",
				ParametersContainer.class);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResult);
		return query.getResultList();
	}

	
	@WebMethod(operationName = "deleteEquipment")
	public void delete(Equipment p) {
		Principal cPrincipal= getCallerPrincipal();
		if (p==null) {
			throw new RuntimeException("Equipment must be not null");
		}
		log.log(Level.FINE, "Deleting equipment:  " + p.getId());

		Equipment mtc1 = em.merge(p);
		em.remove(mtc1);
		// parameterEventSrc.fire(parameter);
	}

	@WebMethod(operationName = "deleteModelTemplateContainer")
	public void delete(ModelTemplateContainer p) {
		Principal cPrincipal= getCallerPrincipal();
		if (p==null) {
			throw new RuntimeException("ModelTemplateContainer must be not null");
		}
		log.log(Level.FINE, "Deleting modeltemplatecontainer:  " + p.getId());

		ModelTemplateContainer mtc = em.merge(p);
		em.remove(mtc);
		// parameterEventSrc.fire(parameter);
	}

	public SimulatorInst findSimulator(Simulator sim, String version) {
		Principal cPrincipal= getCallerPrincipal();
		if ((sim==null) || (version==null)) {
			throw new RuntimeException("Simulator and version must be not null");
		}
		TypedQuery<SimulatorInst> query = em
				.createQuery(
						"SELECT m FROM SimulatorInst m WHERE m.version = :arg1 and m.simulator = :arg2",
						SimulatorInst.class);
		query.setParameter("arg1", version);
		query.setParameter("arg2", sim);
		List<SimulatorInst> res = query.getResultList();
		return ((res.size() == 0) ? null : res.get(0));
	}

	public List<SimulatorInst> findSimulatorsAll() {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<SimulatorInst> query = em
				.createQuery(
						"SELECT m FROM SimulatorInst m order by m.id",
						SimulatorInst.class);
		List<SimulatorInst> res = query.getResultList();
		return res;
	}
	
	public int findSimulatorsAllCount() {
		Principal cPrincipal= getCallerPrincipal();
		Query query = em.createQuery(
				"SELECT count(m) FROM SimulatorInst m");
		return ((Long)query.getSingleResult()).intValue();
	}

	@WebMethod(operationName = "saveSimulator")
	public SimulatorInst save(SimulatorInst p)
	 {
		Principal cPrincipal= getCallerPrincipal();
		if (p==null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		log.log(Level.FINE, "Persisting SimulatorInst" + p);
		if (p.getId() == null)
			em.persist(p);
		else
			em.merge(p);
		// parameterEventSrc.fire(parameter);
		return p;
	 }

	@WebMethod(operationName = "deleteSimulator")
	public void delete(SimulatorInst simInst)
	 {
		Principal cPrincipal= getCallerPrincipal();
		if (simInst==null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		log.log(Level.FINE, "Deleting simulator:  " + simInst.getId());

		SimulatorInst simInst2 = em.merge(simInst);
		em.remove(simInst2);
		// parameterEventSrc.fire(parameter);
	 }
	
	
	/*
	 * equipment and simulator must be not null
	 */
	private ModelTemplate findModelTemplate(ModelTemplateContainer mtc1, SimulatorInst simulator) {
		if ((mtc1 == null) || (simulator == null)) {
			throw new RuntimeException("ModelTemplateContainer and SimulatorInst must be not null");
		}
		List<ModelTemplate> mtList = mtc1.getModelTemplates();
		for (ModelTemplate modelTemplate : mtList) {
			if (modelTemplate.getSimulator().getId() == simulator.getId()) {
				return modelTemplate;
			}
		}
		return null;
	}
	

	/*
	 * equipment and simulator must be not null
	 */
	@WebMethod(operationName = "findModelTemplateEquipment")
	public ModelTemplate findModelTemplate(Equipment equipment,
			SimulatorInst simulator) {
		if ((equipment == null) || (simulator == null)) {
			throw new RuntimeException("Equipment and SimulatorInst must be not null");
		}
		Principal cPrincipal= getCallerPrincipal();
		return ((equipment==null) ? null : this.findModelTemplate(equipment.getModelContainer(), simulator));
	}
	
	
	@SuppressWarnings("unchecked")
	private <T extends Parameter> T getParameterByNameAndType(List<Parameter> parList, String name,  Class<T> parClass) {
		for (Parameter parameter : parList) {
			if ((name.equals(parameter.getName()))
					&& (parClass.isInstance(parameter))) {
				return (T) parameter;
			}
		}
		return null;
	}
	
	
	private <T extends Parameter> T getParameterOrDefaultByNameAndType(Equipment equipment, SimulatorInst simulator,
			String name,  Class<T> parClass) {
		//get parameters for the current internal,simulator
		Parameters pars=this.findParameters(equipment,simulator);
		//first try retrieving parameter by name from Parameters set (has precedence over default parameter, if set)
		T par=getParameterByNameAndType(
				pars.getParameters(),
				name,
				parClass);
		if (par!=null) 
			return par;
		//here parameter is not found explicitly in the parameter container set, try default set linked to the ModelTemplate
		ModelTemplate mt=this.findModelTemplate(equipment.getModelContainer(), simulator);
		if (pars.getDefParamSetNum() > 0) {
			DefaultParameters defParams=mt.defaultParametersBySetNum(pars.getDefParamSetNum());
			if (defParams != null) {
				List<Parameter> dParamList=defParams.getParameters();
				T parD=getParameterByNameAndType(dParamList,
						name,
						parClass);
				if (parD!=null) 
					return parD;
			}
		}
		//here no parameter with given name had been found
		throw new RuntimeException("parameter with name " + name + " and type " + parClass.getName() +" is not defined");
	}


	/*
	 * equipment and simulator must be not null
	 */
	@WebMethod(operationName = "findParametersEquipment")
	public Parameters findParameters(Equipment equipment,
			SimulatorInst simulator) {
		Principal cPrincipal= getCallerPrincipal();
		ParametersContainer pc1 = equipment.getParametersContainer();
		List<Parameters> plist = pc1.getParameters();
		for (Parameters parameters : plist) {
			if (parameters.getSimulator().getId() == simulator.getId()) {
				return parameters;
			}
		}
		return null;
	}

	@WebMethod(operationName = "getFloatParameterEquipment")
	public Float getFloatParameter(Equipment equipment,
			SimulatorInst simulator, String name) {
		ParameterFloat par=getParameterOrDefaultByNameAndType(equipment, simulator,
				name,
				ParameterFloat.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getStringParameterEquipment")
	public String getStringParameter(Equipment equipment,
			SimulatorInst simulator, String name) {
		ParameterString par=getParameterOrDefaultByNameAndType(equipment, simulator,
				name,
				ParameterString.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getIntegerParameterEquipment")
	public Integer getIntegerParameter(Equipment equipment,
			SimulatorInst simulator, String name) {
		ParameterInteger par=getParameterOrDefaultByNameAndType(equipment, simulator,
				name,
				ParameterInteger.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getBooleanParameterEquipment")
	public Boolean getBooleanParameter(Equipment equipment,
			SimulatorInst simulator, String name) {
		ParameterBoolean par=getParameterOrDefaultByNameAndType(equipment, simulator,
				name,
				ParameterBoolean.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getTableParameterEquipment")
	public ParameterTable getTableParameter(Equipment equipment,
			SimulatorInst simulator, String name) {
		
		ParameterTable par=getParameterOrDefaultByNameAndType(equipment, simulator,
				name,
				ParameterTable.class);
		return ((par!=null) ? (par) : null);
	}

	
	

	/*
	 * equipment and simulator must be not null
	 */
	@WebMethod(operationName = "findModelTemplateInternal")
	public ModelTemplate findModelTemplate(Internal internal, SimulatorInst simulator) {
		Principal cPrincipal= getCallerPrincipal();
		return this.findModelTemplate(internal.getModelContainer(), simulator);
	}

	/*
	 * internal and simulator must be not null
	 */
	@WebMethod(operationName = "findParametersInternal")
	public Parameters findParameters(Internal internal, SimulatorInst simulator) {
		Principal cPrincipal= getCallerPrincipal();
		ParametersContainer pc1 = internal.getParametersContainer();
		List<Parameters> plist = pc1.getParameters();
		for (Parameters parameters : plist) {
			if (parameters.getSimulator().getId() == simulator.getId()) {
				return parameters;
			}
		}
		return null;
	}
	
	
	private <T extends Parameter> T getParameterOrDefaultByNameAndType(Internal internal, SimulatorInst simulator, 
			String name,  Class<T> parClass) {
		//get parameters for the current internal,simulator
		Parameters pars=this.findParameters(internal,simulator);
		//first try retrieving parameter by name from Parameters set (has precedence over default parameter, if set)
		T par=getParameterByNameAndType(
				pars.getParameters(),
				name,
				parClass);
		if (par!=null) 
			return par;
		//here parameter is not found explicitly in the parameter container set, try default set linked to the ModelTemplate
		ModelTemplate mt=this.findModelTemplate(internal.getModelContainer(), simulator);
		if (pars.getDefParamSetNum() > 0) {
			DefaultParameters defParams=mt.defaultParametersBySetNum(pars.getDefParamSetNum());
			if (defParams != null) {
				List<Parameter> dParamList=defParams.getParameters();
				T parD=getParameterByNameAndType(dParamList,
						name,
						parClass);
				if (parD!=null) 
					return parD;
			}
		}
		//here no parameter with given name had been found
		throw new RuntimeException("parameter with name " + name + " and type " + parClass.getName() +" is not defined");
	}

	

	@WebMethod(operationName = "getFloatParameterInternal")
	public Float getFloatParameter(Internal internal, SimulatorInst simulator, 
			String name) {
		ParameterFloat par=getParameterOrDefaultByNameAndType(
				internal,
				simulator,
				name,
				ParameterFloat.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getStringParameterInternal")
	public String getStringParameter(Internal internal, SimulatorInst simulator,
			String name) {
		ParameterString par=getParameterOrDefaultByNameAndType(
				internal,
				simulator,
				name,
				ParameterString.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getIntegerParameterInternal")
	public Integer getIntegerParameter(Internal internal, SimulatorInst simulator,
			String name) {
		ParameterInteger par=getParameterOrDefaultByNameAndType(
				internal,
				simulator,
				name,
				ParameterInteger.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getBooleanParameterInternal")
	public Boolean getBooleanParameter(Internal internal, SimulatorInst simulator,
			String name) {
		ParameterBoolean par=getParameterOrDefaultByNameAndType(
				internal,
				simulator,
				name,
				ParameterBoolean.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getTableParameterInternal")
	public ParameterTable getTableParameter(Internal internal, SimulatorInst simulator, String name) {
		ParameterTable par=getParameterOrDefaultByNameAndType(
				internal,
				simulator,
				name,
				ParameterTable.class);
		return ((par!=null) ? (par ) : null);
	}


	@Override
	@WebMethod(operationName = "deleteParametersContainer")
	public void delete(ParametersContainer p) {
		Principal cPrincipal= getCallerPrincipal();
		log.log(Level.FINE, "Deleting ParametersContainer:  " + p.getId());
		ParametersContainer mtc1 = em.merge(p);
		em.remove(mtc1);
		// parameterEventSrc.fire(parameter);
	}

	@Override
	@WebMethod(operationName = "deleteInternal")
	public void delete(Internal p) {
		Principal cPrincipal= getCallerPrincipal();
		log.log(Level.FINE, "Deleting Internal:  " + p.getId());
		Internal mtc1 = em.merge(p);
		em.remove(mtc1);
		// parameterEventSrc.fire(parameter);
	}

	@Override
	@WebMethod(operationName = "getModelDataEquipment")
	public byte[] getModelData(Equipment eqInstance, SimulatorInst simulator, String dname) {
		ModelTemplate mt=this.findModelTemplate(eqInstance, simulator);
		if (mt!=null) {
			return mt.getData(dname);
		} else return null;
	}

	@Override
	@WebMethod(operationName = "getModelDataInternal")
	public byte[] getModelData(Internal internalInstance, SimulatorInst simulator, String dname) {
		ModelTemplate mt=this.findModelTemplate(internalInstance, simulator);
		if (mt!=null) {
			return mt.getData(dname);
		} else return null;
	}

	
	// parameters container parameters accessors
	@WebMethod(operationName = "getParametersParametersContainer")
	public Parameters findParameters(ParametersContainer parametersContainer,
			SimulatorInst simulator) {
		Principal cPrincipal= getCallerPrincipal();
		if ((parametersContainer!=null) && (simulator!=null)) {
			List<Parameters> plist = parametersContainer.getParameters();
			for (Parameters parameters : plist) {
				if (parameters.getSimulator().getId() == simulator.getId()) {
					return parameters;
				}
			}
		}
		return null;
	}


	private <T extends Parameter> T getParameterByNameAndType(ParametersContainer parametersContainer, SimulatorInst simulator,
			String name,  Class<T> parClass) {
		
		List<Parameters> plist = parametersContainer.getParameters();
		Parameters pars=null;
		for (Parameters parameters : plist) {
			if (parameters.getSimulator().getId() == simulator.getId()) {
				pars=parameters;
				break;
			}
		}
		T par=getParameterByNameAndType(
				pars.getParameters(),
				name,
				parClass);
		if (par!=null) 
			return par;
		throw new RuntimeException("parameter with name " + name + " and type " + parClass.getName() +" is not defined");
	}
	

	@WebMethod(operationName = "getFloatParameterParametersContainer")	
	public Float getFloatParameter(ParametersContainer parametersContainer,
			SimulatorInst simulator, String name) {
		ParameterFloat par=getParameterByNameAndType(parametersContainer, simulator,
				name,
				ParameterFloat.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getStringParameterParametersContainer")	
	public String getStringParameter(ParametersContainer parametersContainer,
			SimulatorInst simulator, String name) {
		ParameterString par=getParameterByNameAndType(parametersContainer, simulator,
				name,
				ParameterString.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getIntegerParameterParametersContainer")	
	public Integer getIntegerParameter(ParametersContainer parametersContainer,
			SimulatorInst simulator, String name) {
		ParameterInteger par=getParameterByNameAndType(parametersContainer, simulator,
				name,
				ParameterInteger.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getBooleanParameterParametersContainer")	
	public Boolean getBooleanParameter(ParametersContainer parametersContainer,
			SimulatorInst simulator, String name) {
		ParameterBoolean par=getParameterByNameAndType(parametersContainer, simulator,
				name,
				ParameterBoolean.class);
		return ((par!=null) ? (par.getValue() ) : null);
	}

	@WebMethod(operationName = "getTableParameterParametersContainer")	
	public ParameterTable getTableParameter(ParametersContainer parametersContainer,
			SimulatorInst simulator, String name) {
		
		ParameterTable par=getParameterByNameAndType(parametersContainer, simulator,
				name,
				ParameterTable.class);
		return ((par!=null) ? (par) : null);
	}

	@WebMethod(operationName = "saveConnectionSchema")
	public ConnectionSchema save(ConnectionSchema schema) {
		Principal cPrincipal= getCallerPrincipal();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Persisting ConnectionSchema: " + schema.getCimId() + ", user: " + ((cPrincipal!=null)?cPrincipal.getName():null));
		if (schema.getId() == null)
			em.persist(schema);
		else
			em.merge(schema);
		// parameterEventSrc.fire(parameter);
		return schema;
	}

	@Override
	public ConnectionSchema findConnectionSchema(String cimId,
			SimulatorInst simulator) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ConnectionSchema> query = null;
		if (simulator == null) {
			query = em.createQuery("SELECT c FROM ConnectionSchema c WHERE c.cimId = :arg1 and c.simulator is null", ConnectionSchema.class);
			query.setParameter("arg1", cimId);
		} else {
			query = em.createQuery("SELECT c FROM ConnectionSchema c WHERE c.cimId = :arg1 and c.simulator= :arg2", ConnectionSchema.class);
			query.setParameter("arg1", cimId);
			query.setParameter("arg2", simulator);
		}
		List<ConnectionSchema> res = query.getResultList();
		return (res.size() > 0 ? res.get(0) : null);
	}

	@Override
	public List<ConnectionSchema> findConnectionSchemasAll() {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ConnectionSchema> query = em.createQuery("SELECT c FROM ConnectionSchema c order by c.id", ConnectionSchema.class);
		return query.getResultList();
	}
	
	public int findConnectionSchemasAllCount() {
		Principal cPrincipal= getCallerPrincipal();
		Query query = em.createQuery(
				"SELECT count(m) FROM ConnectionSchema m");
		return ((Long)query.getSingleResult()).intValue();
	}

	public List<ConnectionSchema> findConnectionSchemasAllMaxResults(int firstResult, int maxResult) {
		Principal cPrincipal= getCallerPrincipal();
		TypedQuery<ConnectionSchema> query = em.createQuery("SELECT c FROM ConnectionSchema c order by c.id", ConnectionSchema.class);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResult);
		return query.getResultList();
	}

	
	@Override
	public void delete(ConnectionSchema schema) {
		Principal cPrincipal= getCallerPrincipal();
		log.log(Level.FINE, "Deleting ConnectionSchema:  " + schema.getId());
		ConnectionSchema schema1 = em.merge(schema);
		em.remove(schema1);
	}


}
