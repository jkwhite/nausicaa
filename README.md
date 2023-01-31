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

Cycles: Positive integer indicating the number of iterations this incantation will
run (only relevant if using Sequences, which will be explained later).

Weight: Optional update weight (default 1.0). A real value that will be
multiplied to rule output value. Can be used to decrease or increase the
"speed" at which the lattice changes.

Decay: Optional weight decay (default 1.0). A real value that if set will
change the weight value with each iteration, i.e. weight' = weight * decay.

Incantation: A kind of genome-like set of codons, written as syllables in
the Machine Elvish language, which is described in a separate section below.

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

## Neighborhoods

### Moore

### von Neumann

### Circular

## Automata

Automata are saved in JSON text format, and so can be manipulated via any
text editor or other program in addition to the GUI itself.

## Machine Elvish

Machine Elvish is a simple language for expressing computations. It is
structured as a sequence of syllables (aka codons) which forms an "incantation"
and evaluates over a state which is represented as a stack. Values are pushed
to or popped from the stack by each syllable, and the stack may be
manipulated in other ways as well.

Let's look at a very simple example incantation:

```
ya
```

This incantation consists of one syllable, "ya". The "ya" syllable represents
a computation where the current value of the lattice at the coordinate being
evaluated is pushed onto the stack.

## Evaluation

An incantation is evaluated for each cell in the lattice. During evaluation,
the incantation may store temporary state in a "stack" via push, pop, and
related operations. When evaluation is finished, a final value is popped from
the stack, and this becomes the new cell value. (Weight may also be applied
at this point, if it was specified in the incantation.)

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

This compound codon, slightly more complex, generates a series of circles:

```
kya0+kya0+mu2+kya1+kya1+mu2+mi2+ni
```

### Variables

Incantations may contain "variables", which are demarcated by ```{varname}```
within the incantation. For example:

```
a{var1}
```

Represents a Constant codon whose value is whatever the value of ```var1```
is set to. Variables are set at execution time and fixed throughout the run.

### Codon catalog

#### a (Constant)

Syntax: `a*Value*`

Pushes *Value* onto the stack, where *Value* is any integer or real value.
**Note that discrete automata may only use integer values.**

#### e (Avg N)

Syntax: `e`

Pushes the average of the last N values onto the stack, where N is the top
value on the stack, and not included in the average. For example, if the stack
consists of `4 2 1 3`, then N is 3 and `(4+2+1)/3` will be pushed.

#### i (Pow)

Syntax: `i`

Pushes v1^v2^ onto the stack, where v1 and v2 are popped off the stack.
**Note that for discrete automata, if v2 is less than 0 it will be replaced
by 0.**

#### ka (Bitwise Or)

Syntax: `ka`

Pushes the bitwise-or value of the top two stack values. **Note that this is
only supported for discrete automata. For real automata, the top stack value
will simply be pushed back on the stack.**

#### ke (Greater)

Syntax: `ke`

Pushes 1 or 0, depending on whether `v1>=v2`, where v1 and v2 are the top two
stack values.

#### ki (Surrounding)

Syntax: `ki`

Pushes all surrounding cells values onto the stack. The definition of
"surrounding" depends on two factors: (1) Neighborhood type (Moore, von Neumann,
or Circular), and (2) Neighborhood size.

#### ko (Bitwise Rotate Left)

Syntax: `ko`

Pushes the bitwise-left-rotated value of the top stack value rotated by the next
top stack value. **Note that this is only supported for discrete automata.
For real automata, the top stack value will simply be pushed back on the stack.**

#### ku (Bitwise Rotate Right)

Syntax: `ku`

Pushes the bitwise-right-rotated value of the top stack value rotated by the next
top stack value. **Note that this is only supported for discrete automata.
For real automata, the top stack value will simply be pushed back on the stack.**

#### ma (Equals)

Syntax: `ma`

Pushes 1 or 0, depending on whether `v1=v2`, where v1 and v2 are the top two
stack values.

#### me (Sum N)

Syntax: `me`

Pushes the sum of the top N values onto the stack, where N is the top
value on the stack, and not included in the sum. For example, if the stack
consists of `4 2 1 3`, then N is 3 and `4+2+1` will be pushed.

#### mi (Sum)

Syntax: `mi*Value*`

Pushes the sum of the top *Value* values onto the stack. *Value* is optional;
if not specified, all values on the stack are summed (i.e., the entire stack).

#### mo (Modulo)

Syntax: `mo`

Pushes `v1%v2`, where v1 and v2 are the top two stack values. If v2 is 0,
simply pushes v1.

#### mu (Multiply)

Syntax: `mu*Value*`

Pushes the product of the top *Value* values onto the stack. *Value* is optional;
if not specified, all values on the stack are multiplied (i.e., the entire stack).

#### o (Push Neighbor)

Syntax: `o*Neighbor*`

Pushes *Neighbor* onto the stack, where *Neighbor* is the index of any cell
in the set of neighboring cells. Index values wrap around, so attempts to push
indeces greater than the size of the set of neighbors are "safe".

#### u (Intersects)

Syntax: `u`

Tests if `low<=mid<=high`, where all three values are popped off the stack.
Pushes 1 (true) or 0 (false).

#### ya (Self)

Syntax: `ya`

Pushes the current cell value onto the stack.


## Example automata

### The Game of Life

Incantation:

```
ki mi a2 a3 u ki mi8 a3 ma ya ra
```

This is the classic Game of Life (Conway's Life) automata.
