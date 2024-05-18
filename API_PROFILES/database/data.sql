USE api_profiles;

DROP TABLE IF EXISTS profiles;

CREATE TABLE profiles (
    id INT PRIMARY KEY,
    pagina_personal VARCHAR(255) NOT NULL,
    apodo VARCHAR(50) NOT NULL,
    contacto_publico BOOLEAN NOT NULL,
    direccion VARCHAR(150) NOT NULL,
    biografia TEXT NOT NULL,
    organizacion VARCHAR(100) NOT NULL,
    pais VARCHAR(50) NOT NULL,
    redes_sociales JSON NOT NULL
);

INSERT INTO profiles (id, pagina_personal, apodo, contacto_publico, direccion, biografia, organizacion, pais, redes_sociales) VALUES 
(1, 'http://example.com', 'Oompa Loompa', FALSE, 'Uniquindio', 'Le gustan gordas, le gustan grandes...','gordas S.A', 'OompaLandia', '{"Facebook": "Red", "Instagram": "Red"}'),
(2, 'http://example.org', 'SpiderusNigga', TRUE, 'Montenegro', 'No soy rafa polinesea >:c','Frentonas ltd', 'Colombia', '{"Facebook": "Spiderus", "Instagram": "NiggaSpider"}'),
(3, 'http://example.net', 'Braniang', TRUE, 'Nación agogo', 'Maestro de los 4 agogos','agogo', 'Nación agogo', '{"Facebook": "Braniang", "Instagram": "Maestro agogo"}'),
(4, 'http://example.co', 'Johana', FALSE, 'Montenegro', 'Sus padres son primos','mapleStory', 'Colombia', '{"Facebook": "Johana", "Instagram": "Johana"}');