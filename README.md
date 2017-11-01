  <img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png" width="250"/>
  
  Copyright © 2015-2016 [Open Baton](http://openbaton.org). 
  Licensed under [Apache v2 License](http://www.apache.org/licenses/LICENSE-2.0).

[![Build Status](https://travis-ci.org/openbaton/fm-system.svg?branch=master)](https://travis-ci.org/openbaton/fm-system)

# Open Baton Fault Management System
The Open Baton Fault Management System (`openbaton-fms`) is an external component of the NFVO Open Baton. It manages the alarms coming from the VIM and executes actions through the NFVO.  

The `openbaton-fms` is implemented as a [Spring Boot application][spring-boot]. 
It runs as an external component and communicate with the NFVO via Open Baton's SDK and RabbitMQ.  

Before starting this component you need to ensure that the [technical requirements](#technical-requirements) are met and proceed with the [installation guide](#how-to-install-open-baton-fm-system).

# Technical Requirements

* Preconfigured Open Baton environment (NFVO, VNFMs, VIM drivers)
* Running Zabbix server
* Preconfigured and running Zabbix plugin (see the [doc of Zabbix plugin][zabbix-plugin-doc])
* Mysql server installed and running

# How to install Open Baton FM System

There are two options available for the installation of the `openbaton-fms`. Installation based on the Debian package or on the source code (which is suggested for development). 

## Installation via Debian package

When using the Debian package you need to add the apt-repository of Open Baton to your local environment with the following command if not yet done:
 
```bash
wget -O - http://get.openbaton.org/keys/openbaton.public.key | apt-key add -
echo "deb http://get.openbaton.org/repos/openbaton/<dist>/release <dist> main" >> /etc/apt/sources.list
```
Replace \<dist\> with **trusty**, **xenial** or **jessie** depending on the distribution you are using.  
Once you added the repo to your environment you should update the list of repos by executing:

```bash
apt-get update
```

Now you can install the `openbaton-fms` by executing:

```bash
apt-get install openbaton-fms
```

**Note**: During the installation you will be prompted for entering the IP address of the host of the Zabbix Plugin, make sure this IP can be reached by the Zabbix Server host.

After the installation, the `openbaton-fms` will be already configured and running.

## Installation from the source code

The latest stable version of the Open Baton FM System can be cloned from this [repository][fms-repo] by executing the following command:

```bash
git clone https://github.com/openbaton/openbaton-fms.git
```

Once this is done, go inside the cloned folder and install the project as done below:

```bash
./gradlew installDist
```

The installation from the source code requires manual configuration before running the `openbaton-fms`, which is explained in the following section.

## Manual configuration of the Open Baton FM System

This chapter describes what needs to be done before starting the Open Baton FM System.

## Configuration file

The configuration file must be copied to `/etc/openbaton/openbaton-fms.properties` by executing the following command from inside the repository folder:

```bash
cp src/main/resources/application.properties /etc/openbaton/openbaton-fms.properties
```

In the following sections, we will refer to this configuration file as `openbaton-fms.properties`. 

## Create the database

In order to create the database be sure you have installed [mysql server][mysql-installation-guide] as already mentioned in the requirements section. 
You need root access to mysql-server in order to create a new database called faultmanagement. Once you access into mysql, execute the following operation: 

```bash
create database faultmanagement;
```

Once the database has been created, you should create a user which will be used by the FM system to access and store data on the database. If you decide to use the **root** user you can skip this step, but you need to modify the `openbaton-fms.properties` file accordingly as defined in the next section. 
By default username and password are set with the following values in the `openbaton-fms.properties` properties file (see next section if you plan to use a different user and password): 

* username=fmsuser
* password=changeme

Grant the access to the database "faultmanagement", to the user, running the following command:

```bash
GRANT ALL PRIVILEGES ON faultmanagement.* TO fmsuser@'%' IDENTIFIED BY 'changeme';
```

## Modify openbaton-fms.properties file in order to use different credentials for the database 

In order to use different credentials, you need to modify the following properties: 

```properties
# DB properties
spring.datasource.username=fmsuser
spring.datasource.password=changeme
```

In case your database is running remotely, you can specify a different host, instead of localhost, in the following property (be careful to have port 3306 open and accessible from remote): 

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/faultmanagement
```

## Additional configuration for the NFVO

You should update this file in order to make it work with your NFVO instance. Change the Open Baton related properties section: 

```properties
################################################
####### Open Baton Related properties ##########
################################################
nfvo.ip=localhost
nfvo.port=8080
nfvo-usr=admin
nfvo-pwd=openbaton
nfvo.project.name=default
nfvo.ssl.enabled=false
```
## Configure the Service key

The `openbaton-fms` authenticates to the NFVO through a service key which has to be set in the properties. You need to obtain the service key from the dashboard of the NFVO, 
in particular you can set the service key with the following instructions:
* Go to Admin->Services
* Click on "Enable a new Service"
* Input the name "fms"
* Click on Role
* Select "*" in the Project checkbox
* Click Save
* Open the downloaded file
* Copy the service key in the `openbaton-fms.properties`:

```properties
fms.service.key=
```

# Starting the Open Baton FM System

How to start the Open Baton FM System depends of the way you installed this component.

### Debian packages

If you installed the Open Baton FM System with the Debian packages you can start it with the following command:

```bash
openbaton-fms start
```

For stopping it you can just type:

```bash
openbaton-fms stop
```

### Source code

If you are using the source code you can start the Open Baton FM System easily with the following command from inside the repository folder:

```bash
cd build/install/openbaton-fms
./bin/openbaton-fms start
```

For stopping you can use:
```bash
./bin/openbaton-fms stop
```

**Note** Since the Open Baton FM System subscribes to specific events towards the NFVO, you should take care about that the NFVO is already running when starting the `openbaton-fms`.

# How to use Open Baton FM System

Open Baton FMS is a rule-driven tool. The rules define when to generate an alarm and how to react. The rule for generating the alarm is called fault management policy (see the next section). 
The rule for defining how to react upon alarms is a Drools Rule. Once such rules are in place, Open Baton FM follows the following workflow.      

![Fault management system use case][fault-management-system-use-case]

The actions are listed below:

| ACTION              | DESCRIPTION     | 
| ------------------- | --------------  | 
| Heal   |  The VNFM executes the scripts in the Heal lifecycle event (in the VNFD). The message contains the cause of the fault, which can be used in the scripts. 
| Switch to stanby VNFC (Stateless)   |  If the VDU requires redoundancy active-passive, there will be a component VNFC* in standby mode. This action consists in: activate the VNFC*, route all signalling and data flow(s) for VNFC to VNFC*, deactivate VNFC

## Write a fault management policy for triggering the HEAL action

The fault management policy needs to be present in the VNFD, in particular in the VDU. This is an example of fault management policy:

```json
"fault_management_policy":[
    {
      "name":"web server not available",
      "isVNFAlarm": true,
      "criteria":[
      {
        "parameter_ref":"net.tcp.listen[80]",
        "function":"last()",
        "vnfc_selector":"at_least_one",
        "comparison_operator":"=",
        "threshold":"0"
      }
      ],
      "period":5,
      "severity":"CRITICAL"
    }
]
```

The parameter `isVNFAlarm=true` tells the `openbaton-fms` that the alarm is of type VNF, and at default it will execute the HEAL action.
Description of the fault management policy:  

| Property              | Description     
| ------------------- | --------------  
| name   |  The name of the fault management policy.
| isVNFAlarm   |  if the alarm is of type VNF
| criteria | The criteria defines a threshold on a monitoring paramenter. When the threshold is crossed an alarm is fired
|period | The criteria is checked every "period" seconds
|severity | severity of the alarm

Description of the criteria:  

| Property              | Description     
| ------------------- | --------------  
| parameter_ref | Reference to a monitoring parameter in the VDU. (see below how to define monitoring parameters)
| function | The function to apply to the parameter. ( last(0) means the last value available of the parameter). Since currently only Zabbix is supported, look at the [Zabbix documentation][zabbix-functions] for the all available functions. 
|vnfc_selector | select if the criteria is met when all VNFC components cross the thresold (all) or at least one (at_least_one)
| comparison_operator | comparison operator for the threshold
|threshold | value of the threshold to compare against the parameter_ref value

In order to refer a monitoring parameter with the property **parameter_ref**, it needs to be present in the vdu:
 
```json
"monitoring_parameter":[
   "agent.ping",

   "net.tcp.listen[5001]",

   "system.cpu.load[all,avg5]",

   "vfs.file.regmatch[/var/log/app.log,Exception]"
]
```
You can specify every parameter available for the [Zabbix Agent][zabbix-agent-items].

## How the HEAL method works

The `openbaton-fms` as soon as it gets an alarm from the VIM, it checks if the alarm is referred to a VNF ("isVNFAlarm": true) and it sends the Heal VNF message to the NFVO which forwards it to the respective VNFM.
The VNFM will then execute, in the failed VNFC, the scripts in the HEAL lifecycle event.
Here an example of the heal script you can use:

```bash
#!/bin/bash

case "$cause" in

("serviceDown") 
	echo "Apache is down, let's try to restart it..."
	service apache2 restart
	if [ $? -ne 0 ]; then
	    echo "ERROR: the Apache service is not started"
	    exit 1
    	fi
	echo "The Apache service is running again!"
	;;
*) echo "The cause $cause is unknown"
	exit 2
	;;
esac
```

The variable $cause is specified in the Drools rule. In our case is "serviceDown" and we try to restart the Apache server.

## Drools Rules

The Open Baton FM is a rule-based system. Such rules are specified in Drools language and processed by the Drools engine in the Open Baton FM.
An example rule is the following:
```drools
rule "Save a VNFAlarm"
    when
        vnfAlarm : VNFAlarm()
    then
    VNFAlarm alarm = vnfAlarmRepository.save(vnfAlarm);
    logger.debug("Saved VnfAlarm: "+alarm);
end
```

This rule saves a VNFAlarm in the database.
The following rule executes the HEAL action once a VNFAlarm is received.

```drools
rule "Got a critical VNF Alarm and execute the HEAL action"

    when
       vnfAlarm : VNFAlarm(  alarmState == AlarmState.FIRED, perceivedSeverity == PerceivedSeverity.CRITICAL)
    then

    //Get the vnfr
    VirtualNetworkFunctionRecord vnfr = nfvoRequestorWrapper.getVirtualNetworkFunctionRecord(vnfAlarm.getVnfrId());

    //Get the vnfc failed (assuming only one vnfc is failed)
    VNFCInstance vnfcInstance = nsrManager.getVNFCInstanceFromVnfr(vnfr,vnfAlarm.getVnfcIds().iterator().next());

    logger.info("(VNF LAYER) A CRITICAL alarm is received by the vnfc: "+vnfcInstance.getHostname());

    //Get the vdu of the failed VNFC
    VirtualDeploymentUnit vdu = nfvoRequestorWrapper.getVDU(vnfr,vnfcInstance.getId());

    logger.info("Heal fired!");
    highAvailabilityManager.executeHeal("serviceDown",vnfr.getParent_ns_id(),vnfr.getId(),vdu.getId(),vnfcInstance.getId());

    //Insert a new recovery action

    RecoveryAction recoveryAction= new RecoveryAction(RecoveryActionType.HEAL,vnfr.getEndpoint(),"");
    recoveryAction.setStatus(RecoveryActionStatus.IN_PROGRESS);
    insert(recoveryAction);
end
```


## How the Switch to Standby works

The Switch to Standby action can be performed by the Open Baton FM once a VNFC in standby is present in the VNF. It consists in switch the service from a VNFC to the VNFC in stanby automatically.
In order to have a VNFC in standby, such information must be included in the VNFD, in particular in the VDU, as the following:

```
"high_availability":{
	"resiliencyLevel":"ACTIVE_STANDBY_STATELESS",
	"redundancyScheme":"1:N"
}
```
This information will be processed by the Open Baton FM which will create a VNFC instance in standby.
Then in a Drools rule this action can be called as following:
```
highAvailabilityManager.switchToRedundantVNFC(failedVnfcInstance,vnfr,vdu);
```

# Issue tracker

Issues and bug reports should be posted to the GitHub Issue Tracker of this project

# What is Open Baton?

OpenBaton is an open source project providing a comprehensive implementation of the ETSI Management and Orchestration (MANO) specification.

Open Baton is a ETSI NFV MANO compliant framework. Open Baton was part of the OpenSDNCore (www.opensdncore.org) project started almost three years ago by Fraunhofer FOKUS with the objective of providing a compliant implementation of the ETSI NFV specification. 

Open Baton is easily extensible. It integrates with OpenStack, and provides a plugin mechanism for supporting additional VIM types. It supports Network Service management either using a generic VNFM or interoperating with VNF-specific VNFM. 
It uses different mechanisms (REST or PUB/SUB) for interoperating with the VNFMs. It integrates with additional components for the runtime management of a Network Service. 
For instance, it provides autoscaling and fault management based on monitoring information coming from the the monitoring system available at the NFVI level.

# Source Code and documentation

The Source Code of the other Open Baton projects can be found [here][openbaton-github] and the documentation can be found [here][openbaton-doc] .

# News and Website

Check the [Open Baton Website][openbaton]
Follow us on Twitter @[openbaton][openbaton-twitter].

# Licensing and distribution
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Contribution Policy
You can contribute to the Open Baton community through bug-reports, bug-fixes, new
code or new documentation. For contributing to the Open Baton community, drop a
post to the [openbaton-mail][Open Baton Public Mailing List] providing full information about your
contribution and its value. In your contributions, you must comply with the
following guidelines

* You must specify the specific contents of your contribution either through a
  detailed bug description, through a pull-request or through a patch.
* You must specify the licensing restrictions of the code you contribute.
* For newly created code to be incorporated in the Open Baton code-base, you must
  accept Open Baton to own the code copyright, so that its open source nature is
  guaranteed.
* You must justify appropriately the need and value of your contribution. The
  Open Baton project has no obligations in relation to accepting contributions
  from third parties.
* The Open Baton project leaders have the right of asking for further
  explanations, tests or validations of any code contributed to the community
  before it being incorporated into the Open Baton code-base. You must be ready to
  addressing all these kind of concerns before having your code approved.

# Support
The Open Baton project provides community support through the Open Baton Public Mailing List and through StackOverflow using the tags openbaton.

[spring-boot]:http://projects.spring.io/spring-boot/
[fms-repo]:https://github.com/openbaton/openbaton-fms
[openbaton]: http://openbaton.org
[openbaton-doc]: http://openbaton.org/documentation
[openbaton-github]: http://github.org/openbaton
[openbaton-logo]: https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png
[openbaton-mail]: mailto:users@openbaton.org
[openbaton-twitter]: https://twitter.com/openbaton

[zabbix-plugin-doc]:https://github.com/openbaton/docs/blob/develop/docs/zabbix-plugin.md
[create-db]:README.md#create-the-database
[fault-management-system-use-case]:img/fms-use-case.png
[zabbix-functions]:https://www.zabbix.com/documentation/2.2/manual/appendix/triggers/functions
[zabbix-agent-items]:https://www.zabbix.com/documentation/2.2/manual/config/items/itemtypes/zabbix_agent
[NFV MANO]:http://www.etsi.org/deliver/etsi_gs/NFV-MAN/001_099/001/01.01.01_60/gs_nfv-man001v010101p.pdf
[etsi-draft-Or-VNFM]:https://docbox.etsi.org/isg/nfv/open/Drafts/IFA007_Or-Vnfm_ref_point_Spec/
[mysql-installation-guide]:http://dev.mysql.com/doc/refman/5.7/en/linux-installation.html
