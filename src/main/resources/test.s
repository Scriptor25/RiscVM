.section .text
_start:	li a7, 64
		li a0, 1
		la a1, test
		li a2, 54
		ecall
		
		li a7, 93
		li a0, 0
		ecall
		
		j _start

.section .rodata
hello:	.ascii "Hello World!\n" # 13
test:	.ascii "Hi, my name is Felix Schreiber and I just wrote this!\n" # 54
