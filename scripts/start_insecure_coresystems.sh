#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

time_to_sleep=10s

echo Starting Core Systems... Service initializations usually need around 20 seconds.

cd ../serviceregistry_sql/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-serviceregistry-sql-\*.jar | sort | tail -n1) -d -daemon &> insecure_sr.log &
echo Service Registry started
sleep ${time_to_sleep} #wait for the Service Registry to fully finish loading up

cd ../../authorization/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-authorization-\*.jar | sort | tail -n1) -d -daemon &> insecure_auth.log &
echo Authorization started

cd ../../gateway/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-gateway-\*.jar | sort | tail -n1) -d -daemon &> insecure_gateway.log &
echo Gateway started

cd ../../eventhandler/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-eventhandler-\*.jar | sort | tail -n1) -d -daemon &> insecure_eventhandler.log &
echo Event Handler started

cd ../../gatekeeper/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-gatekeeper-\*.jar | sort | tail -n1) -d -daemon &> insecure_gk.log &
echo Gatekeeper started

cd ../../orchestrator/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-orchestrator-\*.jar | sort | tail -n1) -d -daemon &> insecure_orch.log &
echo Orchestrator started

cd ../../translator/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-translator-\*.jar | sort | tail -n1) -d -daemon &> insecure_orch.log &
echo Translator started
