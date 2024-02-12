.section .stack
.skip 0x1000
STACKSTART: .word 'A'

.section .text
START:
	la sp, STACKSTART
	
	li a0, 10
	jr FIB
	
	# exit with result code
	li a7, 93
	ecall

FIB:
	push ra
	push s0
	push s1
	
	beqz a0, RET0
	li t0, 1
	beq a0, t0, RET1
	
	subi s0, a0, 1
	subi s1, a0, 2
	
	mv a0, s0
	jr FIB
	mv s0, a0
	
	mv a0, s1
	jr FIB
	mv s1, a0
	
	add a0, s0, s1
	
	pop s1
	pop s0
	pop ra
	
	ret
RET0:
	li a0, 0
	
	pop s1
	pop s0
	pop ra
	
	ret
RET1:
	li a0, 1
	
	pop s1
	pop s0
	pop ra
	
	ret
