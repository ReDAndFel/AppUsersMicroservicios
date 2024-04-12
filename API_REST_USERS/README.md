Operaciones

Crear Usuario

Ruta: /usuarios Método HTTP: POST Datos requeridos: Formato: JSON Parámetros: nombre: String (obligatorio) - Nombre del usuario. email: String (obligatorio) - Correo electrónico del usuario. contraseña: String (obligatorio) - Contraseña del usuario. Posibles respuestas: Código 201: Usuario creado exitosamente. Devuelve el usuario creado en formato JSON. Código 400: Parámetros incorrectos o faltantes.

Obtener Usuario

Ruta: /usuarios/{id} Método HTTP: GET Parámetros: id: Integer (obligatorio) - ID del usuario. Posibles respuestas: Código 200: Usuario encontrado. Devuelve el usuario en formato JSON. Código 404: Usuario no encontrado.

Actualizar Usuario

Ruta: /usuarios/{id} Método HTTP: PUT Parámetros: id: Integer (obligatorio) - ID del usuario a actualizar. Datos requeridos: Formato: JSON Parámetros: nombre: String (opcional) - Nuevo nombre del usuario. email: String (opcional) - Nuevo correo electrónico del usuario. contraseña: String (opcional) - Nueva contraseña del usuario. Posibles respuestas: Código 200: Usuario actualizado exitosamente. Devuelve el usuario actualizado en formato JSON. Código 400: Parámetros incorrectos. Eliminar Usuario

Ruta: /usuarios/{id} Método HTTP: DELETE Parámetros: id: Integer (obligatorio) - ID del usuario a eliminar. Posibles respuestas: Código 204: Usuario eliminado exitosamente. Código 404: Usuario no encontrado. Seguridad Se implementará un mecanismo de autenticación mediante tokens JWT (JSON Web Tokens) para asegurar las operaciones de modificación (crear, actualizar, eliminar) de usuarios.

Especificación OpenAPI El archivo de especificación OpenAPI (anteriormente conocido como Swagger) se encuentra en el directorio /openapi_spec del repositorio. Este archivo describe la estructura de la API, incluyendo rutas, métodos, parámetros, esquemas de datos y posibles respuestas. Se ha incluido información adicional sobre la seguridad mediante JWT.
