# ScrumGraph 

Este proyecto corresponde al backend de la aplicación web construida como proyecto de grado.

**SCRUMGRAPH** publica una serie de recursos de RESTful web services relacionados con los elementos claves en el framework de Scrum,  con el objetivo de almacenar dichos elementos en grafos para dar un apoyo en la toma de decisiones en los compromisos a adquirir en la planificación de futuros Sprints, basandose en la información registrada en cada Nodo.

## Tecnologias
   * Servicios RESTful construidos con jersey.
   * Neo4j - (Graph Database)
   * Java 7

### Se realizará migración del proyecto a Spring Framework 5 con Java 8

## Modelo 

![alt text](img/modelodominio.png)



## Servicios REST [En Construcción]


El proyecto cuenta con los siguientes RESTful services:

## User Request

1. Create an user

### Request Info
-	**Http Method:** POST
-	**URI:**	````http://host:port/ScrumGraph/sgrest/users````
-	**Content-Type:**	application/json
-	**Headers:**	*X-ScrumGraph-Header:* {"authToken": "*token_autogenerated*"}
-	**Entity Body:**
```json
    {
    "name": "Cristian",
    "lastName": "Peña",
    "email": "cristiancamilopena@gmail.com",
    "password": "asassaddsd",
    "roleDefault": "team-member",
    "isAdmin": false
    }
```

### Response

+	**Entity Body:**
```json
{
    "status": "ok",
    "errorMsg": "",
    "errorCode": 0,
    "response": {
        "user": {
            "id": 3,
            "name": "Cristian",
            "lastName": "Peña",
            "email": "cristiancamilopena@gmail.com",
            "roleDefault": "team-member",
            "available": "true",
            "isAdmin": "false",
            "authToken": "eea30a4cec3e24441302331"
        }
    }
}
```


2. Crear un Proyecto

**Pendiente de generar la documentación**
