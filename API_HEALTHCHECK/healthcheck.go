package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/smtp"
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
var smtpServer = ""
var smtpPort = 25
var smtpUser = ""
var smtpPassword = ""

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
	healthStatus[microservice.Name] = MicroserviceHealth{
		Name:   microservice.Name,
		Status: "UP",
	}
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
	go func() {
		for {
			select {
			case <-ticker.C:
				resp, err := http.Get(endpoint + "/health")
				if err != nil {
					mutex.Lock()
					healthStatus[name] = MicroserviceHealth{
						Name:   name,
						Status: "DOWN",
						Data:   err.Error(),
					}
					mutex.Unlock()
					continue
				}
				defer resp.Body.Close()

				var health MicroserviceHealth
				err = json.NewDecoder(resp.Body).Decode(&health)
				if err != nil {
					mutex.Lock()
					healthStatus[name] = MicroserviceHealth{
						Name:   name,
						Status: "DOWN",
						Data:   err.Error(),
					}
					notifyStatusChange(name, "DOWN", microservices[name].Emails)
					mutex.Unlock()
					continue
				}

				mutex.Lock()
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
	err := smtp.SendMail(fmt.Sprintf("%s:%d", smtpServer, smtpPort),
		smtp.PlainAuth("", smtpUser, smtpPassword, smtpServer),
		smtpUser, to, []byte(msg))
	if err != nil {
		fmt.Printf("Error sending email: %s\n", err)
	}
}

func monitorMicroservice() {
	for name, ms := range microservices {
		go checkMicroserviceHealth(name, ms.Endpoint, ms.Frequency)
	}
}

func main() {
	http.HandleFunc("/register", registerMicroservice)
	http.HandleFunc("/health/", getMicroserviceHealth)
	http.HandleFunc("/health", getOverallHealth)

	go monitorMicroservice()

	fmt.Println("Microservice health monitoring server started on: 8053")
	http.ListenAndServe(":8053", nil)
}
