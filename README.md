  <img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png" width="250"/>
  
  Copyright © 2015-2016 [Open Baton](http://openbaton.org). 
  Licensed under [Apache v2 License](http://www.apache.org/licenses/LICENSE-2.0).

[![Build Status](https://travis-ci.org/openbaton/fm-system.svg?branch=master)](https://travis-ci.org/openbaton/fm-system)

# Open Baton FM
The Open Baton FM project is a module of the NFVO Openbaton. It manages the alarms coming from the VIM and executes actions through the NFVO.

# Technical Requirements

The technical requirements are:  

- Zabbix plugin running (see the [doc of Zabbix plugin][zabbix-plugin-doc])
- Mysql server installed and running
- Open Baton 3.2.x running
- Generic VNFM 3.2.x running

# How to install Open Baton FM

Once the prerequisites are met, you need to execute the following steps.

## Create the database

In order to create the database be sure you have installed [mysql server][mysql-installation-guide] as already mentioned in the requirements section. 
You need root access to mysql-server in order to create a new database called faultmanagement. Once you access the database, execute the following operation: 

```bash
mysql create database faultmanagement;
```

Once the database has been created, you should create a user which will be used by the FM system to access and store data on the database. If you decide to use the `root` user you can skip this step, but you need to modify the fms.properties file accordingly as defined in next section. 
By default username and password are set with the following values in the fms.properties properties file (see next section if you plan to use a different user and passord): 

* username=fmsuser
* password=changeme

```bash
GRANT ALL PRIVILEGES ON faultmanagement.* TO fmsuser@'%' IDENTIFIED BY 'changeme';
```

## Modify fms.properties file in order to use different credentials for the database 

In the folder etc of this project, there is a file called fms.properties containing all the default properties values used by the FM system. 

In order to use different credentials, you need to modify the following DB properties: 

```bash
# DB properties
spring.datasource.username=fmsuser
spring.datasource.password=changeme
```

In case your DB is running remotely, you can specifcy a different host instead of localhost in the following property (be careful to have port 3306 open and accessible from remote): 

```bash
spring.datasource.url=jdbc:mysql://localhost:3306/faultmanagement
```

## Additional configurion options 

As already mentioned in the previous section, in the folder etc of this project, there is a file called fms.properties containing all the default properties values used by the FM system.
You should update this file in order to make it work with your NFVO instance. Change the Open Baton related properties section: 

```bash
################################################
####### Open Baton Related properties ##########
################################################
nfvo.ip=localhost
nfvo.port=8080
nfvo-usr=admin
nfvo-pwd=openbaton
```


## Checkout the source code of the project and compile it

you can clone this repository and compile it using Gradle and launch it:  

```bash  
git clone https://github.com/openbaton/fm-system.git
```

The configuration file is etc/fms.properties, you have to copy it in the Open Baton etc folder ( /etc/openbaton ). You can do it typing the following command 

```bash  
cd fm-system
cp etc/fms.properties /etc/openbaton/fms.properties
```

Now, you can finally compile and start the FM System. 

```bash  
./fm-system.sh compile start
```


# How to use Open Baton FM

![Fault management system use case][fault-management-system-use-case]

The actions are listed below:

| ACTION              | DESCRIPTION     | 
| ------------------- | --------------  | 
| Heal   |  The VNFM executes the scripts in the Heal lifecycle event (in the VNFD). The message contains the cause of the fault, which can be used in the scripts. 
| Switch to stanby VNFC (Stateless)   |  If the VDU requires redoundancy active-passive, there will be a component VNFC* in standby mode. This action consists in: activate the VNFC*, route all signalling and data flow(s) for VNFC to VNFC*, deactivate VNFC
| Switch to stanby VNFC (Stateful)    |  To investigate. Refer on ETSI GS NFV-REL 001 v1.1.1 (2015-01) Chapter 11.2.1 

## Write a fault management policy

The fault management policy need to be present in the VNFD, in particular in the VDU. This is an example of fault management policy:

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
Description of the fault management policy:  

| Property              | Derscription     
| ------------------- | --------------  
| name   |  The name of the fault management policy.
| isVNFAlarm   |  if the alarm is of type VNF
| criteria | The criteria defines a threshold on a monitoring paramenter. When the threshold is crossed an alarm is fired
|period | The criteria is checked every "period" seconds
|severity | severity of the alarm

Description of the criteria:  

| Property              | Derscription     
| ------------------- | --------------  
| parameter_ref | Reference to a monitoring parameter in the VDU. (see below how to define monitoring parameters)
| function | The function to apply to the parameter. ( last(0) means the last value available of the parameter). Since currently only Zabbix is supported, look at the [Zabbix documentation][zabbix-functions] for the all available funcitons. 
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

The Heal VNF operation is a method of the VNF lifecycle management interface described in the ETSI [NFV MANO] specification. Here is reported the description and the notes about this method:

```
Description: this operation is used to request appropriate correction actions in reaction to a failure.
Notes: This assumes operational behaviour for healing actions by VNFM has been described in the VNFD. An example might be switching between active and standby mode.
```

In the ETSI draft "NFV-IFA007v040" at [this][etsi-draft-Or-VNFM] page, the Heal VNF message is defined as:

```
vnfInstanceId : Identifies the VNF instance requiring a healing action.
cause : Indicates the reason why a healing procedure is required.
```

The fault management system as soon as gets an alarm from the VIM, 
it checks if the alarm is referred to a VNF and it sends the Heal VNF message to the NFVO which forward it to the respective VNFM.
The VNFM executes in the failed VNFC the scripts in the HEAL lifecycle event.
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
```
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

```
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

The Switch to Standby action can be performed by the Open Baton FM once a VNFC in stanby is present in the VNF. It consists in switch the service from a VNFC to the VNFC in stanby automatically.
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

Open Baton is easily extensible. It integrates with OpenStack, and provides a plugin mechanism for supporting additional VIM types. It supports Network Service management either using a generic VNFM or interoperating with VNF-specific VNFM. It uses different mechanisms (REST or PUB/SUB) for interoperating with the VNFMs. It integrates with additional components for the runtime management of a Network Service. For instance, it provides autoscaling and fault management based on monitoring information coming from the the monitoring system available at the NFVI level.

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
