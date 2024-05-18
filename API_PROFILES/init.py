import asyncio
import requests
import os
import json
import uvicorn
from nats.aio.client import Client as NATS
from fastapi import FastAPI, Response
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
""" from sqlalchemy.ext.declarative import declarative_base """
from sqlalchemy import Column, Integer, String, Boolean, Text, JSON

app = FastAPI()

# Configurar la base de datos
SQLALCHEMY_DATABASE_URI = f"mysql+mysqlconnector://{os.environ['DB_PROFILE_USERNAME']}:{os.environ['DB_PROFILE_PASSWORD']}@{os.environ['DB_PROFILE_HOST']}/{os.environ['DB_PROFILE_NAME']}"
engine = create_engine(SQLALCHEMY_DATABASE_URI)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Configuración de conexión a NATS
nats_host = os.getenv('HOST_NATS', 'nats')
nats_port = int(os.getenv('PORT_NATS', 4222))

json_data = {
    "tipo": "",
    "aplicacion": "",
    "clase_modulo": "",
    "resumen": "",
    "descripcion": "" 

}

# Dirección de la API de logs
LOGS_API_URL = 'http://api_logs:8083/logs'

class profiles(Base):
    __tablename__='profiles'

    id = Column(Integer, primary_key=True)
    pagina_personal = Column(String(255), nullable=True)
    apodo = Column(String(50), nullable=True)
    contacto_publico = Column(Boolean, default=False)
    direccion = Column(String(255), nullable=True)
    biografia = Column(Text, nullable=True)
    organizacion = Column(String(100), nullable=True)
    pais = Column(String(50), nullable=True)
    redes_sociales = Column(JSON, nullable=True)

# Función para crear un nuevo perfil de usuario
def crear_perfil_usuario(datos_profile):
    db = SessionLocal()
    # Eliminamos los datos innecesarios para esta tabla
    datos_profile.pop('email', None)
    datos_profile.pop('password', None)

    # Crear un nuevo registro en la tabla Usuario
    try:
        nuevo_usuario = profiles(**datos_profile)
        db.add(nuevo_usuario)
        db.commit()
        db.refresh(nuevo_usuario)
    except Exception as e:
        db.rollback()
        raise e
    finally:
        db.close()
        
    return nuevo_usuario

def enviar_log(json_data):
    try:
        headers = {'Content-Type': 'application/json'}
        requests.post(LOGS_API_URL, json=json_data, headers=headers)
    except requests.exceptions.RequestException as e:
        print(f'Error al enviar log: {e}')

@app.put('/usuarios/{id}', response_model=dict)
def actualizar_perfil(id: int, datos: dict, response: Response):
    json_data['tipo'] = "Actualizar"
    json_data['aplicacion'] = "Api_profiles"
    json_data['clase_modulo'] = "inti"
    json_data['resumen'] = f'Servicio de actualizar usuario {id} invocado'
    json_data['descripcion'] = "Se actualizó el perfil del usuario"

    enviar_log(json_data)

    db = SessionLocal()
    usuario = db.query(profiles).get(id)
    if not usuario:
        response.status_code = 404
        return {'mensaje': 'Usuario no encontrado'}

    usuario.pagina_personal = datos.get('pagina_personal', usuario.pagina_personal)
    usuario.apodo = datos.get('apodo', usuario.apodo)
    usuario.contacto_publico = datos.get('contacto_publico', usuario.contacto_publico)
    usuario.direccion = datos.get('direccion', usuario.direccion)
    usuario.biografia = datos.get('biografia', usuario.biografia)
    usuario.organizacion = datos.get('organizacion', usuario.organizacion)
    usuario.pais = datos.get('pais', usuario.pais)
    usuario.redes_sociales = datos.get('redes_sociales', usuario.redes_sociales)

    db.commit()
    db.close()

    return {'mensaje': 'Perfil actualizado correctamente'}

async def nats_listener():
    # Configurar el suscriptor de mensajes de NATS
    nc = NATS()
    await nc.connect(servers=[f"nats://{nats_host}:{nats_port}"])

    async def callback(msg):
        try:
            datos_profile = json.loads(msg.data.decode())
            crear_perfil_usuario(datos_profile)
        except Exception as e:
            print(f"Error al procesar el mensaje de logs: {e}")

    await nc.subscribe("profile", cb=callback)
    print('Esperando eventos de registro de usuarios. Presiona CTRL+C para salir.')

async def main():
    # Crear tareas asincronas setup_nats y uvicorn
    configuracion = uvicorn.Config(app, host='0.0.0.0', port = int(os.environ['PORT']), log_level='info')
    servidor = uvicorn.Server(configuracion)
    await asyncio.gather(
        nats_listener(),
        servidor.serve()
    )

if __name__ == '__main__':
    asyncio.run(main())
    