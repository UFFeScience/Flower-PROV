#!/bin/bash




# e $dt $num_msg  $num_elements

function e {


        dt=$1 # sec
        num_msg=$2
        num_elements_per_msg=$(($3 / $2))

        output_file=./dt${dt}_num_msg${num_msg}_num_el${num_elements}.data

        echo "Sending " $3 " data elements distributed in " $2 " with " $num_elements_per_msg "element(s) per message "

        df='{"tag":"uq_rtm","transformations":[{"programs":[{"name":"SparseGrid","path":".\/bin\/sparse_grid_cc_dataset"}],"tag":"sparse_grid_construction","sets":[{"tag":"sparsegridinput","attributes":[{"name":"vmid","type":"NUMERIC"},{"name":"dimension","type":"NUMERIC"},{"name":"level","type":"NUMERIC"}],"type":"INPUT"},{"tag":"sparsegridoutput","attributes":[{"name":"vmid","type":"NUMERIC"},{"name":"dimension","type":"NUMERIC"},{"name":"level","type":"NUMERIC"},{"name":"region","type":"FILE"},{"name":"weights","type":"FILE"},{"name":"points","type":"FILE"}],"type":"OUTPUT"}]}]}'
        curl -H "Content-type: application/json" -X POST  -d "$df"    http://localhost:22000/pde/dataflow/json #> $output_file


        elements=''
        for j in `seq 1 $num_elements_per_msg`;
        do
                elements=$elements,'"1;8;'$j';r;w;p"'

        done


        for i in `seq 1 $num_msg`;
        do
                content='{"subid":"'${i}'","workspace":"\/home\/luciano\/Desktop\/pg","dataflow":"uq_rtm","sets":[{"elements":["1;8;1"],"tag":"sparsegridinput"}, {"elements":['$elements'],"tag":"sparsegridoutput"}],"dependency":{},"resource":"local","id":"'${i}'","transformation":"sparse_grid_construction","status":"FINISHED"}'
                curl -H "Content-type: application/json" -X POST -d "${content}"    http://localhost:22000/pde/task/json #>> $output_file
                printf "\n\n\n" #>> $output_file
                sleep $dt
        done
        # curl -X POST http://localhost:22002/pde/perf #>> $output_file
}







# dt=0.010 # sec
# num_msg=1000
# num_elements=1000

# e $dt $num_msg  $num_elements


# dt=0.100 # sec
# num_msg=1000
# num_elements=1000

# e $dt $num_msg  $num_elements


dt=0.100 # sec
num_msg=50
num_elements=$num_msg

e $dt $num_msg  $num_elements

# sleep 5
# touch ../PerformanceMonitor/finish_performance.tkn

# dt=0.010 # sec
# num_msg=100
# num_elements=1000

# e $dt $num_msg  $num_elements


# dt=0.100 # sec
# num_msg=100
# num_elements=1000

# e $dt $num_msg  $num_elements


# dt=0.001 # sec
# num_msg=100
# num_elements=1000

# e $dt $num_msg  $num_elements


# dt=0.010 # sec
# num_msg=10
# num_elements=1000

# e $dt $num_msg  $num_elements


# dt=0.100 # sec
# num_msg=10
# num_elements=1000

# e $dt $num_msg  $num_elements



######################


# dt=0.001 # sec
# num_msg=10
# num_elements=1000

# e $dt $num_msg  $num_elements
