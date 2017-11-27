#!/bin/sh
FMS_SERVICE_KEY=$(grep fms.service.key /etc/openbaton/openbaton-fms.properties|cut -d'=' -f 2)

if [ -z "$FMS_SERVICE_KEY" ];then
    until curl -sSf http://nfvo:8080;do sleep 10;done

    USER=admin
    PASS=openbaton
    NFVO_IP=nfvo
    NFVO_PORT=8080
    PID=$(openbaton -pid none -u "$USER" -p "$PASS" -ip "$NFVO_IP" --nfvo-port "$NFVO_PORT" project list|grep default|awk '{print $2}')
    SERVICE_KEY=$(openbaton -pid "$PID" -u "$USER" -p "$PASS" -ip "$NFVO_IP" --nfvo-port "$NFVO_PORT" service create '{"name":"fms", "roles":["*"]}')

    export FMS_SERVICE_KEY="$SERVICE_KEY"
    sed -i "s/fms.service.key=/fms.service.key=$SERVICE_KEY/g" /etc/openbaton/openbaton-fms.properties
fi

exec java -jar /fms.jar --spring.config.location=file:/etc/openbaton/openbaton-fms.properties
