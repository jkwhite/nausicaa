# NausiCAä

NausiCAä is an application and library for creating visualizations
of computation on discrete lattices in one, two, or three dimensions.
Lattices are composed of "cells", where each cell has a coordinate
in the lattice. A lattice may be composed of either non-negative integer
values, or non-negative real values.

In summary, NausiCAä creates visualizations of computations that are similar to
cellular automata, with some notable differences. But the name stuck,
so here we are.

## Rules

Syntax:

```
Cycles[/Weight[/Decay]]:Incantation
```

Cycles: Positive integer indicating the number of iterations this rule will
run (only relevant if using Sequences, which will be explained later).

Weight: Optional update weight (default 1.0). A real value that will be
multiplied to rule output value. Can be used to decrease or increase the
"speed" at which the lattice changes.

Decay: Optional weight decay (default 1.0). A real value that if set will
change the weight value with each iteration, i.e. weight' = weight * decay.

Incantation: A kind of genome-like set of syllables expressed in Machine Elvish,
which is described in a separate section.

## Sequences

A Sequence is a list of rules that run in serial. Each rule runs for its
cycle time, and when the end of the list is reached the sequence repeats
with the first rule.

Syntax:

```
Cycles1[/Weight1[/Decay1]]:Incantation1
Cycles2[/Weight2[/Decay2]]:Incantation2
...
```

## Automata

Automata are saved in JSON text format, and so can be manipulated via any
text editor or other program.

## Machine Elvish

Machine Elvish is a simple language for expressing computations. It is
structured as a sequence of syllables which forms an "incantation" and
evaluates over a state which is represented as a stack. Values are pushed
to or popped from the stack by each syllable, and the stack may be
manipulated in other ways as well.

Let's look at a very simple example incantation:

```
ya
```

This incantation consists of one syllable, "ya". The "ya" syllable represents
a computation where the current value of the lattice at the coordinate being
evaluated is pushed onto the stack.

### Compound codons

Codons may also be "fused" together to create compound codons which will be
mutated as an atomic unit using the "+" connector. For example:

```
ki ya+a1+mi2 mu
```

The compound codon in this incantation adds 1 to the current cell. When the
incantation is mutated, for example by swapping the order of two codons, it
will be treated as a single codon. So a swap mutation might result in:

```
ya+a1+mi2 ki mu
```

### Codon catalog

#### ya

Syntax: ```ya```

Pushes the current cell value onto the stack.
