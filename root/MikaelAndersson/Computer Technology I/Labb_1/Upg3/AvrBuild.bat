@ECHO OFF
"C:\Program Files (x86)\Atmel\AVR Tools\AvrAssembler2\avrasm2.exe" -S "F:\Assembly\labels.tmp" -fI -W+ie -C V3 -o "F:\Assembly\Upg3.hex" -d "F:\Assembly\Upg3.obj" -e "F:\Assembly\Upg3.eep" -m "F:\Assembly\Upg3.map" "F:\Assembly\Upg3.asm"
