import allure

from pruebas_pytest import get_message


def test_pruebas_pytest():
    with allure.step("Welcome to Allure Report!"):
        assert get_message() == "Hello from pruebas_pytest!"
