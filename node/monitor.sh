#!/bin/bash
for i in {1..11}; do { printf '%(%H:%M:%S)T'; echo -n " "; docker stats loadGenerator --no-stream --format "{{.CPUPerc}}"; } >> logs; done & for i in {1..10}; do { mpstat -P 0 2 1 | awk '{print $1, $3"%"}' | head -n4 | tail -n1; } >> logs2; done &