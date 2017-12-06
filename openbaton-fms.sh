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
_config_file="/etc/openbaton/openbaton-fms.properties"
_app_name=openbaton-fms
_openbaton_base="/opt/openbaton"
_app_base="${_openbaton_base}/fm-system"
_screen_session_name="openbaton"

function checkBinary {
  if command -v $1 >/dev/null 2>&1; then
     return 0
   else
     echo >&2 "FAILED."
     return 1
   fi
}

_ex='sh -c'
if [ "$_user" != 'root' ]; then
    if checkBinary sudo; then
        _ex='sudo -E sh -c'
    elif checkBinary su; then
        _ex='su -c'
    fi
fi

function check_already_running {
	local is_app_running=$(ps aux | grep -v grep |  grep "$_app_name" | grep jar | wc -l )
        if [ "$is_app_running" -ne "0" ]; then
		echo "$_app_name is already running.."
		exit;
        fi
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
    echo -e "Usage:\n\t ./openbaton-fms.sh [compile|start|start_fg|stop]"
}

function compile {
    ./gradlew build -x test
}

function clean {
    ./gradlew clean
}

function stop {
    pkill -f $_app_name-${_version}.jar
}

function start_checks {
    check_already_running
    check_mysql
    if [ ! -d build/  ]
        then
            compile
    fi
}

function start {
    start_checks
    screen_exists=$(screen -ls | grep "\.${_screen_session_name}" | wc -l);
    if [ "${screen_exists}" -eq 0 ]; then
        screen -c screenrc -d -m -S ${_screen_session_name} -t ${_app_name} java -jar "${_app_base}/build/libs/${_app_name}-$_version.jar" --spring.config.location=file:${_config_file}
    else
        screen -S ${_screen_session_name} -X screen -t ${_app_name} java -jar "${_app_base}/build/libs/${_app_name}-$_version.jar" --spring.config.location=file:${_config_file}
    fi
}

function start_fg {
    start_checks
    java -jar "build/libs/${_app_name}-$_version.jar" --spring.config.location=file:${_config_file}
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
        "start_fg" )
            start_fg ;;
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
