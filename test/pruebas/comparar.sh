#!/bin/bash

PRINCIPIO_NOMBRE_SALIDA=o # EJEMPLO: Si los archivos de salida son: o1.bpmn, o2.bpmn, ...; poner "o".

for j in p1 p2 p3 ; do
	UBICACION=$j
	for i in $(ls $UBICACION/r*.bpmn | cut -d/ -f2 | cut -d. -f1 | cut -dr -f2) ; do
		clear
		echo "--- DIFF $i ---"
		diff -ywB --suppress-common-lines $UBICACION/r$i.bpmn $UBICACION/$PRINCIPIO_NOMBRE_SALIDA$i.bpmn
		echo "--- FIN DIFF ---"
		echo "PRESIONE ENTER PARA VER EL SIGUIENTE DIFF"
		read
	done
done
clear
