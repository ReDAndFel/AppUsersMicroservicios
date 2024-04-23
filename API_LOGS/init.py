import datetime
import json
import os
import asyncio
import nats
from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import and_

app = Flask(__name__)
#mysql+mysqlconnector://usuario_database:password_database@host_database/name_database
app.config['SQLALCHEMY_DATABASE_URI'] = f"mysql+mysqlconnector://{os.environ['DB_USERNAME']}:{os.environ['DB_PASSWORD']}@{os.environ['DB_HOST']}/{os.environ['DB_NAME']}"
db = SQLAlchemy(app)
"""
async def natsmain():
    # Connect to NATS!
    nc = await nats.connect(os.environ['NATS_URL'])

    # Receive messages on 'foo'
    sub = await nc.subscribe(os.environ['NATS_TEMA'])

    # Process a message
    msg = await sub.next_msg()
    print("Received:", msg)

    # Make sure all published messages have reached the server
    await nc.flush()

    # Close NATS connection
    await nc.close()
"""
async def handle_message(msg):
     # Recibe el mensaje en binario
    message_data = msg.data

    # Convierte el mensaje binario en un objeto JSON
    try:
        log_data = json.loads(message_data.decode())
    except json.JSONDecodeError:
        print("Error al decodificar mensaje JSON")
        return

    # Procesa el objeto JSON
    log = Log(
        tipo=log_data["tipo"],
        aplicacion=log_data["aplicacion"],
        clase_modulo=log_data["clase_modulo"],
        resumen=log_data["resumen"],
        descripcion=log_data["descripcion"]
    )

    db.session.add(log)
    db.session.commit()

    print(f"Mensaje procesado: {log_data}")

# Crear un bucle de eventos
loop = asyncio.new_event_loop()
asyncio.set_event_loop(loop)

print("url de nats es: ", os.environ['NATS_URL'] )
print("Tema de nats es: ", os.environ['NATS_TEMA'] )

# Conectar a NATS
nc = loop.run_until_complete(nats.connect(os.environ['NATS_URL']))

# Suscribirse al tema
sub = loop.run_until_complete(nc.subscribe(os.environ['NATS_TEMA'], cb=handle_message))

class Log(db.Model):
    
    __tablename__ = 'logs'  # Especifica el nombre de la tabla
    id = db.Column(db.BigInteger, primary_key=True)
    created_at = db.Column(db.DateTime, nullable=True, default=datetime.datetime.now)
    updated_at = db.Column(db.DateTime, nullable=True, default=datetime.datetime.now, onupdate=datetime.datetime.now)
    tipo = db.Column(db.String(255), nullable=False)
    aplicacion = db.Column(db.String(255), nullable=False)
    clase_modulo = db.Column(db.String(255), nullable=False)
    resumen = db.Column(db.String(255), nullable=False)
    descripcion = db.Column(db.String(255), nullable=False)

@app.route('/logs', methods=['GET'])
def get_logs():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 10, type=int)
    start_date = request.args.get('from_date')
    end_date = request.args.get('to_date')
    tipo = request.args.get('tipo')

    query = Log.query

    if start_date and end_date:
        query = query.filter(and_(Log.created_at >= start_date, Log.created_at <= end_date))

    if tipo:
        query = query.filter_by(tipo=tipo)

    logs = query.order_by(Log.created_at).paginate(page=page, per_page=per_page, error_out=False)

    result = []
    for log in logs.items:
        result.append({
            'id': log.id,
            'created_at': log.created_at,
            'updated_at': log.updated_at,
            'tipo': log.tipo,
            'aplicacion': log.aplicacion,
            'clase_modulo': log.clase_modulo,
            'resumen': log.resumen,
            'descripcion': log.descripcion
            
        })

    return jsonify({'logs': result, 'total': logs.total, 'pages': logs.pages})

@app.route('/logs/<application>', methods=['GET'])
def get_logs_by_application(application):
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 10, type=int)
    start_date = request.args.get('start_date')
    end_date = request.args.get('end_date')
    tipo = request.args.get('tipo')

    query = Log.query.filter_by(aplicacion=application)

    if start_date and end_date:
        query = query.filter(and_(Log.created_at >= start_date, Log.created_at <= end_date))

    if tipo:
        query = query.filter_by(tipo=tipo)

    logs = query.order_by(Log.created_at).paginate(page=page, per_page=per_page, error_out=False)

    result = []
    for log in logs.items:
        result.append({
            'id': log.id,
            'created_at': log.created_at,
            'tipo': log.tipo,
            'aplicacion': log.aplicacion,
            'clase_modulo': log.clase_modulo,
            'resumen': log.resumen,
            'descripcion': log.descripcion
        })

    return jsonify({'logs': result, 'total': logs.total, 'pages': logs.pages})

@app.route('/logs', methods=['POST'])
def create_log():
    data = request.json

    log = Log(
        tipo=data['tipo'],
        aplicacion=data['aplicacion'],
        clase_modulo=data['clase_modulo'],
        resumen=data['resumen'],
        descripcion=data['descripcion']
    )

    db.session.add(log)
    db.session.commit()

    return jsonify({'message': 'Log creado exitosamente'}), 201

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0' , port=os.environ['PORT'])
    #asyncio.run(natsmain())
    try:
        loop.run_forever()
    finally:
        loop.close()
