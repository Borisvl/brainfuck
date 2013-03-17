Brainfuck interpreters
=============================

This is a collection of various brainfuck interpreters written in Java. They use different
methods and technologies and their execution speed varies. The FastASMBrainfuckInterpreter is
the fastest Java brainfuck interpreter I know of (and twice as fast as [Bff4](http://mazonka.com/brainf/)).

Benchmark
-----------------------------

The runtimes of the different interpreters on the mandelbrot.bf example:

| Interpreter        | Time           |
| ------------- |:-------------:|
| Simple      | 75088 ms  |
| Adv      | 20108 ms       |
| JIT | 3092 ms       |
| ASM | 2650 ms      |
| FastASM | 2039 ms       |
