.section .text
_start:
	# STDOUT FD = 1
	li a7, 64
	li a0, 1
	la a1, helloworld
	li a2, 12
	ecall
	
	li a7, 93
	li a0, 123
	ecall
    
    j _start

.section .rodata
helloworld:
	.ascii "Hello World\n"
