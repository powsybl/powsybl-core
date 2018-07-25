# How to use and write an itools command

powsybl-core, via the itools script mechanism, provides a common way to interact with PowSyBl using the command line.  
Among predefined commands, exposing some of the framework's functionalities: convert-network, run-script, loadflow.  
powsybl-core itools module is designed and provides the necessary APIs to extend the set of available commands with new ones.  
  
In this tutorial we are going to see how to use and extend itools, the powsybl command line tool.  
In the following sections `$POWSYBL` is powsybl's root installation folder.  

## Table of Contents
 - How to use itools
 - Extending itools for your needs

## How to use itools
Executing *itools* without parameters will show you the help, with the available commands list: 
```
$> cd $POWSYBL/bin
$> ./itools
usage: itools [OPTIONS] COMMAND [ARGS]
```
### Available options are:

| Option | Description |
| ------ | ----------- |
| --config-name <CONFIG_NAME> | Override configuration file name| 
| --parallel | Run command in parallel mode 

### Currently available commands are:

| Theme | Command | Description |
| ------ | ------ | ----------- |
| Application file system | afs | Application File System command line tool |
| Computation | action-simulator | Action simulator |
| Computation | loadflow | Run loadflow |
| Computation | loadflow-validation | Validate load-flow results of a network |
| Computation | run-impact-analysis | Run impact analysis |
| Computation | security-analysis| Run security analysis |
| Data conversion | convert-network| Convert a network from one format to another |
| MPI statistics | export-tasks-statistics | Export tasks statistics to CSV |
| Script | run-script | Run a script (only Groovy is supported, so far) |

Commands in the list are classified in *themes*, to help identifying their purpose.

###  Using a Command 
Following is an example of how to use the `loadflow` command, and run a loadflow on a network.  
To show the command help, with its specific parameters and descriptions, enter: 

```
$> cd $POWSYBL/bin
$> ./itools loadflow --help
usage: itools [OPTIONS] loadflow --case-file <FILE> [--help] [--output-case-file
       <FILE>] [--output-case-format <CASEFORMAT>] [--output-file <FILE>]
       [--output-format <FORMAT>] [--parameters-file <FILE>] [--skip-postproc]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --case-file <FILE>                  the case path
    --help                              display the help and quit
    --output-case-file <FILE>           modified network base name
    --output-case-format <CASEFORMAT>   modified network output format [AMPL,
                                        XIIDM]
    --output-file <FILE>                loadflow results output path
    --output-format <FORMAT>            loadflow results output format [CSV,
                                        JSON]
    --parameters-file <FILE>            loadflow parameters as JSON file
    --skip-postproc                     skip network importer post processors
                                        (when configured)
```
                                        
In order to run the `loadflow` itools command enter:
```
$> cd $POWSYBL/bin
$> ./itools loadflow --case-file networkfileName
```
`NetworkfileName` is the path of the input network file and is a required argument (it must be specified)
(Note that the loadflow implementation to use is defined in powsybl's configuration file whose default location is `$HOME/.itools/config.xml`, ref. powsybl-core [configuration guide]("http://"))

## Extending itools for your needs
itools is designed to be easily extended with new commands, to provide users with new command line functionalities.  
In order to create a new itools command:
1. Create a new maven project and add all the dependencies to the pom.xml file.
2. Write an implementation of `com.powsybl.tools.Command` interface   
3. Write an implementation of `com.powsybl.tools.Tools` interface. 
4. Compile your project and add the jar to your powsybl installation.
  
Maven dependencies required for implementing a new itools command are the following:  

```
<dependencies>
    <dependency>
        <groupId>com.google.auto.service</groupId>
        <artifactId>auto-service</artifactId>
        <version>1.0-rc2</version>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>20.0</version>
    </dependency>
    <dependency>
        <groupId>commons-cli</groupId>
        <artifactId>commons-cli</artifactId>
        <version>1.3.1</version>
    </dependency>
    <dependency>
        <groupId>joda-time</groupId>
        <artifactId>joda-time</artifactId>
        <version>2.9.7</version>
    </dependency>
    <dependency>
        <groupId>${project.groupId}</groupId>
        <artifactId>powsybl-computation</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

In your project you also need to add the other dependencies required by your command business logic implementation
(e.g. `powsybl-iidm-converter-api` if you need to import an IIDM network).

```
public interface Command {

    String getName();

    String getTheme();

    String getDescription();

    Options getOptions();

    String getUsageFooter();

}
```

The class implementing the `Command` interface declares your command, defining name, description and theme (it is used to group the commands; already available values are: *application file system, computation, data conversion, MBI statistics*, but you can also define new themes).  
In this class you also define your command options, if they are required and/or they need an argument (see `org.apache.commons.cli.Options` and `org.apache.commons.cli.Option` classes, an example of usage is provided in the Example section of this tutorial).

```
public interface Tool {

    Command getCommand();

    void run(CommandLine line, ToolRunningContext context) throws Exception;

}  
```

The class implementing the `Tools` interface, besides returning the declaration of your command (the `getCommand` method), is the class in charge of running it (the `run` method).  
You have to declare this class as a service implementation with `@Autoservice` annotation, in order to have your new command automatically added to the list of available itools commands and be able to run it.  
The `line` parameter, input to the `run` method, gives you access to the input options provided by the user via command line (see `org.apache.commons.cli.CommandLine` class, an example of usage  is provided in the Example section of this tutorial).  
The `context` parameter, input to the `run` method, provides you some context objects, e.g. `getOutputStream()`, allowing you to print on the itools command output stream, or `getShortTimeExecutionComputationManager()`, for using the itools command computation manager (see `com.powsybl.tools.ToolRunningContext` class).


Run the following command to create your project jar:

```
$> mvn install
```

The generated jar will be located under the target folder of your project.  
Copy generated jar to $POWSYBL/share/java/ folder (you might need to copy in this directory other dependencies jars, specific to the new command).

### Example: a new command to display how many lines are there in a network

```
@AutoService(Tool.class)
public class CountNetworkLinesTool implements Tool {

    private static final String CASE_FILE = "case-file";
    
    @Override
    public Command getCommand() {
        return new Command() {
            @Override
           
            public String getName() {
                return "count-network-lines";
            }

            @Override
            public String getTheme() {
                return "Network";
            }

            @Override
            public String getDescription() {
                return "Count network lines";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(CASE_FILE)
                        .desc("the case path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());             
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE));
        ImportConfig importConfig = ImportConfig.load();
        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Importers.loadNetwork(caseFile, context.getShortTimeExecutionComputationManager(), importConfig, null);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }
        int lineCount = network.getLineCount();
        context.getOutputStream().println(" Network contains '"+lineCount+"' lines");

       }
```

In the `getCommand` method we declare our new command: name, theme, description and options. The only option defined in the class, `case-file`, allows the user to specify the network file, is 
required (see `.required()`) and has an argument, the input case file (see `.hasArg().argName("FILE")`).  
In the `run` method we use the `line` parameter to read the path to the input network file, passed as command line argument (see `line.getOptionValue(CASE_FILE)`),and we use the `context` parameter to get the computation manager needed to load the network (see `context.getShortTimeExecutionComputationManager()`) and the output stream where to print the command results (see `context.getOutputStream()`).
  
If you add the jar containing this sample command implementation to your powsybl installation, the new `count-network-lines` command will be listed in the itools commands list.  
Following is the help of the new `count-network-lines` command:

```
$> ./itools count-network-lines --help

usage: itools [OPTIONS] count-network-lines --case-file <FILE> [--help]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --case-file <FILE>   the case path
    --help               display the help and quit
```

In order to run the new command enter:

```
$> ./itools count-network-lines --case-file networkfileName
```

where `NetworkfileName` is a the path of the input network file.
