import allure
import requests
import json

url = "http://api_profiles:8084/"

def test_actualizar_perfil():
    with allure.step("Se actualiza un perfil"):
        urlFinal = f"{url}usuarios/3"
        payload = {
            "pagina_personal": "https://ejemplo.com/nuevo-perfil",
            "apodo": "Oompa Loompa",
            "contacto_publico": True,
            "direccion": "Nueva Dirección 123",
            "biografia": "Esta es mi nueva biografía",
            "organizacion": "Acme Corp",
            "pais": "Países Bajos",
            "redes_sociales": {
            "twitter": "https://twitter.com/nuevo-usuario",
            "facebook": "https://facebook.com/nuevo-usuario"
            }
        }
        response = requests.put(url, data=json.dumps(payload), headers={"Content-Type": "application/json"})