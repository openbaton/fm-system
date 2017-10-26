#!/bin/bash

# Copyright (c) 2015-2016 Fraunhofer FOKUS
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

source ./gradle.properties

_version=${version}
_fmsystem_config_file=/etc/openbaton/fms.properties
_app_name=openbaton-fms

function compile {
    ./gradlew build
}

function clean {
    ./gradlew clean
}

function check_already_running {
	local is_app_running=$(ps aux | grep -v grep |  grep "$_app_name" | grep jar | wc -l )
        if [ "$is_app_running" -ne "0" ]; then
		echo "$_app_name is already running.."
		exit;
        fi
}
function start_mysql_osx {
    sudo /usr/local/mysql/support-files/mysql.server start
}

function start_mysql_linux {
    sudo service mysql start
}

function check_mysql {
	result=$(pgrep mysql | wc -l 2>/dev/null);
        if [ ${result} -eq 0 ]; then
		echo "mysql is down, or it was not be possible to check the status. ($_app_name needs mysql)"
        fi
}
function check_zabbix_plugin_up {
        result=$(ps au | grep -v grep | grep openbaton-plugin-monitoring-zabbix | wc -l);
        if [ "${result}" -eq "0" ]; then
                echo "The openbaton-plugin-monitoring-zabbix is not running. $_app_name cannot start"
		exit;
        fi
}
function usage {
    echo -e "Open Baton Fault Management System\n"
    echo -e "Usage:\n\t ./openbaton-fms.sh [compile|start|stop]"
}
function stop {
    pkill -f $_app_name-${_version}.jar
}

function start {

    if [ ! -d build/  ]
        then
            compile
    fi
    
    check_already_running
    check_mysql
#    check_zabbix_plugin_up
    if [ 0 -eq $? ]
        then
	    java -jar "build/libs/$_app_name-$_version.jar" --spring.config.location=file:${_fmsystem_config_file}
    fi
}

if [ $# -eq 0 ]
   then
        usage
        exit 1
fi


declare -a cmds=($@)
for (( i = 0; i <  ${#cmds[*]}; ++ i ))
do
    case ${cmds[$i]} in
        "sc" )
            clean
            compile
            start ;;
        "start" )
            start ;;
        "clean" )
            clean ;;
        "stop" )
            stop ;;
        "compile" )
            compile ;;
        * )
            usage;;
    esac
    if [[ $? -ne 0 ]]; 
    then
	    exit 1
    fi
done
