import asyncio
import requests
import os
import json
from nats.aio.client import Client as NATS
from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config["SQLALCHEMY_DATABASE_URI"] = (
    f"mysql+mysqlconnector://{os.environ['DB_PROFILE_USERNAME']}:{os.environ['DB_PROFILE_PASSWORD']}@{os.environ['DB_PROFILE_HOST']}/{os.environ['DB_PROFILE_NAME']}"
)
db = SQLAlchemy(app)

# Configuración de conexión a NATS
nats_host = 'nats'
nats_port = 4222

json_data = {
    "tipo": "",
    "aplicacion": "",
    "clase_modulo": "",
    "resumen": "",
    "descripcion": "" 

}

# Dirección de la API de logs
LOGS_API_URL = 'http://api_logs:8083/logs'

class profiles(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    pagina_personal = db.Column(db.String(255), nullable=True)
    apodo = db.Column(db.String(50), nullable=True)
    contacto_publico = db.Column(db.Boolean, default=False)
    direccion = db.Column(db.String(255), nullable=True)
    biografia = db.Column(db.Text, nullable=True)
    organizacion = db.Column(db.String(100), nullable=True)
    pais = db.Column(db.String(50), nullable=True)
    redes_sociales = db.Column(db.JSON, nullable=True)

# Función para crear un nuevo perfil de usuario
def crear_perfil_usuario(datos_profile):
    # Crear un nuevo registro en la tabla Usuario
    nuevo_usuario = profiles(
        id=datos_profile['id'],
        pagina_personal=datos_profile.get('pagina_personal'),
        apodo=datos_profile.get('apodo'),
        contacto_publico=datos_profile.get('contacto_publico', False),
        direccion=datos_profile.get('direccion'),
        biografia=datos_profile.get('biografia'),
        organizacion=datos_profile.get('organizacion'),
        pais=datos_profile.get('pais'),
        redes_sociales=datos_profile.get('redes_sociales')
    )
    db.session.add(nuevo_usuario)
    db.session.commit()

def enviar_log(json_data):
    try:
        headers = {'Content-Type': 'application/json'}
        requests.post(LOGS_API_URL, json=json_data, headers=headers)
    except requests.exceptions.RequestException as e:
        print(f'Error al enviar log: {e}')

@app.route('/usuarios/<int:id>', methods=['PUT'])
def actualizar_perfil(id):
    json_data['tipo'] = "Actualizar"
    json_data['aplicacion'] = "Api_profiles"
    json_data['clase_modulo'] = "inti"
    json_data['resumen'] = f'Servicio de actualizar usuario {id} invocado'
    json_data['descripcion'] = "Se actualizó el perfil del usuario"

    enviar_log(json_data)

    usuario = profiles.query.get(id)
    if not usuario:
        return jsonify({'mensaje': 'Usuario no encontrado'}), 404
    
    datos = request.get_json()

    usuario.pagina_personal = datos.get('pagina_personal', usuario.pagina_personal)
    usuario.apodo = datos.get('apodo', usuario.apodo)
    usuario.contacto_publico = datos.get('contacto_publico', usuario.contacto_publico)
    usuario.direccion = datos.get('direccion', usuario.direccion)
    usuario.biografia = datos.get('biografia', usuario.biografia)
    usuario.organizacion = datos.get('organizacion', usuario.organizacion)
    usuario.pais = datos.get('pais', usuario.pais)
    usuario.redes_sociales = datos.get('redes_sociales', usuario.redes_sociales)

    db.session.commit()

    return jsonify({'mensaje': 'Perfil actualizado correctamente'})

async def main():
    # Configurar el suscriptor de mensajes de NATS
    nc = NATS()
    await nc.connect(servers=[f"{nats_host}:{nats_port}"])

    async def callback(msg):
        datos_profile = json.loads(msg.data.decode())
        crear_perfil_usuario(datos_profile)

    await nc.subscribe("Cola_registro_usuarios", cb=callback)
    print('Esperando eventos de registro de usuarios. Presiona CTRL+C para salir.')

    while True:
        try:
            await asyncio.sleep(5)
        except KeyboardInterrupt:
            break
    
    await nc.close()

if __name__ == '__main__':
    asyncio.run(main())
    app.run(host='0.0.0.0', port=os.environ['PORT'], debug=True)
    