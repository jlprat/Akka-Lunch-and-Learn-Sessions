# Akka-Lunch-and-Learn-Sessions

This repository contains the companion code examples of the series of Lunch'n'Learn sessions about Akka, Akka Streams, and Akka HTTP.

## Akka for the Impatient

The first session name is Akka for the Impatient and all corresponding associated code lives under the [intro](https://github.com/jlprat/Akka-Lunch-and-Learn-Sessions/tree/master/src/main/scala/io.github.jlprat.akka.lnl/intro "First Session Code Samples") package.

### Structure

All examples are duplicated (as long as possible) using both Actor APIs: Akka Classic and Akka Typed. Samples corresponding to Akka Classic are under the [classic](https://github.com/jlprat/Akka-Lunch-and-Learn-Sessions/tree/master/src/main/scala/io.github.jlprat.akka.lnl/intro/classic "Akka Classic") package. Samples corresponding to Akka Typed are under the [typed](https://github.com/jlprat/Akka-Lunch-and-Learn-Sessions/tree/master/src/main/scala/io.github.jlprat.akka.lnl/intro/typed "Akka Typed") package.

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
