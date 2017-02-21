;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;
;	1DT301, Computer Technology
;	Date: 2005-09-05
;	Author
;		Student: Michael Racette Olsén
;		Student: Mikael Andersson	
;		
;	Labb number: 1
;	Task number: 3
;	Title: 		How to use the PORTs. Digital input/output. Subroutine call.
;
;	Hardware: STK600, CPU ATmega2560
;
;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
ldi		r16	,0x00		; load	0000 0000 to r16
out		0x01,r16		; write 0000 0000 to DDRA	(INPUTS)

; Set Argument
ldi 	r17,0xDF 		; load 	1101 1111 to r17

; Initialize leds:
ldi		r16	,0xFF		; load	1111 1111 to r16
out 	0x04,r16		; write 1111 1111 to DDRB	(OUTPUTS)

my_loop:

	;Turn off all leds:
	ldi		r16, 0xFF
	out		0x05,r16

	;Read input
	in		r16	,0x00		; load 	input state from PINA

	cp		r16,r17			; Compares the input (r16) to the argument (r17),	
	brne 	my_loop			; if equal led 0 lights up.

	ldi		r16, 0xFE
	out  	0x05,r16

rjmp my_loop

