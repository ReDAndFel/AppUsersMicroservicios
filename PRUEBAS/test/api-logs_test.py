import allure
import requests

url = "http://api_logs:8083/"
   
def test_metricas():
    with allure.step("/metricas arroja código 200"):
        response = requests.get(url+"metrics")
        assert response.status_code == 200 

def test_logs():
    with allure.step("/logs arroja código 200"):
        response = requests.get(url+"logs")
        assert response.status_code == 200 

def test_health():
    with allure.step("/health arroja código 200"):
        response = requests.get(url+"health")
        assert response.status_code == 200 
    with allure.step("/health arroja código 200"):
        response = requests.get(url+"health/live")
        assert response.status_code == 200 
    with allure.step("/health arroja código 200"):
        response = requests.get(url+"health/ready")
        assert response.status_code == 200 