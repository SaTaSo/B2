#!/bin/bash

work="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

results_dir=$work/results
graphs_dir=$work/graphs
logs_dir=$work/logs
bin_dir=$work/bin

mkdir $logs_dir &> /dev/null
mkdir $results_dir &> /dev/null
mkdir $graphs_dir &> /dev/null

tag="test"
max_heap="4g"
young_heap="1g"

function runSet {
    day=`date +"%d:%m:%y"`
    time=`date +"%H:%M:%S"`
    size=$1
    reads=$2
    oper=$3
    run $bin_dir/g1-java g1
}

function run {
    gc_script=$1
    gc=$2

#    prefix=$tag-$max_heap-$young_heap-$size-$reads-$oper-$day-$time
    prefix=$size-$reads-$day-$time
    sufix=$gc
    echo "Starting $sufix"
    echo "start atop"
    install /dev/null "$size-$reads-atop.txt"
    atop 1 -w ./resultsatop/"$size-$reads-atop.txt" &
    echo "starting recording running threads"
    sh ./logthr.sh $size-$reads &

    $gc_script -jar "Microbench.jar" $size $reads $oper &> $logs_dir/microbench.log

    ps aux | grep "logthr.sh" | grep -v grep | awk '{print $2}' | xargs kill -9
    pkill atop
    ps aux | grep "atop" | grep -v grep | cut -d" " -f3 | xargs kill -9
    atop -r ./resultsatop/"$size-$reads-atop.txt" | grep " java" >> ./resultsatop/"$size-$reads-cpu".txt
    atop -Dr ./resultsatop/"$size-$reads-atop.txt" | grep " java" >> ./resultsatop/"$size-$reads-IO".txt
    atop -Nr ./resultsatop/"$size-$reads-atop.txt" | grep " java" >> ./resultsatop/"$size-$reads-net".txt
    sleep 60; 

    # Backup Logs "$GC-$max_heap-$min-heap-gc.log"
    echo "Saving logs to $results_dir/$prefix/$sufix ..."
    mkdir -p $results_dir/$prefix/$sufix
    cp $logs_dir/microbench.log $results_dir/$prefix/$sufix/$reads.log
    cp /tmp/jvm.log $results_dir/$prefix/$sufix
    echo "Finished $prefix/$sufix"
    echo

#    sh ./logload.sh $bench &

}
for iter in 1 2 3 4 5
do
for OPS in 100M
do
    for PERCENT in 25 50 75 100 
    do
        for SIZE in 1M 2M 3M 
        do
            runSet $SIZE $PERCENT $OPS
        done
    done
done
done



