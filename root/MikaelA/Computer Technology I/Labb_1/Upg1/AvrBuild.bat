@ECHO OFF
"C:\Program Files (x86)\Atmel\AVR Tools\AvrAssembler2\avrasm2.exe" -S "F:\Assembly\Upg1\labels.tmp" -fI -W+ie -C V3 -o "F:\Assembly\Upg1\Upg1.hex" -d "F:\Assembly\Upg1\Upg1.obj" -e "F:\Assembly\Upg1\Upg1.eep" -m "F:\Assembly\Upg1\Upg1.map" "F:\Assembly\Upg1\Upg1.asm"
