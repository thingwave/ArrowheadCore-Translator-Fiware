#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

time_to_sleep=10s

echo Starting Core Systems... Service initializations usually need around 20 seconds.

cd ../serviceregistry_sql/target
nohup java -jar arrowhead-serviceregistry-sql-4.0.jar -d -daemon -tls &> secure_sr.log &
echo Service Registry started
sleep ${time_to_sleep} #wait for the Service Registry to fully finish loading up

cd ../../authorization/target
nohup java -jar arrowhead-authorization-4.0.jar -d -daemon -tls &> secure_auth.log &
echo Authorization started

cd ../../gateway/target
nohup java -jar arrowhead-gateway-4.0.jar -d -daemon -tls &> secure_gateway.log &
echo Gateway started

cd ../../eventhandler/target
nohup java -jar arrowhead-eventhandler-4.0.jar -d -daemon -tls &> secure_eventhandler.log &
echo Event Handler started

cd ../../gatekeeper/target
nohup java -jar arrowhead-gatekeeper-4.0.jar -d -daemon -tls &> secure_gk.log &
echo Gatekeeper started

cd ../../orchestrator/target
nohup java -jar arrowhead-orchestrator-4.0.jar -d -daemon -tls &> secure_orch.log &
echo Orchestrator started
