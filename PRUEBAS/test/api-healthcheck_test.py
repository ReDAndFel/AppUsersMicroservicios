import allure
import requests
import json

url_base = "http://api_health:8053/"

def test_registrar_microservicio():
    with allure.step("Se registra un microservicio"):
        
        url = f"{url_base}register"
        payload = {
            "name": "CastroAhuevado",
            "endpoint": "http://api_logs:8083/health",
            "frequency": 60,
            "emails": ["mauricio.burgosg@uqvirtual.edu.co"]
        }
        response = requests.post(url, data=json.dumps(payload), headers={"Content-Type": "application/json"})
        assert response.status_code == 200


def test_obtener_salud_general():
    with allure.step("Se obtiene la salud general"):
       url = f"{url_base}health" 
       response = requests.get(url)
       assert response.status_code == 200
