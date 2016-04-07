/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo;

import java.util.Objects;

import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.mcla.montecarlo.data.BusData;
import eu.itesla_project.mcla.montecarlo.data.GeneratorData;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MCSNetworkUtils {

	public static int getBusType(Bus bus) {
		Objects.requireNonNull(bus, "bus is null");
		int type = BusData.BUS_TYPE_PQ; // a connection bus is PQ
		if ( bus.getGenerators() != null ) {
			for (Generator generator : bus.getGenerators()) {
				if ( generator.isVoltageRegulatorOn() ) {
					type = BusData.BUS_TYPE_PV; // a bus with attached a generator with voltage regulator on is PV
					break;
				}
			}
		}
//		if ( bus.getLoads() != null && bus.getLoads().iterator().hasNext() && type == 3 )
//			type = MCSBusData.BUS_TYPE_PV; // a bus with attached both loads and generators is anyway a PV
		return type;
	}
	
	public static double getNominalPower(Generator generator) {
		Objects.requireNonNull(generator, "generator is null");
		double nominalPower = Double.NaN;
		double pMax = generator.getMaxP();
		double qMax = generator.getReactiveLimits().getMaxQ(generator.getTargetP());
		nominalPower = Math.sqrt(Math.pow(pMax, 2) + Math.pow(qMax, 2));
		return nominalPower;
	}
	
	public static int getRenewableEnergySource(Generator generator) {
		Objects.requireNonNull(generator, "generator is null");
		int renewableEnergySource = GeneratorData.GENERATOR_TYPE_CONVENTIONAL;
		switch (generator.getEnergySource()) {
		case WIND:
			renewableEnergySource = GeneratorData.GENERATOR_TYPE_WIND;
			break;
		case SOLAR:
			renewableEnergySource = GeneratorData.GENERATOR_TYPE_SOLAR;
			break;
		default:
			renewableEnergySource = GeneratorData.GENERATOR_TYPE_CONVENTIONAL;
			break;
		}
		return renewableEnergySource;
	}
	
	public static int getFuelType(Generator generator) {
		Objects.requireNonNull(generator, "generator is null");
		int fuelType = GeneratorData.FUEL_TYPE_GAS;
		switch (generator.getEnergySource()) {
		case WIND:
		case SOLAR:
			fuelType = GeneratorData.FUEL_TYPE_RES;
			break;
		case HYDRO:
			fuelType = GeneratorData.FUEL_TYPE_HYDRO;
			break;
		case THERMAL:
			fuelType = GeneratorData.FUEL_TYPE_GAS;
			break;
		case NUCLEAR:
			fuelType = GeneratorData.FUEL_TYPE_NUCLEAR;
			break;
		default:
			fuelType = GeneratorData.FUEL_TYPE_GAS;
			break;
		}
		return fuelType;
	}
	
	public static boolean isDispatchable(Generator generator) {
		Objects.requireNonNull(generator, "generator is null");
		boolean dispatchable = false;
		switch (generator.getEnergySource()) {
		case HYDRO:
		case THERMAL:
			dispatchable = true;
			break;
		default:
			dispatchable = false;
			break;
		}
		return dispatchable;
	}
}
