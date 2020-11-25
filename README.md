# Akka-Lunch-and-Learn-Sessions

This repository contains the companion code examples of the series of Lunch'n'Learn sessions about Akka, Akka Streams, and Akka HTTP.

## Structure

All examples are duplicated (as close as possible) using both Actor APIs: Akka Classic and Akka Typed. Samples corresponding to Akka Classic are under the `classic` package. Samples corresponding to Akka Typed are under the `typed` package.

## Akka for the Impatient

The first session's name is Akka for the Impatient and all corresponding associated code lives under the [intro](https://github.com/jlprat/Akka-Lunch-and-Learn-Sessions/tree/master/src/main/scala/io.github.jlprat.akka.lnl/intro "First Session Code Samples") package. You can find the matching slides [here](https://jlprat.github.io/Akka-Lunch-and-Learn-Sessions/akka.html).

### How to run the code

First, start sbt with `sbt`

#### Run Akka Classic

``` shell
sbt> runMain io.github.jlprat.akka.lnl.intro.classic.ClassicMain
```

#### Run Akka Typed

``` shell
sbt> runMain io.github.jlprat.akka.lnl.intro.typed.TypedMain
```

## Supervising Akka

The second session's name is Supervising Akka and all corresponding associated code lives under the [supervision](https://github.com/jlprat/Akka-Lunch-and-Learn-Sessions/tree/master/src/main/scala/io.github.jlprat.akka.lnl/supervision "Second Session Code Samples") package. You can find the matching slides [here](https://jlprat.github.io/Akka-Lunch-and-Learn-Sessions/akka-supervision.html).

## Stashing and Persisting Akka

The third session's name is Stashing and Persisting Akka and all corresponding associated code lives under the [stash](https://github.com/jlprat/Akka-Lunch-and-Learn-Sessions/tree/master/src/main/scala/io.github.jlprat.akka.lnl/stash "Third Session Code Samples - Stash") and the [persistence](https://github.com/jlprat/Akka-Lunch-and-Learn-Sessions/tree/master/src/main/scala/io.github.jlprat.akka.lnl/persistence "Third Session Code Samples - Persistence") packages. You can find the matching slides [here](https://jlprat.github.io/Akka-Lunch-and-Learn-Sessions/akka-stash-persistence.html).

### How to run the code

First, start sbt with `sbt`

#### Run Akka Typed

``` shell
sbt> runMain io.github.jlprat.akka.lnl.stash.typed.ChunkedUnstash
```
