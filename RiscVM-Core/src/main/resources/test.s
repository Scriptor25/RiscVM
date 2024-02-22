.section .rodata
HELLO:  .ascii "Hello World!\n" # 13
TEST:   .ascii "Hi, my name is Felix Schreiber and I just wrote this!\n" # 54

.section .text
START:
    li a7, 64
	li a0, 1
	la a1, TEST
	li a2, 54
	ecall
	
	li a7, 93
	li a0, 0
	ecall
