USE user_db;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_Name VARCHAR(12) NOT NULL,
    last_Name VARCHAR(12) NOT NULL,
    email VARCHAR(255) NOT NULL,
    contrasena VARCHAR(12) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);


INSERT INTO users(id, first_Name, last_Name, email, contrasena, created_at, updated_at) VALUES 
(1, 'Admin', 'Admin', 'admin@email.com', 'admin1', NULL, NULL),
(2, 'Johan', 'Ospina', 'johan@email.com', 'johan1', '2024-03-01 08:00:00', '2024-03-01 08:00:00'),
(3, 'Maria', 'Gonzalez', 'maria@email.com', 'maria123', '2024-03-01 08:00:00', '2024-03-01 08:00:00'),
(4, 'John', 'Smith', 'john@email.com', 'smith456', '2024-03-01 09:15:00', '2024-03-01 09:15:00'),
(5, 'Laura', 'Martinez', 'laura@email.com', 'laura789', '2024-03-01 10:30:00', '2024-03-01 10:30:00'),
(6, 'Ana', 'Lopez', 'ana@email.com', 'ana123', '2024-03-01 11:45:00', '2024-03-01 11:45:00'),
(7, 'Carlos', 'Rodriguez', 'carlos@email.com', 'carlos456', '2024-03-01 12:00:00', '2024-03-01 12:00:00'),
(8, 'Sofia', 'Garcia', 'sofia@email.com', 'sofia789', '2024-03-01 13:15:00', '2024-03-01 13:15:00'),
(9, 'David', 'Perez', 'david@email.com', 'david123', '2024-03-01 14:30:00', '2024-03-01 14:30:00'),
(10, 'Elena', 'Sanchez', 'elena@email.com', 'elena456', '2024-03-01 15:45:00', '2024-03-01 15:45:00'),
(11, 'Pablo', 'Martinez', 'pablo@email.com', 'pablo789', '2024-03-01 16:00:00', '2024-03-01 16:00:00'),
(12, 'Luisa', 'Gutierrez', 'luisa@email.com', 'luisa123', '2024-03-01 17:15:00', '2024-03-01 17:15:00'),
(13, 'Diego', 'Hernandez', 'diego@email.com', 'diego456', '2024-03-01 18:30:00', '2024-03-01 18:30:00'),
(14, 'Marta', 'Fernandez', 'marta@email.com', 'marta789', '2024-03-01 19:45:00', '2024-03-01 19:45:00'),
(15, 'Pedro', 'Lopez', 'pedro@email.com', 'pedro123', '2024-03-01 20:00:00', '2024-03-01 20:00:00'),
(16, 'Ana', 'Gomez', 'ana2@email.com', 'ana456', '2024-03-01 21:15:00', '2024-03-01 21:15:00'),
(17, 'Antonio', 'Martinez', 'antonio@email.com', 'antonio789', '2024-03-01 22:30:00', '2024-03-01 22:30:00'),
(18, 'Isabel', 'Rodriguez', 'isabel@email.com', 'isabel123', '2024-03-01 23:45:00', '2024-03-01 23:45:00'),
(19, 'Gabriel', 'Fernandez', 'gabriel@email.com', 'gabriel456', '2024-03-02 00:00:00', '2024-03-02 00:00:00'),
(20, 'Sara', 'Gutierrez', 'sara@email.com', 'sara789', '2024-03-02 01:15:00', '2024-03-02 01:15:00'),
(21, 'Carlos', 'Perez', 'carlos@email.com', 'carlos123', '2020-05-15 14:30:00', '2022-08-20 11:45:00'),
(22, 'Ana', 'Lopez', 'ana@email.com', 'ana456', '2021-07-10 09:00:00', '2023-10-25 16:20:00'),
(23, 'David', 'Garcia', 'david@email.com', 'david789', '2022-09-28 17:00:00', '2024-01-05 08:10:00'),
(24, 'Paula', 'Rodriguez', 'paula@email.com', 'paula123', '2023-02-12 10:45:00', '2024-03-01 12:30:00'),
(25, 'Manuel', 'Sanchez', 'manuel@email.com', 'manuel456', '2024-04-20 15:20:00', '2024-05-18 14:15:00'),
(26, 'Laura', 'Gomez', 'laura@email.com', 'laura789', '2020-12-01 08:30:00', '2022-03-10 09:45:00'),
(27, 'Daniel', 'Martinez', 'daniel@email.com', 'daniel123', '2021-03-05 11:00:00', '2023-06-15 10:00:00'),
(28, 'Sofia', 'Hernandez', 'sofia@email.com', 'sofia456', '2022-06-20 13:45:00', '2023-09-30 12:20:00'),
(29, 'Alejandro', 'Fernandez', 'alejandro@email.com', 'alejandro789', '2023-10-10 16:00:00', '2024-02-05 15:15:00'),
(30, 'Elena', 'Perez', 'elena@email.com', 'elena123', '2024-01-15 09:30:00', '2024-03-01 08:00:00'),
(31, 'Gabriel', 'Diaz', 'gabriel@email.com', 'gabriel456', '2020-08-10 14:15:00', '2022-11-20 13:30:00'),
(32, 'Valeria', 'Gutierrez', 'valeria@email.com', 'valeria789', '2021-01-25 10:00:00', '2023-04-05 09:45:00'),
(33, 'Pablo', 'Alvarez', 'pablo@email.com', 'pablo123', '2022-04-30 12:45:00', '2023-07-10 11:20:00'),
(34, 'Luisa', 'Romero', 'luisa@email.com', 'luisa456', '2023-07-05 15:30:00', '2024-09-15 14:15:00'),
(35, 'Andres', 'Torres', 'andres@email.com', 'andres789', '2024-02-10 11:20:00', '2024-03-01 10:00:00'),
(36, 'Andres', 'Castro', 'andresf.castroc1@uqvirtual.edu.co', 'andres1234', '2024-02-10 11:26:00', '2024-02-10 11:30:00');