Agent-Based COmplex Network Framework (ACONA)
==================================================== 
The ACONA Framework is an agent-based middleware with the purpose to allow complete encapsulation functions of a complex system as well as data transfer between functions. It has put its fokus on flexibility, testability and extendability. It is being developed to 
support the design of cognitive architectures, to allow the development of multi-agent systems and to support evolutionary programming of agents.

The framework offers the following features:
 - Loose coupling between modules:
    - JSON Message-based communication
	- Remote Procedure Calls
	- Services
    - Encapsulation of functions within agents
    - Independent development of modules, which are connected through a system of services and datapoints.
   
 - Flexibility:
	- Invocation of all business logic within an agent
	- Usage of JSON data structures in the datapoints
	- Connection to external systems through drivers or directly through an agent
	- Combination of atomic building blocks into complex structures
	
 - Maintainability:
	- Developer friendly debugging possibilites, where the developer can access all agent agent functions at any time
	- Possibility to unit test all functions through blocking caller functions of agents

The communication base between modules is the popular IoT protocol MQTT. For this implementation, the Mosuitto MQTT messagebus was used together with the Paho library. Each agent 
consists of a JSON configuration string, an internal data storage and one or more agent functions. The agent functions provide a range of possibilites to program an agent: 
A function can be automatically run in a certain interval or it can be triggered either directly from another function through an RPC call or through the subscription of a datapoint 
in the internal data storage.

Repository
==================================================== 
The repository consists of the following folders
/archive: Place for old deploys
/conf: Common logback configuration files for the use with the acona framework
/data: Data used for the projects
/deploy: Compiled, running versions
/docs: Project documentation
/logs: Place for log files
/src: Source files

There are multiple projects in the /src folder: 
_obsolete: The former ACONA project that was based on Java JADE.
acona-cognitiveframework: It is the skelleton of a general cognitive architecture, where codelets can be specified for the actual agent function.


![Cognitive Architecture with ACONA](docs/Support/KORE_Cognitive_Architecture.png?raw=true "Cognitive Architecture with ACONA" | width=100)


**acona-core**: The acona-core is the framework core, which is the base for all other projects. 
acona-demo-webservice: A demo project, where ACONA implemented webservices to check the weather on some places
acona-evolution-demonstration: A demo project to demonstrate evolutionary programming with trader agents in a stock market.


<img src="docs/Support/Stock_Market_Example_V01.png" width="200">


Installation
====================================================
1. Downloaded or clone the repository from https://github.com/aconaframework/acona.git.
2. Install node.js and Ponte to get the Mosuitto MQTT Broker. Urls: https://nodejs.org/en/ and https://www.npmjs.com/package/ponte
3. Import the projects in e.g. eclipse with gradle to get the necessary libraries. For most of the projects, the settings.gradle has to be either added or adapted.

In the src/test/java, the unit tests are put. The unit tests present good examples of how to use the framework, e.g. 
in at.tuwien.ict.acona.mq.cell.core.MqCellCoreFunctionTester.chainOfSubscribersTest(), a chain of subscribing agents is demonstrated. 

To get an adapted log, you can setup your run configurations to use the logback configuration files in the /conf folder. 
Working directory: ${workspace_loc}/acona
VM arguments: -Dlogback.configurationFile=conf/logback.xml or any other file of the configuration


ACONA Documentation
====================================================
A comprehensive documentation of the ACONA framework can be found within the repository in the folder https://github.com/aconaframework/acona/blob/master/docs/Acona_Manual/. 
First, the motivation and the goals of the project are explained. Then the architecture is described very close to the actual code.
It is followed by an installation guide for eclipse and a tutorial to setup your first running ACONA application.

The ACONA framework has been described in the following scientific publications:
- Wendt, A., Wilker, S., Meisel, M., Sauter, T.: A Multi-Agent-Based Middleware for the Development of Complex Architectures, to be published in proceedings of 27th International Symposium on Industrial Electronics 2018 (ISIE), Australia, 2018
- Wendt, A., Kollmann, S., Siafara, L., Biletskiy, Y.: Usage of Cognitive Architectures in the Development of Industrial Applications. In proceedings of  the 10th International Conference on Agents and Artificial Intelligence, ICAART 2018, Portugal, ISBN: 978-989-758-275-2; pp. 94-101, 2018
- Wendt, A., Sauter, T.: Agent-Based Cognitive Architecture Framework. in proceedings of IEEE 21th Conference on Emerging Technologies & Factory Automation (ETFA), DOI: 10.1109/ETFA.2016.7733696, Berlin, Germany, 2016

The ACONA framework has been applied in projects of the following publications:
- Zucker, G., Sporr, A., Kollmann, S., Wendt, A., Chaido, L. S., Fernbach, A: A Cognitive System Architecture for Building Energy Management. in IEEE Transactions on Industrial Informatics, vol. 14, no. 6, pp. 2521-2529 June 2018, doi: 10.1109/TII.2018.2815739, 2018
- Zucker, G., Wendt, A., Siafara, L., Schaat, S.: A Cognitive Architecture for Building Automation, published in proceedings of Industrial Electronics Society, IECON 2016-42nd Annual Conference of the IEEE, pp 6919-6924, DOI: 10.1109/IECON.2016.7793798, Florence, Italy, 2016


![ACONA Logo](docs/Support/aconalogo.png?raw=true "Acona logo" | width=50)