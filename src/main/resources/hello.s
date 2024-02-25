.section .text
_start:	li a7, 64
		li a0, 1		# STDOUT FD = 1
		la a1, hello
		li a2, 12
		ecall
		
		li a7, 93
		li a0, 123
		ecall
		
		j _start

.section .rodata
hello:	.ascii "Hello World\n"
