package com.powsybl.iidm.network;

import java.util.Arrays;

public class TestClass {

	public static void main(String[] args) {

		TopologyLevel topologyLevel = TopologyLevel.fromTopologyKind(TopologyKind.NODE_BREAKER);
		
		System.out.println(
		
	    TopologyLevel.fromTopologyKind(TopologyKind.BUS_BREAKER).name()) ;
		
	}
	
}
