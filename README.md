# Simple criteria language

## Example
```
object['some'] in ['val1', 'val2', 'val3']
    && object['someNum'] + object['someNum2'] * 5 == 60
```

## TODO
- braces support
- functions support
- `in range` operator for numbers
- `contains` operator
- `like`/`startsWith` operator for strings
- `size` function for arrays
- custom identifiers
  - `include` statement (for loading of own identifiers)
- null support for var access and `NULL` token (separate `None` and real `null` ???)
- dates support
  - `olderThan`, `newerThan` operators