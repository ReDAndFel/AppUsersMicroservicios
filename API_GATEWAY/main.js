const express = require('express')
const axios = require('axios')
const winston = require('winston')

const app = express()
app.use(express.json())
const port = process.env.PORT || 8086

// Configurar el logger
const logger = winston.createLogger({
    level: 'info',
    format: winston.format.json(),
    defaultMeta: { service: 'api-gateway' },
    transports: [
        new winston.transports.Console(),
    ]
})

//auth
app.post('/login', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.body)
    try {
        const response = await axios.post('http://172.18.0.10:8082/login', req.body)
        logger.info('Peticion al loguearse de api_users reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

//usuarios
// Ruta para la operación de registro
app.post('/usuarios/', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.headers)
    console.log(req.body)
    try {
        const response = await axios.post('http://172.18.0.10:8082/usuarios/', req.body)
        logger.info('Peticion a registro de api_users reenviada')
        res.send(response.data)
    } catch (error) {
        console.log(error)
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })

    }
})

app.post('/usuarios/recuperarContraseña/:email', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.headers)
    console.log(req.body)
    try {
        const email = req.params.email
        const token = req.headers.authorization
        const response = await axios.post(`http://172.18.0.10:8082/usuarios/recuperarContraseña/${email})`, req.body)
        logger.info('Peticion a recuperar contraseña de api_users reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

app.get('/usuarios/:id', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.headers)
    console.log(req.body)
    try {
        const id = req.params.id
        const token = req.headers.authorization
        const response = await axios.get(`http://172.18.0.10:8082/usuarios/${id}`, {
            headers: {
                Authorization: token
            }
        })
        logger.info('Peticion a obtener usuario de api_users reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})


app.put('/usuarios/:id', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.headers)
    console.log(req.body)
    try {
        const id = req.params.id
        const token = req.headers.authorization
        console.log("Id: " + id)
        console.log("token: " + token)
/*         const responseUsers = await axios.put(`http://172.18.0.10:8082/usuarios/${id}`, req.body, {
            headers: {
                Authorization: token
            }
        })
        logger.info('Peticion a actualizar usuario de api_users reenviada') */
        const responseProfiles = await axios.put(`http://api_profiles:8084/usuarios/${id}`, req.body)
        logger.info('Peticion a actualizar usuario de api_profiles reenviada')

        res.send(/* responseUsers.data, */ responseProfiles.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

app.put('/usuarios/actualizarContraseña/:id', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.headers)
    console.log(req.body)
    try {
        const id = req.params.id
        const response = await axios.put(`http://172.18.0.10:8082/usuarios/actualizarContraseña/${id}`, {
            headers: {
                Authorization: token
            }
        })
        logger.info('Peticion a actualizar la contraseña de api_users reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

app.delete('/usuarios/:id', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.headers)
    console.log(req.body)
    try {
        const id = req.params.id
        const response = await axios.delete(`http://172.18.0.10:8082/usuarios/${id}`, {
            headers: {
                Authorization: token
            }
        })
        logger.info('Peticion a eliminar de api_users reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

//api-logs

app.post('/logs', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.body)
    try {
        const response = await axios.post('http://api_logs:8083/logs', req.body)
        logger.info('Peticion a crear log de api_logs reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

app.get('/logs', async (req, res) => {
    try {
        const response = await axios.get(`http://api_logs:8083/logs`)
        logger.info('Peticion a obtener logs de api_logs reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

app.get('/logs/:aplication', async (req, res) => {

    try {
        const aplication = req.params.aplication
        const response = await axios.get(`http://api_logs:8083/logs/${aplication}`)
        logger.info('Peticion a obtener logs por aplicacion de api_logs reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

//api_health

app.post('/register-service', async (req, res) => {
    console.log("data de la peticion: ")
    console.log(req.body)
    try {
        const response = await axios.post('http://api_health:8053/register', req.body)
        logger.info('Peticion a registrar servicio de api_health reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})

app.get('/health', async (req, res) => {
    try {
        const response = await axios.get(`http://api_health:8053/health`)
        logger.info('Peticion a health por de api_health reenviada')
        res.send(response.data)
    } catch (error) {
        logger.error('Error al reenviar la peticion', error)
        res.status(500).json({ error: 'Error al reenviar la peticion' })
    }
})


//api gateway
app.get('/health-apigateway', async (req, res) => {
    res.send("Hello world")
})

app.listen(port, () => {
    console.log(`API Gateway listening at http://localhost:${port}`)
})