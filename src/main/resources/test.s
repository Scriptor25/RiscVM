.section .data
HELLO:  .string "Hello World!"
TEST:   .string "Hi, my name is Felix Schreiber and I just wrote this!"

.section .text
START:
    la a0, HELLO
	li a1, 0
    sys 0x01
	null
