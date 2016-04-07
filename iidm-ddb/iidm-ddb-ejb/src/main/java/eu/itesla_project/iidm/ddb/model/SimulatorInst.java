/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import java.io.Serializable;
import javax.persistence.*;

//ALTER TABLE `itesladdb`.`simulatorinst` DROP INDEX `version`,
//ADD UNIQUE INDEX `version` USING BTREE(`version`, `simulator`);

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(
		name = "SIMULATORINST",  
		uniqueConstraints={
			   @UniqueConstraint (columnNames={"version","simulator"})
		}
	)
public class SimulatorInst implements Serializable {

	private static final long serialVersionUID = 1L;

	public SimulatorInst() {
		super();
	}
	
	public SimulatorInst(Simulator simulator, String version) {
		super();
		this.simulator=simulator;
		this.version=version;
	}
	

	//The synthetic id of the object.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    //@Enumerated(EnumType.STRING)
    @Enumerated
    @Column(nullable=false)
    private Simulator  simulator;

	public Simulator getSimulator() {
		return simulator;
	}
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}
	
    @Column(nullable=false)
	private String version;

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "SimulatorInst [simulator=" + simulator + ", version=" + version+ "]";
	}
	
	@Override
	// This must return true for another SimulatortInst object as the same key/id.
    public boolean equals(Object other) {
        return other instanceof SimulatorInst && (simulator != null && version != null) ? 
        		simulator.equals( ( (SimulatorInst) other).getSimulator()) && version.equals( ( (SimulatorInst) other).getVersion())  : (other == this);
    }
	
	
    
}
