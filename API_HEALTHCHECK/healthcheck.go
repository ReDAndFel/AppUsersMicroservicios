package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/smtp"
	"os"
	"strconv"
	"sync"
	"time"
)

type MicroserviceHealth struct {
	Name      string    `json:"name"`
	Status    string    `json:"status"`
	Data      any       `json:"data,omitempty"`
	LastCheck time.Time `json:"lastCheck"`
}

type Microservice struct {
	Name      string   `json:"name"`
	Endpoint  string   `json:"endpoint"`
	Frequency int      `json:"frequency"`
	Emails    []string `json:"emails"`
}

var microservices = make(map[string]Microservice)
var healthStatus = make(map[string]MicroserviceHealth)
var mutex = &sync.Mutex{}

// Configuración SMTP para envío de correos
var smtpServer = os.Getenv("SMTP_SERVER")
var smtpPort, _ = strconv.Atoi(os.Getenv("SMTP_PORT"))
var smtpUser = os.Getenv("SMTP_USER")
var smtpPassword = os.Getenv("SMTP_PASSWORD")

func checkInitialMicroserviceHealth(microservice Microservice) MicroserviceHealth {
	resp, err := http.Get(microservice.Endpoint)
	if err != nil {
		return MicroserviceHealth{
			Name:      microservice.Name,
			Status:    "DOWN",
			Data:      err.Error(),
			LastCheck: time.Now(),
		}
	}
	defer resp.Body.Close()

	var health struct {
		Checks        []map[string]map[string]interface{} `json:"checks"`
		OverallStatus string                              `json:"statusOverall"`
	}
	err = json.NewDecoder(resp.Body).Decode(&health)
	if err != nil {
		return MicroserviceHealth{
			Name:      microservice.Name,
			Status:    "DOWN",
			Data:      err.Error(),
			LastCheck: time.Now(),
		}
	}

	var status string
	if health.OverallStatus == "UP" {
		status = "UP"
	} else {
		status = "DOWN"
	}

	healthStatus := MicroserviceHealth{
		Name:      microservice.Name,
		Status:    status,
		Data:      health,
		LastCheck: time.Now(),
	}

	return healthStatus
}

func registerMicroservice(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var microservice Microservice
	err := json.NewDecoder(r.Body).Decode(&microservice)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	mutex.Lock()
	defer mutex.Unlock()

	microservices[microservice.Name] = microservice
	health := checkInitialMicroserviceHealth(microservice)
	healthStatus[microservice.Name] = health
}

func getMicroserviceHealth(w http.ResponseWriter, r *http.Request) {
	name := r.URL.Path[len("/health/"):]
	if name == "" {
		http.Error(w, "Missing microservice name", http.StatusBadRequest)
		return
	}

	mutex.Lock()
	defer mutex.Unlock()

	health, ok := healthStatus[name]
	if !ok {
		http.Error(w, fmt.Sprintf("Microservices %s not found", name), http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(health)
}

func getOverallHealth(w http.ResponseWriter, r *http.Request) {
	mutex.Lock()
	defer mutex.Unlock()

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(healthStatus)
}

func checkMicroserviceHealth(name string, endpoint string, frecuency int) {
	ticker := time.NewTicker(time.Duration(frecuency) * time.Second)
	quit := make(chan struct{})
	fmt.Println("Entró en la función check", ticker)
	go func() {
		for {
			select {
			case <-ticker.C:
				resp, err := http.Get(endpoint)
				fmt.Println(resp)
				if err != nil {
					mutex.Lock()
					healthStatus[name] = MicroserviceHealth{
						Name:      name,
						Status:    "DOWN",
						Data:      err.Error(),
						LastCheck: time.Now(),
					}
					notifyStatusChange(name, "DOWN", microservices[name].Emails)
					mutex.Unlock()
					continue
				}
				defer resp.Body.Close()

				var health MicroserviceHealth
				err = json.NewDecoder(resp.Body).Decode(&health)
				if err != nil {
					mutex.Lock()
					healthStatus[name] = MicroserviceHealth{
						Name:      name,
						Status:    "DOWN",
						Data:      err.Error(),
						LastCheck: time.Now(),
					}
					notifyStatusChange(name, "DOWN", microservices[name].Emails)
					mutex.Unlock()
					continue
				}

				mutex.Lock()
				if health.Status == "DOWN" {
					notifyStatusChange(name, "DOWN", microservices[name].Emails)
				}

				if health.Status != healthStatus[name].Status {
					notifyStatusChange(name, health.Status, microservices[name].Emails)
				}
				health.LastCheck = time.Now()
				healthStatus[name] = health
				mutex.Unlock()
			case <-quit:
				ticker.Stop()
				return
			}
		}
	}()
}

func notifyStatusChange(name, status string, emails []string) {
	subject := fmt.Sprintf("Microservice %s status changed to %s", name, status)
	body := fmt.Sprintf("The microservice %s has changed its status to %s.", name, status)
	sendEmail(emails, subject, body)
}

func sendEmail(to []string, subject, body string) {
	msg := fmt.Sprintf("From: %s\r\nTo: %s\r\nSubject: %s\r\n\r\n%s", smtpUser, to, subject, body)
	auth := smtp.PlainAuth("", smtpUser, smtpPassword, smtpServer)
	err := smtp.SendMail(fmt.Sprintf("%s:%d", smtpServer, smtpPort), auth,
		smtpUser, to, []byte(msg))
	if err != nil {
		fmt.Printf("Error sending email: %s\n", err)
	}
}

func monitorMicroservice() {
	fmt.Println("Entró en la función monitor")
	for {
		if len(microservices) == 0 {
			fmt.Println("Aún no hay microservicios. Esperando")
			time.Sleep(1 * time.Minute) // Esperar 10 segundos antes de volver a verificar
			continue
		}

		for name, ms := range microservices {
			go checkMicroserviceHealth(name, ms.Endpoint, ms.Frequency)
		}

		// Esperar un tiempo antes de realizar la próxima iteración
		time.Sleep(30 * time.Second) // Esperar 1 minuto antes de volver a verificar
	}
}

func main() {
	go func() {
		for {
			monitorMicroservice()
			// Esperar un tiempo antes de reiniciar monitorMicroservice
			time.Sleep(1 * time.Minute) // Por ejemplo, reiniciar cada 5 minutos
		}
	}()

	http.HandleFunc("/register", registerMicroservice)
	http.HandleFunc("/health/", getMicroserviceHealth)
	http.HandleFunc("/health", getOverallHealth)

	fmt.Println("Microservice health monitoring server started on: 8053")
	http.ListenAndServe(":8053", nil)
}
