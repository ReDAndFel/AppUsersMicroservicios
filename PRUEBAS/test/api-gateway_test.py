import allure
import requests
import json

url_base = "http://api-gateway:8086/"
token = ""

def login():
    url = f"{url_base}/login"
    login_data = {
        "email": "mabuga23@gmail.com",
        "password": "andres1234"
    }
    response = requests.post(url, data=json.dumps(login_data), headers={"Content-Type": "application/json"})
    print(response.text)
    return response.text

def test_salud_gateway():
    with allure.step("Salud del gateway"):
        url = f"{url_base}health-apigateway"
        response = requests.get(url)
        print(response.text)
        assert response.status_code == 200

def test_login():
    with allure.step("Se loguea con éxito "):
        url = f"{url_base}/login"
        login_data = {
            "email": "mabuga23@gmail.com",
            "password": "andres1234"
        }
        response = requests.post(url, data=json.dumps(login_data), headers={"Content-Type": "application/json"})
        print(response.text)
        assert response.status_code == 200
        return response.text
        
def test_logs():
    with allure.step("Obtener los logs"):
        response = requests.get(url_base+"logs")
        print(response.text)
        assert response.status_code == 200 

def test_obtener_log_app():
    with allure.step("Obtener log por aplicación"):
        urlFinal = f"{url_base}logs/API?from_date=2023-04-16&to_date=2023-12-31&tipo=Login"
        response = requests.get(urlFinal)
        print(response.text)
        assert response.status_code == 200

def test_crear_log():
    with allure.step("Crear un log"):
        urlFinal = f"{url_base}logs"
        payload = {
            "tipo":"Login",
            "aplicacion":"Api_users",
            "clase_modulo":"auth_controller",
            "resumen":"Se logueo un usuario",
            "descripcion":"Se logueo el usuario con correo test@gmail.com"
        }
        response = requests.post(urlFinal, data=json.dumps(payload), headers={"Content-Type": "application/json"})
        print(response.text)
        assert response.status_code == 200

def test_obtener_usuario():
    with allure.step("Se obtiene usuario por id" ):
        url = f"{url_base}/usuarios/2"
        
        bearerToken = test_login()
        
        headers = {
            "Authorization": f" Bearer {bearerToken}"
        }
        
        response = requests.get(url, headers=headers)
        print(response.text)
        assert response.status_code == 200

def  test_actualizar_usuario():
    with allure.step("Se actualiza el usuario" ):
        url = f"{url_base}/usuarios/1"
        bearerToken = test_login()
        
        headers = {
            "Authorization": f" Bearer {bearerToken}",
            "Content-Type": "application/json"
        }
        
        payload = {
            "email": "johan@email.com"
        }
        
        response = requests.put(url, data=json.dumps(payload), headers=headers)
        print(response.text)
        assert response.status_code == 200
        
def test_registrar_usuario():
    with allure.step("Se crea un usuario"):
        url = f"{url_base}/usuarios/"
        payload = {
            "id": 0,
            "email": "example@gmail.com",
            "password": "12324124",
            "pagina_personal": "local.org",
            "apodo": "Puto enano",
            "contacto_publico": True,
            "direccion": "maplesoft",
            "biografia": "Hola mundo",
            "organizacion": "no se",
            "pais": "Colombia",
            "redes_soaiales": [{"Instagram": "red"}]
        }
        response = requests.post(url, data=json.dumps(payload), headers={"Content-Type": "application/json"})
        print(response.text)
        assert response.status_code == 200
        
def test_recuperar_clave():
    with allure.step("Se recupera la clave" ):
        url = f"{url_base}/usuarios/recuperarContrasenia/mabuga23@gmail.com"
        response = requests.post(url)
        assert response.status_code == 200
        
def test_registrar_servicio():
    with allure.step("Se registra un servicio" ):
        payload = {
            "name": "api-logs",
            "endpoint": "http://api_logs:8083/health",
            "frequency": 60,
            "emails": ["mauricio.burgosg@uqvirtual.edu.co"]
        }
        url = f"{url_base}/registrar-service"
        response = requests.post(url, data=json.dumps(payload), headers={"Content-Type": "application/json"})
        print(response.text)
        assert response.status_code == 200

def test_health():
    with allure.step("Salú" ):
        url = f"{url_base}/health"
        response = requests.get(url)
        print(response.text)
        assert response.status_code == 200
        
def test_eliminar_usuario():
    with allure.step("Eliminar usuario"):
        url = f"{url_base}/usuarios/2"
        bearerToken = test_login()
        
        headers = {
            "Authorization": f" Bearer {bearerToken}"
        }
        
        response = requests.delete(url, headers=headers)
        print(response.text)
        assert response.status_code == 200
        