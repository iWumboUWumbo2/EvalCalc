# EvalCalc
Powerful scientific calculator that uses the shunting yard algorithm to calculate expressions.

Howe to use:
To set a variable equal to an expression, write it in the form "<varname> = <expression>" where <varname> is the name of the variable and <expression> is the expression it should be set to. \
A variable can be used in place of a number in an expression.

**The following default variables are present:** \
        Pi:     3.141592... \
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
