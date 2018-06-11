Agent-Based COmplex Network Framework (ACONA)
==================================================== 
The ACONA Framework is an agent-based middleware with the purpose to allow complete encapsulation functions of a complex system as well as data transfer between functions. It has put its fokus on flexibility, testability and extendability. It is being developed to 
support the design of cognitive architectures, to allow the development of multi-agent systems and to support evolutionary programming of agents.

The framework offers the following features:
 - Loose coupling between modules:
    - JSON Message-based communication
	- Remote Procedure Calls
    - Encapsulation of functions within agents
    - Independent development of modules, which are connected through a system of services and datapoints.
   
 - Flexibility:
	- Invocation of all business logic within an agent
	- Usage of JSON data structures in the datapoints
	- Connection to external systems through drivers or directly through an agent
	- Combination of atomic building blocks into complex structures
	
 - Maintainability:
	- Developer friendly debugging possibilites, where the developer can access all agent cell functions at any time
	- Possibility to unit test all functions through blocking caller functions of agents

The agent base is Java Jade (http://jade.tilab.com). Each agent, which is called a cell, consists of a JSON configuration string, an internal data storage and one or more cell functions.
The cell functions provide a range of possibilites to program an agent: A function can be automatically run in a certain interval or it can be triggered either directly from another function through a 
JSON-RPC call or through a datapoint in the internal data storage.

Installation
====================================================
Once you have downloaded the repository, there are multiple projects in the /src folder: The acona-cell is the framework. The project acona-evolution-demonstration is a multi agent system created to demonstrate 
evolutionary programming for a stock market trading example. 

Use gradle to download all necessary libraries. For most of the projects, the settings.gradle has to be either added or adapted. For the acona-evolution-demonstration, a custom project with a user console can be downloaded here: https://github.com/aconaframework/commonutils 

In the src/test/java, the unit tests are put. The unit tests present good examples of how to use the framework, e.g. in massOfSubscribersTest(), a chain of subscribing agents is demonstrated.