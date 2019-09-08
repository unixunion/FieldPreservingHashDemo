# FPH

This is a demo of doing Field Preserving Hashing, using AES cryptography and then discarding most of the crypto in order 
to produce a relatively unique hash.

## Usage

```bash

$> curl -H "Content-Type: application/text; charset=utf-8" -X POST http://localhost:8080/hash
 -d "12 sheet street, london"
94 cfJeQ ZbqRgT, DsHDCe
 
$> curl -H "Content-Type: application/text; charset=utf-8" -X POST http://localhost:8080/hash
  -d "1234 5678 9123 1523"
7071 7651 6826 5543
```