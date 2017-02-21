;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;
;	1DT301, Computer Technology
;	Date: 2005-09-05
;	Author
;		Student: Michael Racette Olsén
;		Student: Mikael Andersson	
;
;	Labb number: 1
;	Task number: 2
;	Title: 		How to use the PORTs. Digital input/output. Subroutine call.
;
;	Hardware: STK600, CPU ATmega2560
;
;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

; Initialize switches:
ldi		r16	,0x00		; load	0000 0000 to r16
out		0x01,r16		; write 0000 0000 to DDRA	(INPUTS)

; Initialize leds:
ldi		r16	,0xFF		; load	1111 1111 to r16
out 	0x04,r16		; write 1111 1111 to DDRB	(OUTPUTS)

my_loop:

in		r16	,0x00		; load 	input state from PINA
out		0x05,r16		; write to PORTB

rjmp my_loop



