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

#### ba (Bitwise Left Rotate)

Syntax: `ba`

Pushes the bitwise-left-rotated value of the top stack value rotated by the next
top stack value. **Note that this is only supported for discrete automata.
For continuous automata, the top stack value will simply be pushed back on the stack.**

#### be (Greater Than)

Syntax: `be`

Pushes `1` if `v1>v2`, else `0`. where v1 and v2 are the top two stack values.

#### bo (Negate)

Syntax: `bo`

Pushes the negation of the top stack value. For example, if the stack is
[5 3 1], the new stack will be [5 3 -1].

#### bu (Less Than)

Syntax: `bu`

Pushes `1` if `v1<v2`, else `0`. where v1 and v2 are the top two stack values.

#### chi (Min)

Syntax: `chi*Value*`

Pushes the minimum value of the top *Value* values onto the stack. *Value*
is optional; if not specified, all values on the stack are considered
(i.e., the entire stack).

#### cho (Bandpass)

Syntax: `cho`

Uses the top three stack values as `up`, `low`, and `mid` to determine if
`mid` falls between `low` and `up`, inclusive. If true, pushes `mid`, otherwise
pushes `0`.

#### da (Data Block)

Syntax: `da*Data*`

TBD.

#### de (Hyperpolic Tangent)

Syntax: `de`

Pushes the hyperbolic tangent of the top stack value. **This codon only
works with continuous automata. For discrete automata, it does nothing.**

#### do (Duplicate)

Syntax: `do`

Peeks at the top stack value and pushes it again. For example, if the
stack is `1 2 3 4`, then the result is `1 2 3 4 4`.

#### e (Avg N)

Syntax: `e`

Pushes the average of the last N values onto the stack, where N is the top
value on the stack, and not included in the average. For example, if the stack
consists of `4 2 1 3`, then N is 3 and `(4+2+1)/3` will be pushed.

#### ga (Halt)

Syntax: `ga`

Pushes Self and stops evaluation.

#### ge (Push All Rotate)

Syntax: `ge`

Pushes all Neighbor cell values including Self onto the stack, "rotated"
by the top stack value. For example, if the top value is 4, pushes cells
beginning at index 4.

#### gi (Avg)

Syntax: `gi*Value*`

Pushes the average of the top *Value* values onto the stack. If *Value* is not
specified, then the entire stack is considered.

#### go (Push All)

Syntax: `go`

Pushes all Neighbor cell values including Self onto the stack. Self is
always pushed last.

#### gu (Count **DEPRECATED - BROKEN - Use `pu` instead**)

Syntax: `gu`

Counts the number of values in the pattern that are equal to the top stack
value and pushes the count. **Note: Does not actually do this, use `pu` instead.**

#### ha (Exclamatory)

Syntax: `ha`

Pushes `N!`, where N is determined by the top stack value. **Note 1: This
can be quite expensive to compute for large values. There are no guardrails
and no overflow check.**
**Note 2: For continuous automata, this does nothing.**

#### he (Convolve)

Syntax: `he`

Pushes a value computed by summing and multiplying parts of the pattern and
the stack.

#### hi (Histogram)

Syntax: `hi`

Computes a histogram of all values in Pattern and pushes it to the stack
in size order. For example, if the pattern is `1 5 7 2 5 7 7 7 1`, then
it will push `1 2 2 4`.

#### ho (Divide)

Syntax: `ho`

Pushes the division of the top two values onto the stack. In the case of division
by zero, simply pushes the top value back.

#### hu (Supersymmetry)

Syntax: `hu`

Pushes the value that is "opposite" to the top stack value and the middle value,
based on the number of colors in the automata. For example, if the automata
has 4 colors, then color 1 is opposite color 2, and color 0 is opposite color 3.

#### i (Pow)

Syntax: `i`

Pushes v1^v2^ onto the stack, where v1 and v2 are popped off the stack.
**Note that for discrete automata, if v2 is less than 0 it will be replaced
by 0.**

#### ja (Jump)

Syntax: `ja`

"Jumps" forward or backward a number of codons determined by the top stack value.
This can allow evaluations to loop (by jumping backward), or to skip (by jumping
forward). There is a maximum number of codon evaluations allowed per cell, so
infinite loops are impossible. This is currently set to 1000. In such cases, the
evaluation is forcibly terminated, with the evaluation result being whatever value
was at the top when the loop was broken.

#### ji (Skip N)

Syntax: `ji`

Pops N values off the stack, where N is determined by the top stack value (which
is popped before the skip and thus not counted as part of the skip).

#### jo (Min N)

Syntax: `jo`

Pushes the minimum of the last N values onto the stack, where N is the top
value on the stack, and not included in the minimum. For example, if the stack
consists of `4 2 1 3`, then N is 3 and `1` will be pushed.

#### jya (Relative Coordinate)

Syntax: `jya[0|1|2]`

For continuous automata, pushes either the relative `x`, `y`, or `z` cell
coordinate, depending on which numeric argument is used. If no numeric
argument is specified, pushes all available coordinates, depending on the
dimensionality of the automata. Relative coordinates range from -1.0 to 1.0,
with 0.0 at the center of the lattice. For discrete automata, functions
the same as `kya`.

#### ka (Bitwise Or)

Syntax: `ka`

Pushes the bitwise-or value of the top two stack values. **Note that this is
only supported for discrete automata. For continuous automata, the top stack value
will simply be pushed back on the stack.**

#### ke (Greater)

Syntax: `ke`

Pushes 1 or 0, depending on whether `v1>=v2`, where v1 and v2 are the top two
stack values.

#### ki (Push Surrounding)

Syntax: `ki`

Pushes all surrounding cells values onto the stack. The definition of
"surrounding" depends on two factors: (1) Neighborhood type (Moore, von Neumann,
or Circular), and (2) Neighborhood size.

#### ko (Bitwise Rotate Left)

Syntax: `ko`

Pushes the bitwise-left-rotated value of the top stack value rotated by the next
top stack value. **Note that this is only supported for discrete automata.
For continuous automata, the top stack value will simply be pushed back on the stack.**

#### ku (Bitwise Rotate Right)

Syntax: `ku`

Pushes the bitwise-right-rotated value of the top stack value rotated by the next
top stack value. **Note that this is only supported for discrete automata.
For continuous automata, the top stack value will simply be pushed back on the stack.**

#### kya (Coordinate)

Syntax: `kya[0|1|2]`

Pushes either the `x`, `y`, or `z` cell coordinate, depending on which numeric
argument is used. If no numeric argument is specified, pushes all available
coordinates, depending on the dimensionality of the automata.

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

#### mya (Mandelbulb)

Syntax: `nya`

Pushes the Mandelbulb Set value using the top five values on the stack, defined
in order as `scl` (scaling factor), `z` (iterations), `x`, and `y`, and `z`
(coordinates). For safety, iterations is capped at 100. While this codon is
intended to be used with 3D automata, it can be used with any dimensionality
since the coordinate values are popped from the stack. Note that this produces
a *discrete approximation* of the fractal, as lattices are discrete in nature.

#### na (Lesser)

Syntax: `na`

Pushes 1 or 0, depending on whether `v1<=v2`, where v1 and v2 are the top two
stack values.

#### ne (Not Equals)

Syntax: `ne`

Pushes 1 or 0, depending on whether `v1!=v2`, where v1 and v2 are the top two
stack values.

#### ni (Square Root)

Syntax: `ni`

Pushes the square root of the absolute value of the top of the stack.

#### no (Push N)

Syntax: `no`

Pushes the Nth pattern value, where N is determined by the top value on the
stack. The pattern contains both neighbor cells and self.

#### nu (Cube Root)

Syntax: `nu`

Pushes the cube root of the top of the stack.

#### nya (Mandelbrot)

Syntax: `nya`

Pushes the Mandelbrot Set value using the top four values on the stack, defined
in order as `scl` (scaling factor), `z` (iterations), `x`, and `y` (coordinates).
For safety, iterations is capped at 100. While this codon is
intended to be used with 2D automata, it can be used with any dimensionality
since the coordinate values are popped from the stack. Note that this produces
a *discrete approximation* of the fractal, as lattices are discrete in nature.
 
#### o (Push Neighbor)

Syntax: `o*Neighbor*`

Pushes *Neighbor* onto the stack, where *Neighbor* is the index of any cell
in the set of neighboring cells. Index values wrap around, so attempts to push
indeces greater than the size of the set of neighbors are "safe".

#### pa (Push Cardinal)

Syntax: `pa`

Pushes all cells lying in cardinal directions from the main cell.

#### pe (Absolute Value)

Syntax: `pe`

Pushes the absolute value of the top stack value.

#### pi (Filter)

Syntax: `pi*Value*`

**Bugged**

#### po (Equals Array)

Syntax: `po*Value*`

If an entire array up to length *Value* is equivalent to a compared array,
returns 1, else 0.

#### pu (Count - Fixed)

Syntax: `pu`

Counts the number of values in the pattern that are equal to the top stack
value and pushes the count.

#### ra (If)

Syntax: `ra`

Pops the top three stack values as *cond*, *tr*, and *fl* (condition, true case,
and false case). If *cond* is non-zero, pushes *tr*, otherwise pushes *fl*.

#### ri (Max N)

Syntax: `jo`

Pushes the maximum of the last N values onto the stack, where N is the top
value on the stack, and not included in the maximum. For example, if the stack
consists of `4 2 1 3`, then N is 3 and `4` will be pushed.

#### re (Sine)

Syntax: `re`

Pushes the sine of the top stack value. **This codon only works with continuous
automata. For discrete automata, it does nothing.**

#### ro (Skip)

Syntax: `ro*Value*`

Pops *Value* values off the stack.

#### ru (Sigmoid)

Syntax: `ru`

Pushes the sigmoid of the top stack value. Specifically, 1/(1+e^-*v*^), where
*v* is the top stack value. **This codon only works with continuous automata.
For discrete automata, it does nothing.**

#### sa (Stop)

Syntax: `sa`

The top of the stack is popped, and if 0 stops evaluation.

#### se (Not)

Syntax: `se`

Pushes 1 if the top stack value is 0, otherwise pushes 0.

#### shi (Position)

Syntax: `shi`

Pushes the current position (i.e., size) of the stack. For example,
if the stack is "5 3 3 1", then pushes 4.

#### so (Not Intersects)

Syntax: `so`

Tests if `mid<low || mid>high`, where all three values are popped off the stack
in the order high, low, mid. Pushes 1 (true) or 0 (false).

#### su (Subtract)

Syntax: `su`

Pushes `v1-v2`, where v1 and v2 are the top two stack values.

#### ta (Max)

Syntax: `ta*Value*`

Pushes the maximum value of the top *Value* values onto the stack. *Value*
is optional; if not specified, all values on the stack are considered
(i.e., the entire stack).

#### te (Time)

Syntax: `te`

Pushes the current iteration count, starting from 0. **Note that for animated
automata, this value will only be consistent over the duration of a single
animation execution. If the animation is stopped and started again, time will
reset to 0. For step animation, the value will always be 0.**

#### to (Bitwise And)

Syntax: `to`

Pushes the bitwise-and value of the top two stack values. **Note that this is
only supported for discrete automata. For continuous automata, the top stack value
will simply be pushed back on the stack.**

#### tsu (Bitwise Xor)

Syntax: `tsu`

Pushes the bitwise-xor value of the top two stack values. For continuous automata,
real values are converted bitwise to integers, then bitwise back to reals
after the xor operation.

#### wa (Most)

Syntax: `wa*Value*`

Pushes the value that occurs the most of times in the pattern.

#### wo (Least)

Syntax: `wo*Value*`

Pushes the value that occurs the least of times in the pattern.

#### u (Intersects)

Syntax: `u`

Tests if `low<=mid<=high`, where all three values are popped off the stack.
Pushes 1 (true) or 0 (false).

#### ya (Self)

Syntax: `ya`

Pushes the current cell value onto the stack.

#### yo (Intersects Self)

Syntax: `yo`

Tests if `low<=mid<=high`, where all three values are popped off the stack.
Pushes `mid` if true, or 0 if false.

#### yu (Fork)

Syntax: `yu*Value*`

Fork skips over a variable number of stack values based on a Neighbor value,
specified by index using the optional *Value*. If *Value* is not specified,
Self is used. This can also be used to effectively branch evaluation within
the incantation if used in tandem with Stop codons for each branch. Further
description TBD as this works in mysterious ways and I'm tired.

#### za (Halt)

Syntax: `za`

Stops evaluation. The evaluation result is whatever value is currently the
top stack value.

#### ze (Random)

Syntax: `ze`

Pushes a random value between 1 and N, where N is determined by the top stack
value. If N is negative, pushes a random value between -1 and N. If N is 0,
pushes 0.

#### zu (Non-zero)

Syntax: `za*Value*`

Filters all non-positive values from the stack for the top *Value* values.
If *Value* is not specified, the entire stack is considered.

Bug: This should probably not filter negative values, but alas.

## Example automata

### The Game of Life

Incantation:

```
ki mi a2 a3 u ki mi8 a3 ma ya ra
```

This is the classic Game of Life (Conway's Life) automata.
