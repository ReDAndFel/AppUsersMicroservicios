import datetime
import json
import os
import time
import threading
import asyncio
import uvicorn
from urllib.parse import urlparse
from fastapi import FastAPI, Response, Depends, Query, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from nats import NATS
from nats.aio.client import Msg
from nats.aio.errors import ErrConnectionClosed, ErrTimeout, ErrNoServers
from sqlalchemy import Column, String, BigInteger, DateTime, create_engine, and_
from sqlalchemy.orm import sessionmaker, declarative_base, Session
from prometheus_client import make_wsgi_app, Counter, Gauge, Histogram, generate_latest
from werkzeug.middleware.dispatcher import DispatcherMiddleware
from typing import Optional, Dict, Any
from contextlib import asynccontextmanager


app = FastAPI() 

SQLALCHEMY_DATABASE_URI = f"mysql+mysqlconnector://{os.environ['DB_USERNAME']}:{os.environ['DB_PASSWORD']}@{os.environ['DB_HOST']}/{os.environ['DB_NAME']}"
engine = create_engine(SQLALCHEMY_DATABASE_URI)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Configuración de conexión a NATS
nats_host = os.getenv('NATS_HOST', 'nats')
nats_port = int(os.getenv('NATS_PORT', 4222))

# Configurar las métricas
requests_total = Counter('requests_total', 'Total requests')
requests_by_status = Counter('requests_by_status', 'Requests by status', ['status'])
requests_latency = Histogram('requests_latency', 'Request latency (seconds)')

# Envolver la aplicación Flask con el middleware de Prometheus
""" app = make_wsgi_app(app, metrics_endpoint='/metrics')
app = DispatcherMiddleware(app.wsgi_app, {'/metrics': app}) """
"""app.config["SQLALCHEMY_DATABASE_URI"] = (
    f"mysql+mysqlconnector://DBLOGS:DBLOGS@localhost:13302/api_logs"
)"""

nats_client = NATS()

MAX_RETRIES = 3  # Número máximo de reintentos
RETRY_DELAY = 2  # Tiempo de espera en segundos entre reintentos

START_TIME = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global START_TIME
    START_TIME = datetime.datetime.now()
    yield

app = FastAPI(lifespan=lifespan)

class Log(Base):
    __tablename__ = "logs"  # Especifica el nombre de la tabla

    id = Column(BigInteger, primary_key=True)
    created_at = Column(DateTime, nullable=True, default=datetime.datetime.now)
    updated_at = Column(
        DateTime,
        nullable=True,
        default=datetime.datetime.now,
        onupdate=datetime.datetime.now,
    )
    tipo = Column(String(255), nullable=False)
    aplicacion = Column(String(255), nullable=False)
    clase_modulo = Column(String(255), nullable=False)
    resumen = Column(String(255), nullable=False)
    descripcion = Column(String(255), nullable=False)

@app.get('/metrics')
def metrics(response: Response):
    # Incrementar métricas
    requests_total.inc()

    # Generar métricas en formato Prometheus
    prometheus_metrics = generate_latest()

    # Retornar las métricas
    response.headers['Content-Type'] = 'text/plain; version=0.0.4'
    return prometheus_metrics

# Dependencia para obtener la sesión de la base de datos
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/logs", response_model=dict)
def get_logs(page: int = Query(1, description="Número de página"),
             per_page: int = Query(10, description="Items por página"),
             start_date: Optional[str] = Query(None, description="Fecha de inicio"),
             end_date: Optional[str] = Query(None, description="Fecha de fin"),
             tipo: Optional[str] = Query(None, description="Log type"), 
             db: Session = Depends(get_db)
):
    
    requests_total.inc()
    status=200 
    requests_by_status.labels(status).inc()
    start_time = time.time()

    query = db.query(Log)

    if start_date and end_date:
        query = query.filter(
            and_(Log.created_at >= start_date, Log.created_at <= end_date)
        )

    if tipo:
        query = query.filter_by(tipo=tipo)

    logs = query.order_by(Log.created_at).offset((page -1)*per_page).limit(per_page).all()

    total = query.count()
    pages = (total + per_page -1) // per_page # Calcular el número total de páginas

    result = [
        {
            "id": log.id,
            "created_at": log.created_at,
            "updated_at": log.updated_at,
            "tipo": log.tipo,
            "aplicacion": log.aplicacion,
            "clase_modulo": log.clase_modulo,
            "resumen": log.resumen,
            "descripcion": log.descripcion,
        }
        for log in logs
    ]

    latency = time.time() - start_time
    requests_latency.observe(latency)

    return {"logs": result, "total": total, "pages": pages}

@app.get("/logs/{application}")
def get_logs_by_application(application: str, 
                            page: int = Query(1, description="Número de página"),
                            per_page: int = Query(10, description="Items por página"),
                            start_date: Optional[str] = Query(None, description="Fecha de inicio"),
                            end_date: Optional[str] = Query(None, description="Fecha de fin"),
                            tipo: Optional[str] = Query(None, description="Log type"), 
                            db: Session = Depends(get_db)
):
    
    requests_total.inc()
    status=200 
    requests_by_status.labels(status).inc()
    start_time = time.time()

    query = db.query(Log).filter_by(aplicacion=application)

    if start_date and end_date:
        query = query.filter(
            and_(Log.created_at >= start_date, Log.created_at <= end_date)
        )

    if tipo:
        query = query.filter_by(tipo=tipo)

    total_logs = query.count()
    logs = query.order_by(Log.created_at).offset((page - 1) * per_page).limit(per_page).all()

    result = [
        {
            "id": log.id,
            "created_at": log.created_at,
            "tipo": log.tipo,
            "aplicacion": log.aplicacion,
            "clase_modulo": log.clase_modulo,
            "resumen": log.resumen,
            "descripcion": log.descripcion,
        }
        for log in logs
    ]

    pages = (total_logs + per_page - 1) // per_page

    latency = time.time() - start_time
    requests_latency.observe(latency)

    return {"logs": result, "total": total_logs, "pages": pages}

# Definición del modelo de entrada con Pydantic
class LogCreate(BaseModel):
    tipo: str
    aplicacion: str
    clase_modulo: str
    resumen: str
    descripcion: str

@app.post("/logs")
def create_log(log_data: LogCreate, db: Session = Depends(get_db)):
    requests_total.inc()
    status=200 
    requests_by_status.labels(status).inc()
    start_time = time.time()

    log = Log(
        tipo=log_data.tipo,
        aplicacion=log_data.aplicacion,
        clase_modulo=log_data.clase_modulo,
        resumen=log_data.resumen,
        descripcion=log_data.descripcion,
    )

    db.add(log)
    db.commit()
    db.refresh(log)

    latency = time.time() - start_time
    requests_latency.observe(latency)

    return JSONResponse(status_code=201, content={"message": "Log creado exitosamente", "log_id": log.id})

# Obtener el nombre de la base de datos desde la URI de la base de datos
def get_database_name():
    db_uri = SQLALCHEMY_DATABASE_URI
    parsed_uri = urlparse(db_uri)
    database_name = parsed_uri.path.lstrip('/')
    return database_name
    
def verificar_conexion_db():
    retries = 0
    while retries < MAX_RETRIES:
        try:
            engine.connect().close()
            return 'UP'
        except Exception as e:
            retries += 1
            print(f"Error al verificar la conexión a la base de datos. Reintento {retries}/{MAX_RETRIES}")
            time.sleep(RETRY_DELAY)
        
    return 'DOWN'

async def verificar_conexion_nats():
    try:
        nc = NATS()
        await nc.connect(servers=[f"nats://{nats_host}:{nats_port}"])
        await nc.close()
        return "UP"
    except Exception as e:
        print(f"Error al verificar la conexión a NATS: {e}")
        return 'DOWN'

@app.get('/health')
async def health_check():
    global START_TIME
    now = datetime.datetime.now()
    uptime = str(now - START_TIME)
    # Obtener el nombre de la base de datos
    database_name = get_database_name()
    db_status = verificar_conexion_db()
    nats_status = await verificar_conexion_nats()

    if db_status == 'UP' or nats_status == 'UP':
        status = 'UP'
    else:
        status = 'DOWN'

    health_data = {
        "statusOverall": status,
        "version": "1.0.0",
        "checks": [
            {
                "db": {
                    "name": database_name,
                    "status": db_status,
                    "from": uptime
                },
            },
            {
                "nats": {
                    "status": nats_status,
                    "from": uptime
                }
            }
        ]
    }

    return health_data

@app.get('/health/live', response_model=dict)
async def liveness_check():
    # Agrega aquí la lógica para verificar si la aplicación está ejecutándose
    return {"status": "UP"}

@app.get('/health/ready', response_model=dict)
async def readiness_check():
    db_status = verificar_conexion_db()
    nats_status = await verificar_conexion_nats()

    if db_status == 'UP' or nats_status == 'UP':
        return {"status": "UP"}
    else:
        return {"status": "DOWN"}

async def setup_nats():
    # Configurar el cliente NATS
    nc = NATS()
    try:
        await nc.connect(servers=[f'nats://{nats_host}:{nats_port}'])
        print('¡Conectado a NATS!')
    except Exception as e:
        print(f"Error al conectar con NATS: {e}")

    # Suscribirse al tema 'logs' y definir la función de controlador
    async def handle_logs(msg: Msg):
        print("Recibiendo mensaje por el tema logs...")
        data = msg.data.decode()

        db = SessionLocal()
        # Suponiendo que los mensajes son objetos JSON
        try: 
            log_data = json.loads(data)
    
            print("Mensaje recibido!")
            # Crear un registro de log en la base de datos
            log = Log(
                tipo=log_data["tipo"],
                aplicacion=log_data["aplicacion"],
                clase_modulo=log_data["clase_modulo"],
                resumen=log_data["resumen"],
                descripcion=log_data["descripcion"],
            )
            db.add(log)
            db.commit()
        except Exception as e:
            print("Error al procesar el mensaje de logs: ", e)
        
    await nc.subscribe(os.environ["NATS_TEMA"], cb=handle_logs)

async def main():
    # Crear tareas asincronas setup_nats y uvicorn
    config = uvicorn.Config(app, host='0.0.0.0', port = int(os.environ['PORT']), log_level="info")
    server = uvicorn.Server(config)
    await asyncio.gather(
        setup_nats(),
        server.serve()
    )

if __name__ == "__main__":
    asyncio.run(main())
    