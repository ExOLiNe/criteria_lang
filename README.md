# Simple criteria language

## Example
```
object['some'] in ['val1', 'val2', 'val3']
    && object['someNum'] + object['someNum2'] * 5 == 60
```

```
$some = object['field'] + object['field2'];
$some == 20
```

## TODO
- `in range` operator for numbers
- fix equals/notEquals for date comparison
- fix identifier true/false assignment
- add test: function call with insufficient args