#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )

cd "$parent_path"

time_to_sleep=10s

echo Starting Core Systems - wait 1 minute
#More sleep time between core systems might be needed on slower devices like a Raspberry Pi

cd ../serviceregistry_sql/target
nohup java -jar arrowhead-serviceregistry-sql-4.0.jar -d -daemon -tls &> secure_sr.log &
echo Service Registry started
sleep ${time_to_sleep}

cd ../../authorization/target
nohup java -jar arrowhead-authorization-4.0.jar -d -daemon -tls &> secure_auth.log &
echo Authorization started
sleep ${time_to_sleep}

cd ../../gateway/target
nohup java -jar arrowhead-gateway-4.0.jar -d -daemon -tls &> secure_gateway.log &
echo Gateway started
sleep ${time_to_sleep}

cd ../../eventhandler/target
nohup java -jar arrowhead-eventhandler-4.0.jar -d -daemon -tls &> secure_eventhandler.log &
echo Event Handler started
sleep ${time_to_sleep}

cd ../../gatekeeper/target
nohup java -jar arrowhead-gatekeeper-4.0.jar -d -daemon -tls &> secure_gk.log &
echo Gatekeeper started
sleep ${time_to_sleep}

cd ../../orchestrator/target
nohup java -jar arrowhead-orchestrator-4.0.jar -d -daemon -tls &> secure_orch.log &
echo Orchestrator started
sleep ${time_to_sleep}