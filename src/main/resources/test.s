.section .data
HELLO:  .string "Hello World!"
TEST:   .string "Hi, my name is Felix Schreiber and I just wrote this!"

.section .stack
.skip 0x1000 ; 4 KB Stack

.section .text
START:
    li a0, HELLO
    sys 0x01
    nop
