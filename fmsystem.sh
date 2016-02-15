#!/bin/bash

source ./gradle.properties

_version=${version}
_fmsystem_config_file=/etc/openbaton/fms.properties

function compile {
    ./gradlew build -x test 
}
function tests {
    ./gradlew test
}

function clean {
    ./gradlew clean
}

function check_already_running {
        result=$(ps au | grep -v grep | grep fault-management-system | wc -l);
        if [ "${result}" -ne "0" ]; then
                echo "The fault-management-system is already running.."
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
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
	result=$(pgrep mysql | wc -l);
        if [ ${result} -eq 0 ]; then
                read -p "mysql is down, would you like to start it ([y]/n):" yn
		case $yn in
			[Yy]* ) start_mysql_linux ; break;;
			[Nn]* ) echo "you can't proceed withuot having mysql up and running" 
				exit;;
			* ) start_mysql_linux;;
		esac
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
	mysqladmin status
	result=$?
        if [ "${result}" -eq "0" ]; then
                echo "mysql service running..."
        else
                read -p "mysql is down, would you like to start it ([y]/n):" yn
                case $yn in
                        [Yy]* ) start_mysql_osx ; break;;
                        [Nn]* ) exit;;
                        * ) start_mysql_osx;;
                esac
        fi
    fi
}
function check_zabbix_plugin_up {
        result=$(ps au | grep -v grep | grep zabbix-plugin | wc -l);
        if [ "${result}" -eq "0" ]; then
                echo "The zabbix-plugin is not running. The fault management system cannot start"
		exit;
        fi
}

function start {

    if [ ! -d build/  ]
        then
            compile
    fi
    
    check_already_running
    check_mysql
    check_zabbix_plugin_up
    if [ 0 -eq $? ]
        then
	    java -jar "build/libs/fault-management-system-$_version.jar" --spring.config.location=file:${_fmsystem_config_file}
    fi
}

declare -a cmds=($@)
for (( i = 0; i <  ${#cmds[*]}; ++ i ))
do
    case ${cmds[$i]} in
        "clean" )
            clean ;;
        "sc" )
            clean
            compile
            start ;;
        "start" )
            start ;;
        "update" )
            update ;;
        "stop" )
            stop ;;
        "restart" )
            restart ;;
        "compile" )
            compile ;;
        "kill" )
            kill ;;
        "test" )
            tests ;;
        * )
            usage
            end ;;
    esac
    if [[ $? -ne 0 ]]; 
    then
	    exit 1
    fi
done
