#!/bin/bash
name=$1
while true;
 do
  echo q | htop | aha --line-fix | html2text | grep "running" >> "$name-runningthr".txt;
#echo q | htop | aha --line-fix | html2text | grep "running" | awk '{print $6}' >> "$name-runningthr".txt;
  sleep 1; 
 done
