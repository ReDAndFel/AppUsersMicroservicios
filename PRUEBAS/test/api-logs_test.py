import allure
import requests
import json

url = "http://api_logs:8083/"
   
def test_metricas():
    with allure.step("Obtener las métricas"):
        response = requests.get(url+"metrics")
        assert response.status_code == 200 

def test_logs():
    with allure.step("Obtener los logs"):
        response = requests.get(url+"logs")
        assert response.status_code == 200 

def test_health():
    with allure.step("/health arroja código 200"):
        response = requests.get(url+"health")
        assert response.status_code == 200 
    with allure.step("/health/live arroja código 200"):
        response = requests.get(url+"health/live")
        assert response.status_code == 200 
    with allure.step("/health/ready arroja código 200"):
        response = requests.get(url+"health/ready")
        assert response.status_code == 200 
        
def test_obtener_log_app():
    with allure.step("Obtener log por aplicación"):
        urlFinal = f"{url}logs/API?from_date=2023-04-16&to_date=2023-12-31&tipo=Login"
        response = requests.get(urlFinal)
        assert response.status_code == 200
        
def test_crear_log():
    with allure.step("Crear un log"):
        urlFinal = f"{url}logs"
        payload = {
            "tipo":"Login",
            "aplicacion":"Api_users",
            "clase_modulo":"auth_controller",
            "resumen":"Se logueo un usuario",
            "descripcion":"Se logueo el usuario con correo test@gmail.com"
        }
        response = requests.post(urlFinal, data=json.dumps(payload), headers={"Content-Type": "application/json"})
        assert response.status_code == 200
        
