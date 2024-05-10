USE user_db;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,    
    email VARCHAR(255) NOT NULL,
    contrasena VARCHAR(12) NOT NULL
);


INSERT INTO users(id, email, contrasena) VALUES 
(1, 'admin@email.com', 'admin1'),
(2, 'johan@email.com', 'johan1'),
(3, 'maria@email.com', 'maria123'),
(4, 'andresf.castroc1@uqvirtual.edu.co', 'andres1234');