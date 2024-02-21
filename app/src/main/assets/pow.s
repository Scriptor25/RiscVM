.section .text

_start:
    li a0, 4
    li a1, 3
    jr pow
	
	li a7, 93
	ecall
    
    j _start

# pow:
#  base   = a0
#  power  = a1
#
#  result = a0
#
pow:
    li s0, 0
    li s1, 1
check:
    blt s0, a1, loop
    mv a0, s1
    ret
loop:
    mul s1, s1, a0
    addi s0, s0, 1
    j check
