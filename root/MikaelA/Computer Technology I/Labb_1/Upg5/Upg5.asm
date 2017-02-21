;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;
;	1DT301, Computer Technology
;	Date: 2005-09-05
;	Author
;		Student: Michael Racette Olsén
;		Student: Mikael Andersson
;	Labb number: 1
;	Task number: 5
;	Title: 		How to use the PORTs. Digital input/output. Subroutine call.
;
;	Hardware: STK600, CPU ATmega2560
;
;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

.include "m2560def.inc"		; include file for Atmega 2560
; by including the file m2560def.inc names can be used instead of addresses
; for example DDRA, DDRB, PORTA, PORTB, PINA, PINB etc

; Initialize SP, Stack Pointer
; Necessary for using instructions rcall and ret

ldi r20, HIGH(RAMEND) ; R20 = high part of RAMEND address
out SPH,R20 ; SPH = high part of RAMEND address
ldi R20, low(RAMEND) ; R20 = low part of RAMEND address
out SPL,R20

; Initialize leds:
ldi 	r16	, 0xFF			; load 	1111 1111 to register r16.
out 	0x04, r16			; write 1111 1111 to Data Direction Register.
; Set registers.
ldi		r17	, 0x01			; load 	0000 0001 to register r17.
ldi		r18 , 0x00			; load 	0000 0000 to register r18.

my_loop:

; Output:
	mov r16, r17			; Copies the register r16 to r17.
	com r16					; Use 1-complement to invert outputs.
	out 0x05, r16			; Turn on the correct leds using the register r16.

	rcall Delay

; Calculation:
	lsl r17					; Logical shift left. Shifts the led that should be lit one step to the left.
; Argument:
	cp	r17	,r18			; Compares the registers r17 to r18, 
	brne my_loop			; loops until the last led is lit.

	ldi r17	,0x01			; When the last led is lit, the register is set to default (First led is lit)
							; and the loop starts over.

rjmp my_loop




Delay:

	ldi r25, 255			; r25 = limit value
	ldi r21, 0				; r21 = loop counter 1
	ldi r22, 0				; r22 = loop counter 2

delay_1:

	inc r21					; r21 = r21 + 1
	rcall delay_2			; Call subrutin delay_2
	cp r25, r21				; Compare counter 1 (r21) with limit (r25)
	brne delay_1			; loop until limit (r25) has been reached

ret							; Return to Delay caller

delay_2:

	inc r22					; r22 = r22 + 1
	cp r25, r22				; Compare counter 2 (r22) with limit (r25)
	brne delay_2			; loop until limit (r25) has been reached

ret							; Return


