# FPH

This is a demo of doing Field Preserving Hashing, with AES and discarding portions of the hash to maintain a matching style
between the input format and the hash, Preserving digit, space, punctuation and alpha char position.

Because this is not a fixed size hashing function, there are collisions when using small inputs.

## Usage

```bash

$> curl -H "Content-Type: application/text; charset=utf-8" -X POST http://localhost:8080/hash
 -d "12 sheet street, london"
94 cfJeQ ZbqRgT, DsHDCe
 
$> curl -H "Content-Type: application/text; charset=utf-8" -X POST http://localhost:8080/hash
  -d "1234 5678 9123 1523"
7071 7651 6826 5543
```