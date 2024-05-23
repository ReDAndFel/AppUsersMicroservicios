import allure
import requests
import json

url_base = "http://api_users:8082"
token = ""

def test_login_exito():
    with allure.step("Se loguea con éxito "):
        url = f"{url_base}/login"
        login_data = {
        "email": "andresf.castroc1@uqvirtual.edu.co",
        "password": "andres1234"
        }
        response = requests.post(url, data=json.dumps(login_data), headers={"Content-Type": "application/json"})
        assert response.status_code == 200