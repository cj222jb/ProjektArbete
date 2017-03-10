;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;
;	1DT301, Computer Technology
;	Date: 2005-09-05
;	Author
;		Student: Michael Racette Olsén
;		Student: Mikael Andersson	
;		
;	Labb number: 1
;	Task number: 1
;	Title: 		How to use the PORTs. Digital input/output. Subroutine call.
;
;	Hardware: STK600, CPU ATmega2560
;
;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

ldi 	r16	, 0xFF			; load 1111 1111 to register r16
out 	0x04, r16			; write 1111 1111 to Data Direction Register
							; in I/O adress 0x01

my_loop:

ldi 	r16	, 0xFD			; load 1111 1101 to register r16
out 	0x05, r16 			; write 1111 1101 to Port B Output register
							; in I/O adress 0x05

rjmp 	my_loop
