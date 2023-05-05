#!/bin/bash
loadgenpid=$(docker inspect -f '{{.State.Pid}}' loadGenerator)
echo $loadgenpid
for i in {1..300}; do { printf '%(%H:%M:%S)T'; echo -n " "; docker stats loadGenerator --no-stream --format "{{.CPUPerc}}"; } >> logs_docker; done & 
for i in {1..300}; do { mpstat 2 1 | awk '{print $1, $3""}' | head -n4 | tail -n1; } >> logs_mpstat; done &
for i in {1..300}; do { mpstat -P 0 2 1 | awk '{print $1, $3""}' | head -n4 | tail -n1; } >> logs_mpstat_0; done &
for i in {1..300}; do { mpstat -P 1 2 1 | awk '{print $1, $3""}' | head -n4 | tail -n1; } >> logs_mpstat_1; done &
for i in {1..300}; do { mpstat -P 2 2 1 | awk '{print $1, $3""}' | head -n4 | tail -n1; } >> logs_mpstat_2; done &
for i in {1..300}; do { mpstat -P 3 2 1 | awk '{print $1, $3""}' | head -n4 | tail -n1; } >> logs_mpstat_3; done &
for i in {1..300}; do { pidstat -p $loadgenpid 2 1 | awk '{print $1, $4""}' | head -n4 | tail -n1; } >> logs_pidstat; done &
wait
sed -i 's/\./,/g' logs_docker logs_mpstat logs_mpstat_0 logs_mpstat_1 logs_mpstat_2 logs_mpstat_3 logs_pidstat
sed -i 's/%//g' logs_docker
echo 'Monitoring done' 