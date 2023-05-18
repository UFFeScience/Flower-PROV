# DfAnalyzer

## Installation

### RESTful services

DfAnalyzer project can be easily built by running the following command lines:

```bash
cd DfAnalyzer
mvn clean package
```

Then, the package file JAR is generated for DfAnalyzer project. It allows Java runtimes to efficiently deploy our tool. This JAR file is named as *DfAnalyzer-v2.jar* and it can be found in *./DfAnalyzer/target*.

## An overview of DfAnalyzer components and their documentations

### Initialization

The project DfAnalyzer at `dfanalyzer` contains all web applications and RESTful services provided by our tool. Therefore, the following components are present in this project: *Provenance Data Extractor* (PDE), *Dataflow Viewer* (DfViewer), *Query Interface* (QI), and *Query Dashboard* (QP).

We provide a compressed file of our MonetDB database (to DfAnalyzer) for a local execution of the project DfAnalyzer. Therefore, users only need to run the script `start-dfanalyzer.sh` at the path `application/dfanalyzer`. We assume the execution of these steps in an Unix-based operating system, as follows:

```bash
cd application/dfanalyzer
./start-dfanalyzer.sh
```

Then, a similar output message should be displayed in the terminal tab:

```
--------------------------------------------
Restoring MonetDB database...
--------------------------------------------
Starting database system...
property            value
hostname         localhost
dbfarm           /dfanalyzer/applications/dfanalyzer/data
status           monetdbd[3068] 1.7 (Jul2017-SP1) is serving this dbfarm
mserver          /program/monetdb/bin/mserver5
logfile          /dfanalyzer/applications/dfanalyzer/data/merovingian.log
pidfile          /dfanalyzer/applications/dfanalyzer/data/merovingian.pid
sockdir          /tmp
listenaddr       localhost
port             50000
exittimeout      60
forward          proxy
discovery        true
discoveryttl     600
control          no
passphrase       <unknown>
mapisock         /tmp/.s.monetdb.50000
controlsock      /tmp/.s.merovingian.50000
starting database 'dataflow_analyzer'... done
      name         state   health                       remarks
dataflow_analyzer  R  0s  100% 11s  mapi:monetdb://localhost:50000/dataflow_analyzer
--------------------------------------------
Starting DfA RESTful API

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.8.RELEASE)

2017-11-09 09:12:44.451  INFO 3073 --- [           main] rest.server.WebApplication               : Starting WebApplication v1.0 on mercedes with PID 3073 (/dfanalyzer/applications/dfa/DfA-1.0 started by vitor in /dfanalyzer/applications/dfanalyzer)
...
2017-11-09 09:12:55.397  INFO 3073 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2017-11-09 09:12:55.419  INFO 3073 --- [           main] o.s.c.support.DefaultLifecycleProcessor  : Starting beans in phase 0
2017-11-09 09:12:55.815  INFO 3073 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 22000 (http)
2017-11-09 09:12:55.833  INFO 3073 --- [           main] rest.server.WebApplication               : Started WebApplication in 12.433 seconds (JVM running for 13.257)
```

### Provenance Data Extractor (PDE)

Provenance Data Extractor (PDE) is a DfAnalyzer component responsible for extracting provenance and scientific data from scientific applications. With this purpose, PDE delivers a RESTful API that users can send HTTP request with POST method in order to register data extracted from their applications. These extracted data follows a dataflow abstraction, considering file and data element flow monitoring. Then, PDE stores these extracted data into a provenance database to enable online query processing.

Since our RESTful application has been initialized, users can submit HTTP requests with POST method to PDE for extracting provenance and scientific data. Then, considering the dataflow abstraction followed by DfAnalyzer, users have to introduce a message according to this abstraction. Therefore, PDE provides a set of methods to be present in the HTTP message, as follows:

![PDE RESTful services](../img/dfa-docs-pde.png)

This web page (http://localhost:22000/dfview/help) can be accessed using a browser after the initialization of DfAnalyzer.

**Example**

**Dataflow specification**

As an example of dataflow specification to be included in our provenance database, users submit the following HTTP request to PDE, considering a dataflow with one data transformation of our libMesh application (systems_of_equations_ex2):

HTTP message send to the URL `http://localhost:22000/pde/dataflow`

```bash
dataflow(systems_of_equations_ex2)

program(systems_of_equations_ex2::solver.solve(), /root/systems_of_equations_ex2)

dataset(ocreate_equation_systems, {n_dofs}, {NUMERIC})
dataset(isolve_equation_systems, 
            {dt, timesteps, nonlinear_steps, nonlinear_tolerance, nu},
            {NUMERIC, NUMERIC, NUMERIC, NUMERIC, NUMERIC})
dataset(osolve_equation_systems, 
            {timestep, time, nonlinear_steps, linear_iterations, final_linear_residual, norm_delta, converged},
            {NUMERIC, NUMERIC, NUMERIC, NUMERIC, NUMERIC, NUMERIC, TEXT})

transformation(solve_equation_systems, {ocreate_equation_systems, isolve_equation_systems}, {odeduplication}, )
```

**Task record**

After the dataflow specification, provenance and scientific data generated during the execution of scientific applications can be registered in our provenance database. In this case, PDE has to capture HTTP request in the task level (*i.e.*, execution of a data transformation).

For example, the following message should be sent in an HTTP request to the URL `http://localhost:22000/pde/task`

```bash
task(systems_of_equations,  solve_equation_systems, 1, RUNNING)

collection(ocreate_equation_systems, {{ dofs }})
collection(isolve_equation_systems, {{ timestep, time, nonlinear_steps, 
            linear_iterations, final_linear_residual, norm_delta, converged }})

dependency({create_equation_systems}, {1})
```

More details about DfAnalyzer RESTful services can be found [here](https://hpcdb.github.io/armful/dfanalyzer.html).

### Dataflow Viewer (DfViewer)

After the provenance and domain data capture, users can also analyze dataflow specifications stored in our provenance database using DfViewer. DfViewer is a feature provided by our Web application.

As a first step, users have to use a Web browser (*e.g.*, Google Chrome) with the following URL for accessing DfViewer features:

```bash
http://localhost:22000 # users can also use the hostname for external connections
```

Then, they have access to a list of dataflow specifications stored in DfAnalyzer's database.

![List of dataflow specifications](../img/dfa-webpage.png)

Since users have decided to view a specific dataflow by clicking on the button with the name *View* (*e.g.*, to analyze dataflow specification with tag *systems_of_equations_ex2*), then the following web page will be provided to them. This visualization consists of a dataset perspective view of the dataflow specification, where users can investigate the schema (*i.e.*, attributes of each dataset).

![List of dataflow specifications](../img/dfviewer-libmesh-app.png)

### Query Interface (QI)

Query Interface (QI) is a DfAnalyzer component responsible to get user input, which characterizes and specifies the parameters and properties of the desired data, and then crafts a query responsible to retrieve the necessary datasets, data transformations and data attributes from the database.

Thus, QI generates a SQL-based query according to a specification, where:

* The SELECT clause is populated according to the user-specified projections, representing the data attributes chosen by the user;
* The WHERE clause, which is the most important one from the viewpoint of
  provenance tracing, acts like a filter by selecting and limiting the query to retrieve only the data elements (from datasets) that met a set of specified criteria (conditions);
* and, finally, the FROM clause contains the datasets from where the data attributes specified in the SELECT clause and the conditions specified in the WHERE clause are part of.

Query Interface key function is called <tt>generateSqlQuery</tt>, which
requires the following input arguments that need to be informed by
the users:

* D: the dataflow to be analyzed (it includes dataflow tag and identifier)
* dsOrigins: the datasets to be used as sources for the path finding algorithm;
* dsDestinations: the datasets to be used as destinations (ends) for the path finding algorithm;
* type: the attribute mapping type, which can be either logical (based on domain-specific attributes), physical (based on the identifiers of data transformations) or hybrid;
* projections: data attributes chosen to be part of the SELECT clause;
* selections: set of conditions intended to potentially filter and limit the query results, being part of the WHERE clause;
* dsIncludes: datasets that must be present in the paths found by the path
  finding algorithm;
* dsExcludes: datasets that must not be present in the paths found by the path finding algorithm.

By calling <tt>generateSqlQuery</tt> with the desired arguments, the user is capable of generating and running a SQL code using QI.

Using the RESTful API of DfAnalyzer, users can submit HTTP requests with the POST method to run queries in DfAnalyzer's database. So, these requests have to use the URL `http://localhost:22000/query_interface/{dataflow_tag}/{dataflow_id}` and to add a message, which should contain the query specification as follows:

**Table**: query specification using QI.

Concept | Method (body of the HTTP request) | Additional information
--- | --- | ---
Mapping | mapping(type) | type = PHYSICAL, LOGICAL, HYBRID
Source datasets | source(datasetTags)
Target datasets | target(datasetTags)
Includes | include(datasetTags) | Datasets to be included in the fragment of dataflow path.
Excludes | exclude(datasetTags) | Datasets to be excluded from the fragment of dataflow path.
Projections | projection(attributes) | *attributes* argument defines which attributes will be obtained after query processing, e.g., attributes = {table1.att1;table2.att2}
Selections | selection(conditions) | *conditions* is used to filter only relevant data elements, *e.g.*, table1.att1 > 100

**Example**

Considering our libMesh application (more details below) instrumented to extract provenance and scientific data using DfAnalyzer, users might like to investigate the data element flow from the input dataset *osolve_equation_systems* to the output dataset *oextract_data*, when the velocity of the fluid at the axis x is greater than 0.01. More specifically, they want to know in which times and distances (*oextract_data.x*, which is equivalent to the point in coordinate x) this situation occurs. The figure below presents the dataflow fragment analyzed by this query.

![Dataflow representation of the application systems_of_equations_ex2](../img/dfviewer-zoom.png)

Based on this dataflow analysis, an HTTP request has to be submitted to our RESTful API with the following URL and message (*i.e.*, HTTP body).

URL:
```
http://localhost:22000/query_interface/systems_of_equations_ex2/2
``` 

Message:

```
mapping(physical)
source(osolve_equation_systems)
target(oextract_data)
projection(osolve_equation_systems.time; oextract_data.x;oextract_data.u)
selection(oextract_data.u > 0.01)
```

As a result, our RESTful API returns a CSV-format file with the selected content after the query processing.

### Query Dashboard (QD)

Besides the query processing capabilities provided by QI, users can develop their queries using our graphical interface, as follows:

![Query specification using our graphical interface](../img/query-dashboard.png)