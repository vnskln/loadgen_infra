#!/bin/bash
loadgenpid=$(docker inspect -f '{{.State.Pid}}' loadGenerator)
echo $loadgenpid
for i in {1..700}; do { printf '%(%H:%M:%S)T'; echo -n " "; docker stats loadGenerator --no-stream --format "{{.MemUsage}}" | awk '{print $1}'; } >> logs_mem_docker; done & 
for i in {1..700}; do { pidstat -r -p $loadgenpid 2 1 | awk '{print $1, $6, $7, $8""}' | head -n4 | tail -n1; } >> logs_mem_pidstat; done &
wait
sed -i 's/\./,/g' logs_mem_docker logs_mem_pidstat
sed -i 's/MiB//g' logs_mem_docker
echo 'Monitoring done' 