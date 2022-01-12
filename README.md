# WORD COUNTER

## Summary
The `word-counter` app is composed basically by 3 main components:
- An **Akka-Streams** process which is in charge of consuming all jsons from the standard input, 
which come from the binary `blackbox.amd64`. The stream basically consumes all the jsons from the 
provided binary and applies some kind of transformations in order to be able to:
  - extract the lines
  - discard malformed jsons
  - parse them into Json objects
  - group them by type
  - count the words from the data field
  - save the current stream status, thanks to the `wordCounterActor` 
  - and finally persist the current status of the actor into a postgres instance, in order to 
  recover the whole stream status, in case of error.
- An Akka-HTTP web server, in charge of exposing a `GET /counter` endpoint to consume the current status (words counts)
- An Akka actor responsible for keeping the words counts update, using an immutable Map internally, and also responsible 
for persisting the internal.

As the akka stream graph has a source that expects data from the standard input, we just need to pipe the output from the 
binary to the java process input in order to consume the jsons generated by the binary you provided.

## Instructions

In order to launch the application, you need to follow the next steps from your terminal:
1. Clone the repo
2. Build the postgres docker image
```shell
$ docker build -t postgres-counter -f dockerfiles/postgres/Dockerfile . 
```
3. Build the application from the app folder you cloned into:
```shell
$ cd word-counter
$ sbt compile assembly

/path-to-blackbox-binary/blackbox.amd64 | java -Xms512m -Xmx1024m -cp target/scala-2.13/akka-streams-word-count-assembly-0.1.jar WordCount
```

Also, if you want, you can place the blackbox binary into the project folder and then run `docker-compose`:
```shell
$ cd word-counter
$ cp /path-to-blackbox-binary/blackbox.amd64 .
$ cd dockerfiles
$ docker-compose up --build #you can use also '-d' if you dont want to see any log trace
```
A sample of the log traces:
```[2022-01-12 12:14:55,999][INFO ][com.zaxxer.hikari.HikariDataSource][] slick-postgres.db - Starting...
[2022-01-12 12:14:56,018][INFO ][com.zaxxer.hikari.HikariDataSource][] slick-postgres.db - Start completed.
[2022-01-12 12:14:56,973][INFO ][WordCount$][] Window duration: 10 seconds
[2022-01-12 12:14:57,013][INFO ][CounterUtils$][] Server online at http://0:0:0:0:0:0:0:0:8000/
[2022-01-12 12:14:57,026][INFO ][actor.WordCounterActor][] no previous status. Starting the actor with empty status...
[INFO] [01/12/2022 12:14:57.060] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(baz,amet,1641986094)
[INFO] [01/12/2022 12:15:00.655] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(foo,lorem,1641986100)
[INFO] [01/12/2022 12:15:00.660] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(baz,ipsum,1641986100)
[INFO] [01/12/2022 12:15:00.663] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(foo,dolor,1641986100)
[INFO] [01/12/2022 12:15:00.666] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(foo,lorem,1641986100)
[INFO] [01/12/2022 12:15:03.655] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(foo,ipsum,1641986103)
[INFO] [01/12/2022 12:15:03.658] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,lorem,1641986103)
[INFO] [01/12/2022 12:15:03.661] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,dolor,1641986103)
[INFO] [01/12/2022 12:15:03.663] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,sit,1641986103)
[INFO] [01/12/2022 12:15:03.667] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,lorem,1641986103)
[INFO] [01/12/2022 12:15:03.670] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,lorem,1641986103)
[INFO] [01/12/2022 12:15:03.673] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(baz,dolor,1641986103)
[INFO] [01/12/2022 12:15:03.674] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,ipsum,1641986103)
[INFO] [01/12/2022 12:15:05.654] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,sit,1641986105)
[INFO] [01/12/2022 12:15:05.656] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,sit,1641986105)
[INFO] [01/12/2022 12:15:05.658] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(baz,dolor,1641986105)
[INFO] [01/12/2022 12:15:05.659] [WordCount-akka.actor.default-dispatcher-6] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(foo,dolor,1641986105)
[2022-01-12 12:15:07,117][INFO ][actor.WordCounterActor][] data persisted
[INFO] [01/12/2022 12:15:09.654] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,sit,1641986109)
[INFO] [01/12/2022 12:15:09.656] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(baz,lorem,1641986109)
[INFO] [01/12/2022 12:15:09.657] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(foo,dolor,1641986109)
[INFO] [01/12/2022 12:15:09.658] [WordCount-akka.actor.default-dispatcher-5] [akka.stream.Log(akka://WordCount/system/Materializers/StreamSupervisor-0)] [parser-log] Element: Event(bar,dolor,1641986109)
[2022-01-12 12:15:10,675][INFO ][actor.WordCounterActor][] data persisted
[2022-01-12 12:15:13,676][INFO ][actor.WordCounterActor][] data persisted
[2022-01-12 12:15:17,117][INFO ][actor.WordCounterActor][] data persisted
...
```

and also a sample of the response from `curl -XGET /localhost:8000/counter`:
```json
{"data":{"bar":141,"baz":99,"foo":114}}
```