#!/bin/bash
#More sleep time between these commands might be needed on slower devices like a Raspberry Pi (because of the database accesses)
echo Shutting down Core Systems
pkill -f orchestrator
pkill -f gatekeeper
pkill -f authorization
pkill -f eventhandler
pkill -f gateway
sleep 5s
pkill -f serviceregistry
pkill -f translator

if pgrep -f serviceregistry
then
  kill -KILL $(ps aux | grep 'orchestrator' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'gatekeeper' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'authorization' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'eventhandler' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'gateway' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'serviceregistry' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'translator' | awk '{print $2}')
  echo Core systems forcefully killed
else
  echo Core systems killed
fi