# Template results - under development

----------------


#Benchmark Concurrent Vacuum

Benchmark the effect of the concurrent CRUD operations and Vacuum

**Test method:**

* pre-populate storage with sample records
* concurrently write/delete/read records for period of time
* kick of a parallel job that will perform vacuum every 1.0 seconds
* measure latency trend for all operations


**99.9% Write Latency:** 82

**99.9% Delete Latency:** 105

**99.9% Read Latency:** 1

**99.9% Clean Latency:** 2216

**Latency Trend (ms):**

Iteration|Write Average|Write Min|Write Max|Delete Average|Delete Min|Delete Max|Read Average|Read Min|Read Max|Clean Average|Clean Min|Clean Max
---|---|---|---|---|---|---|---|---|---|---|---|---
2021-05-04 19:31:00.000|0.58|0|2958|0.55|0|1841|0.01|0|32|1,312.67|0|1841
2021-05-04 19:32:00.000|0.4|0|835|0.61|0|732|0.01|0|7|1,117.33|0|732




----------------


## Benchmark Delete & Insert Rate

Measure impact of continues cleanup on delete/ insert performance

**Test method:**

* Pre-populate storage with sample records
* Alternate write/ delete operations, maintaining 1000 records in the DB
* Measure latency trend for each operation


**Latency Histogram:**

Latency (milliseconds)|Write Count|Delete Count
---|---|---
0|66|97
1|934|903


----------------


## Benchmark Concurrent CRUD operations

Benchmark performance during concurrent write/delete/read operations

**Test method:**

* pre-populate storage with sample records
* concurrently write/delete/read records for period of time
* measure latency trend for all operations


**99.9% Write Latency:** 39

**99.9% Delete Latency:** 130

**99.9% Read Latency:** 1

**Latency Trend (ms):**

Iteration|Write Average|Write Min|Write Max|Delete Average|Delete Min|Delete Max|Read Average|Read Min|Read Max
---|---|---|---|---|---|---|---|---|---
2021-05-04 19:33:00.000|0.25|0|2036|0.82|0|2634|0.01|0|34
2021-05-04 19:34:00.000|1.1|0|2434|0.19|0|632|0.01|0|8




----------------


## Benchmark Query & Insert Rate

Measure nominal reading & insert performance

**Test method:**

* Alternate read/write operations, inserting latest, reading latest records.
* Measure latency trend for each operation


**Latency Histogram:**

Latency (milliseconds)|Write Count|Read Count
---|---|---
0|88|1000
1|909|0
2|3|0


----------------


## Benchmark Maintenance Performance

Measure nominal performance of Vacuum operation

**Test method:**

* pre-populate storage with sample records
* Alternate write/ delete operations, maintaining 1000 records in the DB
* measure storage consumption before and after continues to write/delete operations
* perform database cleanup with Vacuum. Measure Vacuum processing time


**Storage consumption of pre-populated 1000 records:** 0.12288 MB

**Storage consumption of 1000 records after continues write/ deletes:** 0.135168 MB

**Storage cleanup took:** 1118 milliseconds

**Storage consumption after Vacuum:** 0.118784 MB


Process finished with exit code 0