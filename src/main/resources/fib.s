.section .text
_start:	la sp, stack
		
		li a0, 10
		jr fib
		
		li a7, 93
		ecall		# exit with result code
		
		j _start
		
fib:	push ra
		push s0
		push s1
		
		beqz a0, ret0
		li t0, 1
		beq a0, t0, ret1
		
		subi s0, a0, 1
		subi s1, a0, 2
		
		mv a0, s0
		jr fib
		mv s0, a0
		
		mv a0, s1
		jr fib
		mv s1, a0
		
		add a0, s0, s1
		
		pop s1
		pop s0
		pop ra
		ret

ret0:	li a0, 0
		
		pop s1
		pop s0
		pop ra
		ret
ret1:	li a0, 1
		
		pop s1
		pop s0
		pop ra
		ret

.section .stack
		.skip 0x1000
stack:	.word 'A'
