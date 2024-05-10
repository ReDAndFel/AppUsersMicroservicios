package main

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	"os"

	//"log"
	"net/http"

	_ "github.com/go-sql-driver/mysql" // Importar el driver de MySQL
)

type User struct {
	ID             int    `json:"id"`
	PersonalURL    string `json:"personal_url"`
	Nickname       string `json:"nickname"`
	ContactPublic  bool   `json:"contact_public"`
	MailingAddress string `json:"mailing_address"`
	Bio            string `json:"bio"`
	Organization   string `json:"organization"`
	Country        string `json:"country"`
	SocialLinks    string `json:"social_links"`
}

type LogEntry struct {
	Message string `json:"message"`
}

var db *sql.DB
var logServiceURL = "http://api_logs:8083/log"

func main() {
	// Obtener las variables de entorno del docker-compose
	dbUser := os.Getenv("DB_PROFILE_USERNAME")
	dbPass := os.Getenv("DB_PROFILE_PASSWORD")
	dbHost := os.Getenv("DB_PROFILE_HOST")
	dbName := os.Getenv("DB_PROFILE_NAME")

	dsn := fmt.Sprintf("%s:%s@tcp(%s)/%s", dbUser, dbPass, dbHost, dbName)

	//Configurar la conexión a la base de dato MySQL
	var err error
	db, err = sql.Open("mysql", dsn)
	if err != nil {
		sendMsg(fmt.Sprintf("Error al conectar a la base de datos: %v", err))
		return
	}
	defer db.Close()

	// Iniciar el servidor HTTP
	http.HandleFunc("/users/profiles", handleUserProfile)
	sendMsg("Servicio de perfil de usuario iniciado")
	err = http.ListenAndServe(":8084", nil)
	if err != nil {
		sendMsg(fmt.Sprintf("Error al iniciar el servidor HTTP: %v", err))
	}
}

func handleUserProfile(w http.ResponseWriter, r *http.Request) {
	// Registrar la invocación al servicio en el sistema de logs
	sendMsg(fmt.Sprintf("%s %s", r.Method, r.URL.Path))

	// Obtener el ID del usuario autenticado (suponiendo que ya se autenticó)
	userID := 1 // Reemplazar con el mecanismo real de autenticación

	switch r.Method {
	case "GET":
		// Obtener el perfil del usuario de la base de datos
		user, err := getUserProfile(userID)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		// Enviar la respuesta en formato JSON
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(user)

	case "PUT":
		// Actualizar el perfil del usuario en la base de datos
		var user User
		err := json.NewDecoder(r.Body).Decode(&user)
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		err = updateUserProfile(userID, user)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		// Enviar una respuesta exitosa
		w.WriteHeader(http.StatusOK)

	default:
		http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
	}
}

func getUserProfile(userID int) (*User, error) {
	// Consultar la base de datos para obtener el perfil del usuario
	row := db.QueryRow("SELECT personal_url, nickname, contact_public, mailing_address, bio, organization, country, social_links FROM users WHERE id = ?", userID)

	var user User
	err := row.Scan(&user.PersonalURL, &user.Nickname, &user.ContactPublic, &user.MailingAddress, &user.Bio, &user.Organization, &user.Country, &user.SocialLinks)
	if err != nil {
		return nil, err
	}

	user.ID = userID
	return &user, nil
}

func updateUserProfile(userID int, user User) error {
	// Actualizar el perfil del usuario en la base de datos
	_, err := db.Exec("UPDATE users SET personal_url = ?, nickname = ?, contact_public = ?, mailing_address = ?, bio = ?, organization = ?, country = ?, social_links = ? WHERE id = ?", user.PersonalURL, user.Nickname, user.ContactPublic, user.MailingAddress, user.Bio, user.Organization, user.Country, user.SocialLinks, userID)
	return err
}

func sendMsg(message string) {
	logEntry := LogEntry{Message: message}
	jsonData, err := json.Marshal(logEntry)

	if err != nil {
		// Manejo de error en caso de falla al manejar el log en JSON
		return
	}

	_, err = http.Post(logServiceURL, "application/json", bytes.NewBuffer(jsonData))
	if err != nil {
		// Manejo de error en caso de fallo al enviar el log a la API de logs
		return
	}
}
