@ECHO OFF
"C:\Program Files (x86)\Atmel\AVR Tools\AvrAssembler2\avrasm2.exe" -S "F:\Assembly\Upg6\labels.tmp" -fI -W+ie -C V3 -o "F:\Assembly\Upg6\Upg6.hex" -d "F:\Assembly\Upg6\Upg6.obj" -e "F:\Assembly\Upg6\Upg6.eep" -m "F:\Assembly\Upg6\Upg6.map" "F:\Assembly\Upg6\Upg6.asm"
