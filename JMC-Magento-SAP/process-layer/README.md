# Acqua Circular Process Layer

El objetivo de esta API es consumir los servicios expuestos por el System Layer, que a su vez, consumen los servicios de 4 sistemas para realizar diferentes operaciones por medio de RESTful.

## `Aclaracion`
Todas las consultas tienen que enviarse el `Content-Type` en `application/json`

## Integraciones

* Liberya (ERP)
* BioStar2 (Sistema de Seguridad)
* PaperCut (Sistema de Impresión)
* UniFi (Sistema de Redes)

## Mule Enviroment
Para cambiar entre los entornos de mule, hay que cambiar el *mule_env* en [src/main/app/mule-app.properties](https://github.com/AcquaNet/Circular-Process-Layer/blob/development/src/main/app/mule-app.properties) de dev o prod. Ejecutando del Mule CE hay que setear el entorno antes de correr mule con el comando: SET mule_env=prod

## Auth
Todos los metodos utilizan basic auth para la autenticacion. Las credenciales son configuradas en la configuracion de mule [src/main/resources/{mule_env}.properties](https://github.com/AcquaNet/Circular-Process-Layer/blob/development/src/main/resources).

## [1] Asignar Creditos de Impresion

### Detalle 
Agregar o quitar creditos de impresión.
* Metodo: `POST`
* Ruta: `/updateCredits`
* Destino: `PaperCut`

### Request 
```
{
    "username": "Usuario de PaperCut",
    "amount": (int) "Cantidad de creditos por agregar (En negativo para restar)"
}

```
  
### Respuesta
```
(UserInfo) {
    newBalance (int): Creditos del Usuario,
}
```

  
## [2] Status de Consumos de Impresión
### Detalle 
Consulta de creditos de impresión.
* Metodo: `GET`
* Ruta: `/getUserInfo`
* Destino: `PaperCut`

### Parametros
```
HEADER: 
Key -> user 
Valor -> "Usuario PaperCut"
```

### Respuesta
```
(UserInfo) {
    balance (int): Creditos del Usuario,
    jobCount (int): Cantidad de trabajos de impresión,
    pageCount (int): Cantidad de paginas impresas
}
```

## [3] Alta de Usuario
### Detalle
Alta de usuario en todos los sistemas. Alta de entidad comercial en Libertya ~ Alta de cuenta Radius para red wifi en UniFi ~ Alta de Usuario en PaperCut ~ Alta de usuario y tarjeta de seguridad en BioStar2. La peticion soporta la alta individual en cada sistema
* Metodo: `POST`
* Ruta: `/createUser`
* Destino: `PaperCut ~ Libertya ~ BioStar2 ~ UniFi`

### Request
```
{
	"bioStar2": {
		"biostarEnabled": "true/false para dar de alta o no en sistema",
		"biostarCreds": {
			"subdomain": "Subdominio de biostar2 (Ej. `impresiones`)",
			"pass": "Contraseña de Usuario BioStar2",
			"user": "Usuario de BioStar2"
		},
		"biostarUser": {
			"name": "Nombre de Usuario a crear",
			"userId": (int) "Id de Usuario",
			"email": "Direccion de email",
			"phoneNumber": "Numero de Telefono",
			"status": "Estado de Usuario (AC -> Activo)",
			"startDateTime": "Fecha de Inicio. ISO 8601",
			"endDateTime": "Fecha de Fin. ISO 8601",
			"userGroup": (int) "Id del Grupo de Usuario",
			"accessGroups": (array[int]) "Ids de Grupos de Acceso",
			"cardIds":  (array[int]) "Ids de Tarjeta de Seguridad. Min 2",
			"cardFormat": (int) "0 (Default)"
		}
	},
	"libertya": {
		"libertyaEnabled": "true/false para dar de alta o no en sistema",
		"libertyaUser": {
			"id": "Id de Entidad Comercial",
			"taxId": "CUIT sin '-' ",
			"taxIdType": (int) "Id de tipo de Documento (80 -> CUIT)",
            "taxCategory": (int) "Id de Categoria de IVA",
			"firstName": "Nombre de la Entidad",
			"secondName": "Nombre alternativo de la Entidad",
			"bpGroupId": (int) "Id de Grupo de Entidad(1010045 -> Standard)",
			"isOneTime": "Y/N si es ",
			"isProspect": "Y/N si es ",
			"isVendor": "Y/N si es proveedor",
			"isCustomer": "Y/N si es cliente",
			"isEmployee": "Y/N si es empleado",
			"isSalesRep": "Y/N si es ",
			"paymentTermId": (int) "1010083",
			"priceListId": (int) "1010595",
			"address": "Direccion de Entidad",
			"phone": "Numero de Telefono",
			"employeeNumber": (int) "Cantidad de Empleados"
		}
	},
	"paperCut": {
		"papercutEnabled": "true/false para dar de alta o no en sistema",
		"papercutUser": {
			"username": "Nombre de Usuario",
			"password": "Contraseña",
			"fullName": "Nombre de Entidad",
			"cardNumber": (int) "Numero de Tarjeta",
			"pin": (int) "pin",
			"businessName": "Razon Social",
			"office": "Numero de Oficina",
			"notes": "Notas de Papercut",
			"initialBalance": (int) "Saldo Inicial",
			"email": "Direccion de email"
		}
	},
	"uniFi": {
		"unifiEnabled": "true/false para dar de alta o no en sistema",
		"unifiCreds": {
			"username": "Usuario de UniFi",
			"password": "Contraseña de UniFi"
		},
		"unifiUsers": {
			"radius": {
				"radiusUser": "Nombre de Usuario de Wifi",
				"radiusPass": "Contraseña",
				"netId": (int) "Id de Red de Wifi),
				"radiusTunnelType": (int) "Id de tipo de (13 -> VLAN),
				"radiusMiddleType": (int) "Id de tipo de (6 -> 802)
			}
		}
	}
} 
```

### Respuesta
```
(CreateUser) {
    paperCutDetails (PaperCut): Resultado de Operacion en PaperCut,
    libertyaDetails (Libertya): Resultado de Operacion en Libertya,
    uniFiDetails (UniFi): Resultado de Operacion en UniFi,
    bioStarDetails (BioStar2): Resultado de Operaciones en BioStar2
}
PaperCut {
    id (String): Id de Usuario PaperCut (username)
}
Libertya {
    bPartnerID (int): Id de Entidad Comercial en Libertya,
    locationID (int): Id de Ubicacion de Entidad Comercial
}
UniFi {
    _id (String): Id de Usuario Radius en UniFi
}
BioStar2{
    user (User): Resultado de Alta de Usuario,
    card (Card): Resultado de Alta de Tarjeta de Seguridad
}
User {
    status_code (String, Ej. 'CREATED'): Resultado de la Operacion,
    user_id (String): Id de Usuario en BioStar2,
    message (String, Ej. 'Created successfully'): Mensaje de la Operacion
}
Card {
    status_code (String, Ej. 'SUCCESSFUL'): Resultado de la Operacion,
    message (String, Ej. 'Processed Successfully'): Mensaje de la Operacion,
    userId (String): Id de Usuario en BioStar2,
    cardId (int): Id de Tarjeta en BioStar2
}
```

## [4] Suspender Usuario

### Detalle
Suspender un usuario en los sistemas excluyendo ERP. Borrar usuario en BioStar2 y UniFi, y reduccion de creditos de impresion a 0.
* Metodo: `POST`
* Ruta: `/suspendUserAccount`
* Destino: `PaperCut ~ BioStar2 ~ UniFi`

### Requests
```
{
	"bioStar2": {
		"biostarEnabled": "true/false para dar de baja o no en sistema",
		"biostarCreds": {
			"subdomain": "Subdominio de biostar2 (Ej. `impresiones`.circularinnova....)",
			"pass": "Contraseña de Usuario BioStar2",
			"user": "Usuario de BioStar2"
		},
		"biostarSystem": {
			"userId": (int) Id de Usuario en BioStar2
		}
	},
	"paperCut": {
		"papercutEnabled":"true/false quitar creditos en sistema",
		"papercutSystem": {
			"username": "Usuario de PaperCut"
		}
	},
	"uniFi": {
		"unifiEnabled": "true/false para dar de baja o no en sistema",
		"unifiCreds": {
			"username": "Usuario de UniFi",
			"password": "Contraseña de UniFi"
		},
		"unifiSystem": {
				"radiusID": "Id de Cuanta de Wifi"
		}
	}
}
```
  
### Respuesta
```
(SuspendUser) {
    bioStarDetails (BioStar2): Resultado de Operacion en BioStar2,
    paperCutDetails (PaperCut): Resultado de Operacion en PaperCut,
    uniFiDetails (UniFi): Resultado de Operacion en UniFi,
}
BioStar {
    message (String, Ej. 'Deleted succesfully'): Mensaje de la Operacion,
    status_code (String, Ej. 'DELETED'): Resultado de la Operacion
}
PaperCut {
    message (String, Ej. 'User acquaprueba deleted.')
}
UniFi ?
```
## [5] Alta de Tarjeta de Seguridad

### Detalle
Alta de Tajeta de Seguridad en BioStar2.
* Metodo: `POST`
* Ruta: `/altaTarjeta`
* Destino: `BioStar2`

### Request
```
{
	"accessGroups": (array[int]) "Ids de Grupos de Acceso",
	"cardIds":  (array[int]) "Ids de Tarjeta de Seguridad. Min 2",
	"cardFormat": (int) "0 (Default)"
}
```

### Respuesta
```
(AltaTarjeta) {
    message (String, Ej. 'Processed Successfully'): Mensaje de la Operacion,
    status_code (String, Ej. 'SUCCESSFUL'): Resultado de la Operacion
    userId (String): Id de Usuario en BioStar2,
    cardId (String): Id de Tarjeta de Seguridad
}
```

## [6] Consulta de Facturas

### Detalle
Consulta de facturas por CUIT de la entidad
* Metodo: `GET`
* Ruta: `/getInvoicesDetails`
* Destino: `Libertya`

### Parametros
```
HEADER: 
Key -> taxId 
Valor -> "Cuit de Entidad Comercial"
```

### Respuesta
Todos los datos de cabezera recuperados de Libertya en formato Json

## [6] Consulta de Recibos

### Detalle
Consulta de facturas por CUIT de la entidad
* Metodo: `GET`
* Ruta: `/getAllocationDetails`
* Destino: `Libertya`

### Parametros
```
HEADER: 
Key -> user 
Valor -> "Cuit de Entidad Comercial"
```

### Respuesta
Todos los datos de cabezera recuperados de Libertya en formato Json

## [7] Consulta de Grupos de Acceso

### Detalle
Consulta de todos los grupos de acceso con sus Ids en BioStar2
* Metodo: `GET`
* Ruta: `/getAccessGroups`
* Destino: `BioStar2`

### Parametros
```
HEADER: 
Key -> user 
Valor -> "Usuario de BioStar2"
Key -> pass 
Valor -> "Contraseña de BioStar2"
Key -> subdomain 
Valor -> "Sub dominio de BioStar2 (impresiones)"
```

### Respuesta
```
accessGroups {
	id (int): Id de Grupo de Acceso,
	accessGroup (String): Nombre de Grupo de Acceso
}
```

## [8] Consulta de Grupos de Usuario

### Detalle
Consulta de todos los grupos de usuario con sus Ids en BioStar2
* Metodo: `GET`
* Ruta: `/getUserGroups`
* Destino: `BioStar2`

### Parametros
```
HEADER: 
Key -> user 
Valor -> "Usuario de BioStar2"
Key -> pass 
Valor -> "Contraseña de BioStar2"
Key -> subdomain 
Valor -> "Sub dominio de BioStar2 (impresiones)"
```

### Respuesta
```
userGroups {
	id (int): Id de Grupo de Acceso,
	userGroup (String): Nombre de Grupo de Acceso
}
```

## Desarollo
Esta implementacion se realizo con el Runtime CE de Mule.

### Requerimientos para correr local
...

### Archivos de Ejemplo
Se encuentran archivos de ejemplos de peticiones y respuestas en formato json en: [src/main/resources/samples](https://github.com/AcquaNet/Circular-Process-Layer/tree/development/src/main/resources/samples)

### Clases Java de Requests
Se encuentran clases Pojo de los Requests a los Apis: [src/main/java/com.acqua.models](https://github.com/AcquaNet/Circular-Process-Layer/tree/development/src/main/java/com/acqua/models)

### Configuracion de Mule
Se encuentra la configuracion de mule en: [src/main/app/mule-app.properties](https://github.com/AcquaNet/Circular-Process-Layer/blob/development/src/main/app/mule-app.properties)

### Instalacion de Docker

Todos los comandos son sin comillas y es un requerimiento ya tener clonados los repositorios del Service y Process Layer.

1. Borrar las carpetas .docker y .VirtualBox si existen en la carpeta del usuario en %Users%
2. Instalar la ultima version de [VirtualBox](https://www.virtualbox.org/wiki/Downloads)
3. Instalar [Git](https://git-scm.com/download/win) para Windows
4. Instalar [Docker Toolbox](https://github.com/docker/toolbox/releases) para Windows. En la instalacion, no instalar VirtualBox ni Git, y en la siguiente pagina no seleccionar el upgrade del Boot2Docker.
5. Abrir el Kitematic, configurar y logear en la cuenta de usuaio.
6. Hacer un mvn clean package en ambos repositorios. Confirmar que los dos sistemas esten en el directorio %Process Layer%/docker_jre/images como archivos .zip.
7. Descargar la version 3.9.0 de [Mule CE](https://developer.mulesoft.com/download-mule-esb-runtime) en la version para linux (.tar.gz) y guardarlo en el directorio %Process Layer%/docker_jre/images
8. Abrir CMD/Windows Powershell/Docker Terminal, ir al directorio %Process Layer%/docker_jre y crear la imagen de docker con el comando: "docker build -t circular-server ."
9. Confirmar la imagen y visualizar el Image ID: "docker images"
10. Generar e iniciar el Docker container con el comando: "docker run -i --net=host -t *idImage* " <-- donde *idImage* es el Image ID. Ej, docker run -i --net=host -t abc0d7808672
11. Confirmar el estado del container con el comando: "docker ps -a" 

