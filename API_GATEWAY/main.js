const express = require('express');
const axios = require('axios');
const winston = require('winston');

const app = express();
const port = 8086;

// Configurar el logger
const logger = winston.createLogger({
    level: 'info',
    format: winston.format.json(),
    defaultMeta: { service: 'api-gateway' },
    transports: [
        new winston.transports.Console(),
    ]
});

// Ruta para la operación de autenticación
app.post('/auth', async (req, res) => {
    try {
        const response = await axios.post('http://api_users/login', req.body);
        logger.error('Error al reenviar la peticion de autenticacion a api_users', error);
        res.json(response.data);
    } catch (error) {
        logger.error('Error forwarding authentication request', error);
        res.status(500).json({ error: 'Error al reenviar la peticion de autenticacion a api_users' });
    }
});

// Ruta para la operación de registro
app.post('/register', async (req, res) => {
    try {
        const response = await axios.post('http://api_users/register', req.body);
        logger.info('Peticion a registro de api_users reenviada');
        res.json(response.data);
    } catch (error) {
        logger.error('Error al reenviar la peticion de registro a api_users', error);
        res.status(500).json({ error: 'Error al reenviar la peticion de registro a api_users' });
    }
});
app.get('/health', async (req, res) => {
    res.send("Hello world")
});

app.listen(port, () => {
    console.log(`API Gateway listening at http://localhost:${port}`);
});