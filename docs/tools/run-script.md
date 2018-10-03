# iTools run-script command

itools `run-script` command allows you to run scripts (only Groovy is supported, so far).

Here below is described how to use the `run-script` command.    

*Note:* In the following sections [\<POWSYBL_HOME\>](../configuration/directoryList.md) represents powsybl's root installation directory.  

## Running run-script command 
To show the `run-script` help, with its specific parameters and descriptions, enter: 
```shell
$> cd <POWSYBL_HOME>/bin
$> ./itools run-script --help
usage: itools [OPTIONS] run-script --file <FILE> [--help]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --file <FILE>   the script file
    --help          display the help and quit
```

In order to run the `run-script` command, you have to provide as input the required argument: 
- `file`: Groovy script to run  

## Run groovy script
In the following example, we will see how you can implement groovy script that import a network from a case file, increase the load's active power, print all `Load` values (current and increase of 1%) and execute a loadflow.

The Groovy script can be found [here](../samples/groovyScript/increaseLoadActivePowerAndRunLoadFlow.groovy)

You have to: 

1. Write a `Groovy` script that implement the busines logic.

    ```groovy
    
    package com.powsybl.samples.groovyScript
    
    import com.powsybl.iidm.network.Load
    import com.powsybl.iidm.network.Network
    import com.powsybl.iidm.import_.Importers
    import com.powsybl.loadflow.LoadFlowFactory
    import com.powsybl.loadflow.LoadFlowParameters
    import com.powsybl.commons.config.ComponentDefaultConfig
    
    def caseFile = ""
    
    switch (args.length) {
        case 0:
           System.err.println "You need to specify network case file. "
    	   System.exit(1)
        case 1:
    	   caseFile = this.args[0]
    	   break
        default:
    	   println "Too many arguments! You have to specify only one. "
    	   System.exit(1)
    }
    
    println " Imported newtwork file :" +caseFile
    
    def network = Importers.loadNetwork(caseFile)
    println " Imported Network's Data: Network Id: " + network.getId()  + "  Generators: " + network.getGeneratorCount()+ "  Lines : " + network.getLineCount() +" Loads: " + network.getLoadCount() 
    
    println "\nDump LOADS "
    println " id | p | p+1%"
    
    // change the network
    def  percent = 1.01
    
    network.getLoads().each { load ->
        if ( load.getTerminal != null) {
            def currentValue = load.getTerminal().getP()
    	    load.getTerminal().setP(currentValue * percent)
    	    def newVal = load.getTerminal().getP()
    	    println " "+load.getId() + "| " +currentValue + "| " + newVal
    	}
    }
    
    // execute a LF
    println "\nExecute a LF"
    
    def defaultConfig = ComponentDefaultConfig.load()
    loadFlowFactory = defaultConfig.newFactoryImpl(LoadFlowFactory.class)
    loadFlowParameters = new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES)
    loadFlow = loadFlowFactory.create(network, computationManager, 0)
    result = loadFlow.run(network.getStateManager().getWorkingStateId(),loadFlowParameters).join()
    
    println " LF results - converge:" + result.ok + " ; metrics: " +result.getMetrics()
    
    ```
    ComponenteDefaultConfig binds configuration loaded from [powsybl configuration file](../configuration/configuration.md). It provide you access to loadFlow implemantation.
    
    The network variable provides access to the imported network (see com.powsybl.iidm.network.Network class), you can work on it using the IIDM API. In the sample code, we use it to get the list of all network loads (network.getLoads()).
    
    The computationManager parameter provides you access to the computation platform. It can be used to distribute the computation (e.g. if you need to run a loadflow on the imported network, or some other kind of heavy computation).

2. Configure the `loadFlow` 

    The configuration for the loadflow is defined in [powsybl configuration file](../configuration/configuration.md).
    
    The loadflow implementation to use is read from the `LoadFlowFactory` tag of the `componentDefaultConfig` section. 
    
    Here is an example of a minimal configuration for a `mock` loadflow (i.e. an implementation that does nothing on the network). If you want to execute a true computation, you should configure a 'real' loadflow implementation 
    (e.g. RTE's [Hades2LF](http://www.rte.itesla-pst.org/), is currently free to use for academic/non commercial purposes).
    
    ### YAML version
    ```yaml
    componentDefaultConfig:
        LoadFlowFactory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
    ```
    ### XML version
    ```xml
    <componentDefaultConfig>
        <LoadFlowFactory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</LoadFlowFactory>
    </componentDefaultConfig>
    ```

3.  Runnig `run-script` command

    Note that in the following sections [\<POWSYBL_SAMPLE\>](../configuration/directoryList.md) represents powsybl's sample  directory. 
    
    ```shell
    $> cd <POWSYBL_HOME>/bin
    $>  ./itools run-script --file <POWSYBL_SAMPLE>/groovyScript/increaseLoadActivePowerAndRunLoadFlow.groovy networkfileName
    ```
    
    `networkfileName` is the path of the input network file, it's mandatory.

The outcome of the script will show:

* Imported newtwork file: networkfileName imported

* Network Id: (networkId)  Generators: (numGenerators)  Lines: (numLines)  Loads: (numLoads)
 
* Dump LOADS:  list of id | p | p+1%
  
* LF results - converge:true ; metrics: [:]


In this sample, the LF results - converge, will be always true because we are using a mockloadFlow. 
In a real loadflow implementation, the results could be different.