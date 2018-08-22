# Extending itools for your needs
itools is designed to be easily extended with new commands, that would be added to the set of available commands, providing users with new command line functionalities.  
In order to create a new itools command:
1. Create a new maven project and add all the required dependencies.
2. Implement the `com.powsybl.tools.Tools` interface. 
3. Compile your project and add the jar to your powsybl installation.

In the following sections we will see how, following these steps, you can implement a new itools command to display how many lines are there in a network.  
A sample maven project implementing this command can be found [here](../samples/count-lines-tool).  

## Maven dependencies
  
After creating the Maven project, you need to add the necessary dependencies to your pom.xml file.  
Maven dependencies required for implementing a new itools command are the following:  

```
<dependency>
    <groupId>com.google.auto.service</groupId>
    <artifactId>auto-service</artifactId>
    <version>1.0-rc2</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-tools</artifactId>
    <version>${project.version}</version>
</dependency>
```

In your project you also need to add the other dependencies required by your command business logic implementation, e.g. for implementing the itools command displaying the number of lines of a network, you would have to add the following dependency to IIDM converter API, needed to import IIDM networks:

```
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-converter-api</artifactId>
    <version>${powsybl.version}</version>
</dependency>
```

## Implement the Tools interface

For creating a new itool command, you need to implement the `com.powsybl.tools.Tools` interface.  
Following is a sample class, where you will put the code for displaying the number of lines of a IIDM network.  

```
@AutoService(Tool.class)
public class CountNetworkLinesTool implements Tool {

    @Override
    public Command getCommand() {
        return null;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
    }

}
```

You have to declare the class as a service implementation, using `@Autoservice` annotation. This will allow you to have the new command automatically added to the list of available itools commands, and to be able to run it (see last section).  
The methods of the `Tools` interface to override in your class are: 
 - `getCommand` method, that returns the declaration of your command
 - `run` method, in charge of running your command 

```
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
```

The `getCommand` method returns a class implementing the `com.powsybl.tools.Command` interface. This interface declares your command, defining name, description and theme (the theme is used to group the commands, see [itools description](README.md); already available values, in powsybl-core, are: *application file system, computation, data conversion, MBI statistics*, but you can also define new themes). Our samples class defines name (`count-network-lines`), description (`Count network lines`) and theme (`Network`, a new theme) of the new command for counting network lines.  
The `Command` class also defines your command options (input parameters), if they are required and/or they need an argument (see `org.apache.commons.cli.Options` and `org.apache.commons.cli.Option` classes). The only option defined in our sample class, `case-file`, allows the user to specify the network file to analyze. The option is required (`.required()`) and has an argument, the input case file (`.hasArg().argName("FILE")`).  

```
    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE));
        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Importers.loadNetwork(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), null);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }
        int lineCount = network.getLineCount();
        context.getOutputStream().println("Network contains '" + lineCount + "' lines");
    }
```

The `run` method is in charge of running your command, implementing your business logic.  
The `line` parameter gives you access to the input options provided by the user via command line (see `org.apache.commons.cli.CommandLine` class). In our example we use it to read the path to the input network file, passed as command line argument (`line.getOptionValue(CASE_FILE)`)   
The `context` parameter provides you some context objects, e.g. `getOutputStream()`, allowing you to print on the itools command output stream, `getShortTimeExecutionComputationManager()`, for using the itools command computation manager, or `.getFileSystem()`, for accessing the file system where the command is run (see `com.powsybl.tools.ToolRunningContext` class). In the sample code we use this parameter to get the input file (`context.getFileSystem().getPath()`), the computation manager needed to load the network (`context.getShortTimeExecutionComputationManager()`) and the output stream to print log messages and command results (`context.getOutputStream().println()`).  
The rest of the code in our sample class loads the input network, using the importing API (`Importers.loadNetwork()`), and gets the number of lines, using the IIDM API (`network.getLineCount()`). 

## Update your installation with the new command

Run the following command to create your project jar:

```
$> cd <PROJECT_HOME>
$> mvn install
```

The generated jar will be located under the target folder of your project.  
Copy the generated jar to `<POWSYBL_HOME>/share/java/` folder (you might need to copy in this directory other dependencies jars, specific to your new command).  
Adding the jar containing our sample command implementation to your powsybl installation, the new `count-network-lines` command will be listed in the itools commands list.  
Following is the help of the the new `count-network-lines` sample command:

```
$> cd <POWSYBL_HOME>/bin
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
$> cd <POWSYBL_HOME>/bin
$> ./itools count-network-lines --case-file networkfileName
```

where `NetworkfileName` is a the path of the input network file.