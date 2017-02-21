@ECHO OFF
"C:\Program Files (x86)\Atmel\AVR Tools\AvrAssembler2\avrasm2.exe" -S "F:\Assembly\Upg5\labels.tmp" -fI -W+ie -C V3 -o "F:\Assembly\Upg5\Upg5.hex" -d "F:\Assembly\Upg5\Upg5.obj" -e "F:\Assembly\Upg5\Upg5.eep" -m "F:\Assembly\Upg5\Upg5.map" "F:\Assembly\Upg5\Upg5.asm"
