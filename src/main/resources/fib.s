.section .stack
.skip 0x1000
STACKSTART: .word 1

.section .data
OUTTEXT: .string "FIB:%d = %d%n"

.section .text
START:
	la sp, STACKSTART
	
	li a0, 20
	
	li a1, 2
	mv a2, a0
	
	jr FIB
	
	mv a3, a0
	la a0, OUTTEXT
	sys 0x01
	
	null

FIB:
	push ra
	push t0
	push t1
	
	beqz a0, RET0
	li t0, 1
	beq a0, t0, RET1
	
	subi t0, a0, 1
	subi t1, a0, 2
	
	mv a0, t0
	jr FIB
	mv t0, a0
	
	mv a0, t1
	jr FIB
	mv t1, a0
	
	add a0, t0, t1
	
	pop t1
	pop t0
	pop ra
	
	ret
RET0:
	li a0, 0
	
	pop t1
	pop t0
	pop ra
	
	ret
RET1:
	li a0, 1
	
	pop t1
	pop t0
	pop ra
	
	ret
