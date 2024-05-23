import allure
import requests
import json

url_base = "http://172.18.0.15:8082"
token = ""

def login():
    url = f"{url_base}/login"
    login_data = {
    "email": "andresf.castroc1@uqvirtual.edu.co",
        "password": "andres1234"
    }
    response = requests.post(url, data=json.dumps(login_data), headers={"Content-Type": "application/json"})
    return response.text

def test_login_exito():
    with allure.step("Se loguea con éxito "):
        url = f"{url_base}/login"
        login_data = {
        "email": "andresf.castroc1@uqvirtual.edu.co",
        "password": "andres1234"
        }
        response = requests.post(url, data=json.dumps(login_data), headers={"Content-Type": "application/json"})
        assert response.status_code == 200
        
def test_health():
    with allure.step("Se verifica la salud: "):
        url = f"{url_base}/actuator/health"
        response = requests.get(url)
        assert response.status_code == 200
        
def test_registrar_usuario():
    with allure.step("Se crea un usuario"):
        url = f"{url_base}/usuarios"
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
        assert response.status_code == 200
        
        
def test_obtener_usuario():
    with allure.step("Se obtiene usuario por id" ):
        url = f"{url_base}/usuarios/36"
        
        bearerToken = login()
        
        headers = {
            "Authorization": f"Bearer {bearerToken}"
        }
        
        response = requests.get(url, headers=headers)
        assert response.status_code == 200
        
    with allure.step("Se obtienen la pagina del usuario" ):
        url = f"{url_base}/usuarios/list?page=0&size=10&sort=id,asc"
        bearerToken = login()
        
        headers = {
            "Authorization": f"Bearer {bearerToken}"
        }
        
        response = requests.get(url, headers=headers)
        assert response.status_code == 200
        
def  test_actualizar_usuario():
    with allure.step("Se actualiza el usuario" ):
        url = f"{url_base}/usuarios/1"
        bearerToken = login()
        
        headers = {
            "Authorization": f"Bearer {bearerToken}",
            "Content-Type": "application/json"
        }
        
        payload = {
        "email": "johan@email.com"
        }
        
        response = requests.put(url, data=json.dumps(payload))
        assert response.status_code == 200
    
        
def test_recuperar_clave():
    with allure.step("Se recupera la clave" ):
        url = f"{url_base}/usuarios/recuperarContraseña/mabuga23@gmail.com"
        response = requests.post(url)
        assert response.status_code == 200