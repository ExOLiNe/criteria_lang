# Simple criteria language

## Example
```
object['some'] in ['val1', 'val2', 'val3']
    && object['someNum'] + object['someNum2'] * 5 == 60
```

## TODO
- null support for var access and `NULL` token (separate `None` and real `null` ???)
- `in range` operator for numbers
- `contains` operator
- `like`/`startsWith` operator for strings
- `size` function for arrays
- dates support
  - `olderThan`, `newerThan` operators
- variables support and var debug
- custom identifiers
  - `include` statement (for loading of own identifiers)