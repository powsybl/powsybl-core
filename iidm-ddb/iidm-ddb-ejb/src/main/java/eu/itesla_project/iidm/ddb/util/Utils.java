/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.util;

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
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.model.TableRow;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Utils {
	
	public static List<String> getEquipmentData(Equipment eq) {
		List<String> ret=new ArrayList<String>();
		ret.add("Equipment id:" + eq.getId() + ", cimId:" + eq.getCimId());

		ModelTemplateContainer mtc1 = eq.getModelContainer();
		if (mtc1 != null) {
			ret.add(" Model template container " + mtc1.getDdbId() + ", "
					+ mtc1.getComment());
			List<ModelTemplate> mlist = mtc1.getModelTemplates();
			for (ModelTemplate modelTemplate : mlist) {
				ret.add("  Model template - " + modelTemplate.getComment() + ", "
						+ modelTemplate.getSimulator());
				List<DefaultParameters> dparams=modelTemplate.getDefaultParameters();
				int defsetindex=1;
				for (DefaultParameters defaultParameters : dparams) {
					ret.add("   defaultparameters - " + defsetindex);
					List<Parameter> dparams2=defaultParameters.getParameters();
					for (Parameter parameter : dparams2) {
						ret.add("     param - "+ parameter.getName() + ", "
								+ parameter.getValue());
						
					}
					defsetindex=defsetindex+1;
				}
			}
		} else {
			ret.add(" No model template container");
		}
		ParametersContainer pc1 = eq.getParametersContainer();
		if (pc1 != null) {
			ret.add(" Parameter  container " + pc1.getDdbId());
			List<Parameters> plist = pc1.getParameters();
			for (Parameters parameters : plist) {
				ret.add("  Parameters - " + parameters.getSimulator());
				List<Parameter> plistp = parameters.getParameters();
				for (Parameter parameter : plistp) {
					ret.add("   Parameter - " + parameter.getName() + ", "
							+ parameter.getValue());
				}
			}
		} else {
			ret.add(" No parameter container");
		}
		return ret;
		
	}

	public static void dump(Equipment eq, Logger log) {
		List<String> equipData=Utils.getEquipmentData(eq);
		for (String dataLine : equipData) {
			log.info(dataLine);
		}
		
		/*
		log.info("Equipment id:" + eq.getId() + ", cimId:" + eq.getCimId());

		ModelTemplateContainer mtc1 = eq.getModelContainer();
		log.info(" Model template container " + mtc1.getDdbId() + ", "
				+ mtc1.getComment());
		List<ModelTemplate> mlist = mtc1.getModelTemplates();
		for (ModelTemplate modelTemplate : mlist) {
			log.info("  Model template - " + modelTemplate.getComment() + ", "
					+ modelTemplate.getSimulator());
			List<DefaultParameters> dparams=modelTemplate.getDefaultParameters();
			int defsetindex=1;
			for (DefaultParameters defaultParameters : dparams) {
				log.info("   defaultparameters - " + defsetindex);
				List<Parameter> dparams2=defaultParameters.getParameters();
				for (Parameter parameter : dparams2) {
					log.info("     param - "+ parameter.getName() + ", "
							+ parameter.getValue());
					
				}
				defsetindex=defsetindex+1;
			}

		}
		ParametersContainer pc1 = eq.getParametersContainer();
		log.info(" Parameter  container " + pc1.getDdbId());
		List<Parameters> plist = pc1.getParameters();
		for (Parameters parameters : plist) {
			log.info("  Parameters - " + parameters.getSimulator());
			List<Parameter> plistp = parameters.getParameters();
			for (Parameter parameter : plistp) {
				log.info("   Parameter - " + parameter.getName() + ", "
						+ parameter.getValue());
			}
		}
		*/
		//log.info("-------------------------------------------");
	}

	public static void dump(Internal eq, Logger log) {
		log.info("Internal id:" + eq.getId() + ", nativeId:" + eq.getNativeId());

		ModelTemplateContainer mtc1 = eq.getModelContainer();
		if (mtc1!=null) {
		log.info(" Model template container " + mtc1.getDdbId() + ", "
				+ mtc1.getComment());
		List<ModelTemplate> mlist = mtc1.getModelTemplates();
		for (ModelTemplate modelTemplate : mlist) {
			log.info("  Model template - " + modelTemplate.getComment() + ", "
					+ modelTemplate.getSimulator());
			List<DefaultParameters> dparams=modelTemplate.getDefaultParameters();
			int defsetindex=1;
			for (DefaultParameters defaultParameters : dparams) {
				log.info("   defaultparameters - " + defsetindex);
				List<Parameter> dparams2=defaultParameters.getParameters();
				for (Parameter parameter : dparams2) {
					log.info("     param - "+ parameter.getName() + ", "
							+ parameter.getValue());
					
				}
				defsetindex=defsetindex+1;
			}
		}
			
		}
		ParametersContainer pc1 = eq.getParametersContainer();
		if (pc1!=null) {
		log.info(" Parameter  container " + pc1.getDdbId());
		List<Parameters> plist = pc1.getParameters();
		for (Parameters parameters : plist) {
			log.info("  Parameters - " + parameters.getSimulator());
			List<Parameter> plistp = parameters.getParameters();
			for (Parameter parameter : plistp) {
				log.info("   Parameter - " + parameter.getName() + ", "
						+ parameter.getValue());
			}
		}
		}
		//log.info("-------------------------------------------");
	}

	
	
	public static void dump(ModelTemplateContainer m, Logger log) {
		log.info("ModelTemplateContainer:- ddbid:" + m.getDdbId() +", "+ m.getComment() +",  (id:" + m.getId()+")");
		List<ModelTemplate> mlist = m.getModelTemplates();
		for (ModelTemplate modelTemplate : mlist) {
			log.info("  Model template - " + modelTemplate.getComment() + ", "
					+ modelTemplate.getSimulator());
			List<DefaultParameters> dparams=modelTemplate.getDefaultParameters();
			int defsetindex=1;
			for (DefaultParameters defaultParameters : dparams) {
				log.info("   defaultparameters - " + defsetindex);
				List<Parameter> dparams2=defaultParameters.getParameters();
				for (Parameter parameter : dparams2) {
					log.info("     param - "+ parameter.getName() + ", "
							+ parameter.getValue());
					
				}
				defsetindex=defsetindex+1;
			}
		}
	}

	
	static Class jaxbClasses[] = { Equipment.class, Parameter.class,
			Internal.class, SimulatorInst.class, ModelTemplateContainer.class,
			ParametersContainer.class, ParameterString.class,
			ParameterInteger.class, ParameterFloat.class,
			ParameterBoolean.class, ParameterTable.class, TableRow.class };

	public static <T> T unserialize(InputStream file) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(jaxbClasses);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		T object = (T) jaxbUnmarshaller.unmarshal(file);
		return object;
	}

	public static <T> OutputStream serialize(T object, OutputStream stream)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(jaxbClasses);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(object, stream);
		return stream;
	}

}
