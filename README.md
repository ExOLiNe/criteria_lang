# Simple criteria language

## Example
```
import header27;
$some3 = object['field1'] + object['field2'];

size($some) == 5 && ($some3 - 5) > 20 && (object['field3/innerField'] > date(1998, 12)
 || object['field4'] in [10, 15, 18])
```

## Features
- numbers and arithmetics
- boolean logic
- functions and infix functions
- variables
- imports
- dates

## TODO
- `in range` operator for numbers
- fix equals/notEquals for date comparison
- fix identifier true/false assignment