# Explicacion de como funciona la aplicacion

## Poll B1_Sync_Fetch (Minutos 0, 10, 20, 30, 40, 50 de c/hora)

- Login
- Loop de cada grupo (Maestros, transacciones)
	- Fetch de resultados Nuevos
		- Si hay mas de 20, loopea por el flow de busqueda extra hasta que no hay mas
		  segun si existe la variable odata.nextLink en el payload
	- Va a CheckIfNullOrEmpty
		- Check si hay novedades o ninguna
		- Guarda en flowVars.mostRecent el valor mas nuevo entre las novedades
		- Busca si hay Nulos
			- Nulos: Patch a cada nulo
				- Busqueda de novedades entre ultimo nulo patcheado y fecha de flowVars.mostRecent
					- Combina resultados entre originales y nuevos buscados
					- Repetir checkeo nulos (CheckIfNullOrEmpty) y patch hasta que no hay mas nulos ni novedades 
				      (se asegura de agarrar todo hasta flowVars.mostRecent)
			- No nulos: Nada. todo OK
	- Va a PollFlow (Envio a ActiveMQ)
		- Setear resultados a payload (Para manipulacon)
		- Ordenado de resultados por fecha y hora
		- Inicio de Conexion a ActiveMQ
		- ForEach de cada novedad (referenciado a forEachFlowTransaction)
			- Convierte la novedad a JSON y guarda a variable
			- Inicio de session de ActiveMQ
			- ForEach de cada Destino
				- Intercambia payload (sobreescribido por el foreach) 
				  y la destinacion a una variable
				- Crea un Queue endpoint para el destino (y queue en ActiveMQ si no existe)
				- Agrega el mensaje a la queue
			- Intento de actualizar la fecha y hora
			- Si no falla, commit de todos los mensajes (novedad del Loop actual a cada destino)
			- ForEach de cada destino para cerrar los endpoint de queue
			- Fin de session de ActiveMQ
			- (Siguiente novedad - forEachFlowTransaction)
		- Fin de todas las novedades
	- Fin de PollFlow
- Fin de Loop de grupo

## Poll B1_Sync_Distribute (Minutos 2, 12, 22, 32, 42, 52 de c/hora)

- Inicializacion de flowVars y configuracion
- ForEach de cada Destino
	- Login al destino
	- ForEach de cada Grupo (Maestros, Transacciones)
		- Seteo de variables del grupo
		- b1_sync_distributeFlow:
			- Inicio de session de ActiveMQ
			- While loop a traves de java hasta que no hay mas novedades en la cola de ActiveMQ
				- Check extra si la novedad es nula
				- (B1_Sync_ProcessQueue)
				- Setear URL segun sea transaccion o maestro (transacciones usan integer, maestros usan string)
				- Checkear si existe
					- Si existe: PATCH
					- Si no existe: POST
				- Mandar PATCH/POST
				- Todo OK
				- Commit de transaccion en java (Saca el mensaje de la queue)
			- Fin while loop
			- Cerrado de session de ActiveMQ
			- Fin de b1_sync_distributeFlow
			- (Siguiente grupo)
	- Fin del ForEach de cada grupo
	- (siguiente destino)
- Fin del Foreach de cada desitno