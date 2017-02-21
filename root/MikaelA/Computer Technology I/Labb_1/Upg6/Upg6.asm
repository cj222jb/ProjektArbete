;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;
;	1DT301, Computer Technology
;	Date: 2005-09-05
;	Author
;		Student: Michael Racette Olsén
;		Student: Mikael Andersson
;
;	Labb number: 	1
;	Task number:	6
;	Title: 		How to use the PORTs. Digital input/output. Subroutine call.
;
;	Hardware: STK600, CPU ATmega2560
;
;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

.include "m2560def.inc"

; Initialize SP, Stack Pointer
ldi r20, HIGH(RAMEND) ; R20 = high part of RAMEND address
out SPH,R20 ; SPH = high part of RAMEND address
ldi R20, low(RAMEND) ; R20 = low part of RAMEND address
out SPL,R20


ldi 	r16	, 0xFF			;Load 	1111 1111 to register r16
out 	DDRB, r16			;Write	1111 1111 to Data Direction Register. (Outputs)

ldi		r17	, 0x00			;Led representation:	Currently turned on leds
ldi		r18 , 0xFF			;Used to compare:		ALL LEDS ON
ldi		r19 , 0x00			;Used to compare:		ALL LEDS OFF



left:

	;Calculation
	lsl r17					;Logical Shift Left:	Move all leds to left
	inc r17					;Increase by one:		Turn on led0.

	;Output:
	mov r16, r17			;Copy led representation to temp reg (r16)
	com r16					;Use 1-complement to invert outputs
	out 0x05, r16			;Turn on correct leds

	;Delay
	rcall Delay				;Call delay function

	;Compare
	cp	r17	,r18			;Compares led representation (r17) with argument (r18)  
	brne left				;If not all leds are on, continue loop.

jmp right					;When all leds are turned on, jump to right


right:

	;Calculation
	lsr r17					;Logical Shift Right: Move all leds to right

	; Output:
	mov r16, r17			;Copy led representation to temp reg (r16)
	com r16					;Use 1-complement to invert outputs
	out 0x05, r16			;Turn on correct leds


	rcall Delay				;Call delay function

	cp r17, r19				;Compares led representation (r17) with argument (r19)
	brne right				;Loop until all leds are turned on

jmp left					;When all leds are turned off: jump to left


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


