# EvalCalc
A powerful scientific calculator that uses the shunting yard algorithm to calculate expressions.

How to use:
Reminder! Input is case-insensitive!

To set a variable equal to an expression, write it in the form "<varname> = <expression>" where <varname> is the name of the variable and <expression> is the expression it should be set to. \
A variable can be used in place of a number in an expression. \

To set output to binary, octal, decimal, or hexadecimal, type bin, oct, dec, hex, respectively. \
To type a binary number, prefix its string with "0b"; octal, "0o"; decimal, "0d"; hexadecimal, "0x"

**The following default variables are present:** \
        Pi:     3.1415926... \
		e:		2.7182818... \
        Ans:    The output of the most recently executed expression. \
        Rand:   A randomly generated number between 0 and 1.

**The following operators are available:** \
        Addition:       '+' \
        Subtraction:    '-' \
        Multiplication: '*' \
        Division:       '/' \
        Exponentiation: '^' \
        Factorial:      '!'
  
**The following functions are available:** \
        sin,    cos,    tan, \
        asin,   acos,   atan, \
        cosh,   sinh,   tanh, \
        sqrt,   cbrt,   exp, \
        abs,    ceil,   floor, \
        round,  sign,   ln, \
        log,    deg,    rad

Debug mode allows for testing to see the stack and is used for ensuring the expression is being correctly parsed.
