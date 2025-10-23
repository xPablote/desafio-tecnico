# CRUD Persona - Desafío Técnico

API REST para gestionar personas, con sincronización en Google Firestore y almacenamiento temporal persistente en H2. 
Utiliza el RUT como identificador único e implementa validaciones.
## Características

### Endpoints REST
La API ofrece los siguientes endpoints para la gestión de personas:

- **`POST /personas`**  
  Crea una nueva persona en la base de datos.
- **`GET /personas/{rut}`**  
  Recupera los detalles de una persona específica por su RUT.
- **`PUT /personas/{rut}`**  
  Actualiza los datos de una persona existente (el RUT es inmutable).
- **`DELETE /personas/{rut}`**  
  Elimina una persona de la base de datos por su RUT.

### Sincronización
- Las operaciones se almacenan temporalmente en H2 cuando Firestore no está disponible, devolviendo un código `HTTP 202` ("Operación guardada temporalmente").
- La sincronización se realiza automáticamente cada 30 segundos mediante el componente `SincronizadorPendientes`.
- Se han implementado mecanismos para evitar bucles infinitos, eliminando operaciones pendientes fallidas (por ejemplo, RUT no registrado o datos inválidos).
### Validaciones
- **RUT**: Validado con el algoritmo módulo 11 a través de `ValidadorRut`.
- **RUT Inmutable**: No se permite modificar el RUT al actualizar una persona.
- **JSON**: Validación de entrada mediante `Validator` y serialización/deserialización 

### Manejo de Excepciones
Todas las excepciones son gestionadas centralmente por `ManejadorExcepcionesGlobal`, asegurando un control uniforme:

- **`ExcepcionFirestoreNoDisponible`**: "Operación guardada temporalmente" (`HTTP 202`).
- **`ExcepcionRutDuplicado`**: "El RUT ya está registrado" (`HTTP 409`).
- **`ExcepcionRutInmutable`**: "No se permite actualizar el RUT de una persona" (`HTTP 400`).
- **`JsonProcessingException`**: "Error de formato en los datos" (`HTTP 400`).
- **`IllegalArgumentException`**: "Persona no encontrada" (`HTTP 404`).
- **`ExecutionException`**: "Error al ejecutar la operación en Firestore" (`HTTP 500`).
- **`InterruptedException`**: "La sincronización fue interrumpida" (`HTTP 500`).

### Códigos de Estado
- `200`: Operación exitosa.
- `202`: Operación guardada temporalmente.
- `400`: Solicitud inválida (por ejemplo, RUT inmutable).
- `404`: Recurso no encontrado.
- `409`: Conflicto (por ejemplo, RUT duplicado).
- `500`: Error interno del servidor.

### Logging
Los eventos, operaciones y errores se registran en el archivo `logs/crud-persona.log`, facilitando la depuración y el monitoreo.

### Documentación
La API incluye documentación interactiva en `/swagger-ui.html`, con ejemplos de JSON que utilizan el formato `dd-MM-yyyy` para el campo `fechaNacimiento`.

## Ejemplos de Solicitudes y Respuestas

- **POST /personas**:
    - Solicitud:
      ```json
      {
        "rut": "19911121-3",
        "nombre": "Julia",
        "apellido": "Sim",
        "fechaNacimiento": "01-01-1990",
        "direccion": {
          "calle": "Viva 123",
          "comuna": "Santiago",
          "region": "Metropolitana"
        }
      }

**Clonar**: ```bash git clone https://github.com/xPablote/desafio-tecnico.git``` 
- cd crud-persona