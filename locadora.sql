DROP DATABASE IF EXISTS locadora;
CREATE DATABASE locadora;
USE locadora;

CREATE TABLE Locadora (
    CNPJ VARCHAR(18) PRIMARY KEY,
    Nome VARCHAR(45) NOT NULL,
    Cidade VARCHAR(45) NOT NULL
);

CREATE TABLE Cliente (
    CPF VARCHAR(14) PRIMARY KEY,
    Nome VARCHAR(45) NOT NULL,
    Data_de_nascimento DATETIME NOT NULL,
    Cidade VARCHAR(45) NOT NULL
);

CREATE TABLE Filme (
    Id INT PRIMARY KEY AUTO_INCREMENT,
    Titulo VARCHAR(45) NOT NULL,
    Ano INT,
    Diretor VARCHAR(45),
    Genero VARCHAR(45),
    Classificacao VARCHAR(5),
    Quantidade INT NOT NULL  CHECK (Quantidade > 0),
    Disponivel INT NOT NULL CHECK (Disponivel >= 0),
    Locadora_CNPJ VARCHAR(18) NOT NULL,

	FOREIGN KEY (Locadora_CNPJ) REFERENCES Locadora(CNPJ)
);

CREATE TABLE Vendedor (
    CPF VARCHAR(14) PRIMARY KEY,
    Nome VARCHAR(45) NOT NULL,
    Salario FLOAT NOT NULL,
    Data_de_nascimento DATETIME NOT NULL,
	Locadora_CNPJ VARCHAR(18) NOT NULL,

	FOREIGN KEY (Locadora_CNPJ) REFERENCES Locadora(CNPJ)
);

CREATE TABLE Emprestimo (
    Id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    DataInicio DATETIME NOT NULL,
    Devolvido TINYINT,
    Devolucao DATETIME NOT NULL DEFAULT "2999-01-02 12:00:00",
    Vendedor_CPF VARCHAR(20) NOT NULL,
    Cliente_CPF VARCHAR(20) NOT NULL,
    Locadora_CNPJ VARCHAR(18) NOT NULL,
    Filme_Id INT,
    
	FOREIGN KEY (Vendedor_CPF) REFERENCES Vendedor(CPF),
	FOREIGN KEY (Cliente_CPF) REFERENCES Cliente(CPF),
	FOREIGN KEY (Locadora_CNPJ) REFERENCES Locadora(CNPJ),
	FOREIGN KEY (Filme_Id) REFERENCES Filme(Id)
);

CREATE TABLE Multa (
    Id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    Valor_inicial FLOAT NOT NULL,
    Valor_atual FLOAT,
    DataInicio DATETIME NOT NULL,
    Locadora_CNPJ VARCHAR(18) NOT NULL,
    Emprestimo_Id INT NOT NULL,
    
	FOREIGN KEY (Locadora_CNPJ)REFERENCES Locadora(CNPJ),
	FOREIGN KEY (Emprestimo_Id)	REFERENCES Emprestimo(Id)
);

CREATE TABLE Cliente_da_Locadora (
    Locadora_CNPJ VARCHAR(18) NOT NULL,
    Cliente_CPF VARCHAR(20) NOT NULL,

    PRIMARY KEY (Locadora_CNPJ, Cliente_CPF),
	FOREIGN KEY (Locadora_CNPJ) REFERENCES Locadora(CNPJ),

	FOREIGN KEY (Cliente_CPF) REFERENCES Cliente(CPF)
);

DELIMITER $$
CREATE FUNCTION horario_devolucao(horario DATETIME) RETURNS DATETIME DETERMINISTIC
BEGIN
	RETURN DATE_ADD(horario, INTERVAL 15 DAY);
END $$


CREATE FUNCTION juros_compostos(valor_inicial FLOAT, data_inicial DATETIME, data_final DATETIME) RETURNS FLOAT DETERMINISTIC
BEGIN
	RETURN valor_inicial *
       POW(1.67,
           TIMESTAMPDIFF(DAY, data_inicial, data_final));
END $$

DELIMITER $$

CREATE TRIGGER nova_multa BEFORE INSERT ON Multa FOR EACH ROW
BEGIN
	SET NEW.valor_atual = NEW.valor_inicial;
END $$
CREATE TRIGGER nova_quantidade BEFORE INSERT ON Emprestimo FOR EACH ROW
BEGIN
    DECLARE qtd_disponivel INT;

	SET NEW.Devolucao = horario_devolucao(NEW.DataInicio);
    SELECT Disponivel INTO qtd_disponivel FROM Filme WHERE Id = NEW.Filme_Id;
    IF NEW.Devolvido = 0 THEN
        IF qtd_disponivel > 0 THEN
            UPDATE Filme SET Disponivel = Disponivel - 1 WHERE Id = NEW.Filme_Id;
		ELSE
			 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Filme indisponivel para emprestimo';
        END IF;
    END IF;
END $$

DELIMITER ;

DROP ROLE IF EXISTS 'Cargo_Vendedor';
CREATE ROLE 'Cargo_Vendedor';
GRANT SELECT, INSERT ON locadora.Filme TO 'Cargo_Vendedor';

DROP ROLE IF EXISTS 'Cargo_Gerente';
CREATE ROLE 'Cargo_Gerente';
GRANT ALL ON locadora.Cliente TO 'Cargo_Gerente';
GRANT ALL ON locadora.Vendedor TO 'Cargo_Gerente';

DROP USER IF EXISTS 'gerente'@'%';
CREATE USER 'gerente'@'%' IDENTIFIED BY '1234';

DROP USER IF EXISTS 'vendedor'@'%';
CREATE USER 'vendedor'@'%' IDENTIFIED BY '1234';

GRANT 'Cargo_Gerente' TO 'gerente'@'%'; 
GRANT 'Cargo_Vendedor' TO 'vendedor'@'%'; 

INSERT INTO Locadora (CNPJ, Nome, Cidade) VALUES
('12.345.678/0001-95', 'Localdora','Cachoeira de Minas'),
('47.892.113/0001-06', 'Mega Filmes HD', 'Conceição dos Ouros'),
('28.561.904/0001-71', 'Netflix 2', 'Pouso Alegre'),
('63.770.245/0001-18', 'Torrente', 'Santa Rita do Sapucaí'),
('91.438.526/0001-42', 'Inafilmes', 'Tupaciguara');
    
INSERT INTO Filme (Titulo, Ano, Diretor, Genero, Classificacao, Quantidade, Disponivel, Locadora_CNPJ) VALUES
('O Bicho Vai Pegar',2006, 'Roger Allers','Comedia', 'livre', 10, 1, '12.345.678/0001-95'),
('O Bicho Vai Pegar 2',2008, 'Todd Wilderman','Comedia', 'livre', 12, 4, '12.345.678/0001-95'),
('O Segredo dos Animais', 2006, 'Steve Oedekerk','Comedia', 'livre', 6, 3, '12.345.678/0001-95'),
('Kill Bill - Volume 1',2003,'Quentin Tarantino','Acao', '+18', 10, 4, '63.770.245/0001-18'),
('Em Ritmo de Fuga',2017,'Edgar Wright','Acao', '+16', 10, 10, '91.438.526/0001-42');

SELECT Id, Titulo, Quantidade, Disponivel FROM Filme;
    
INSERT INTO Cliente (CPF, Nome, Data_de_nascimento, Cidade) VALUES
('123.456.789-10', 'Luis Eduardo', '1995-03-15', 'Tricordiano'),
('987.654.321-00', 'Eric', '2001-07-21', 'Itajubá'),
('741.852.963-11', 'Igor Grecco', '1988-12-01', 'Marte'),
('852.963.741-22', 'Sheldon Dinkleberg', '1999-05-10', 'Porto Real'),
('159.357.456-33', 'Guerzoni', '1992-09-30', 'Cachoeira de Minas');

INSERT INTO Vendedor (CPF, Nome, Salario, Data_de_nascimento, Locadora_CNPJ) VALUES
('111.222.333-44', 'João Pedro', 2500.00, '1990-02-15', '12.345.678/0001-95'),
('555.666.777-88', 'Ana Clara', 3200.50, '1987-08-20', '47.892.113/0001-06'),
('999.888.777-66', 'Felipe Rocha', 2800.75, '1995-11-05', '28.561.904/0001-71'),
('444.555.666-77', 'Camila Martins', 3100.00, '1993-04-18', '63.770.245/0001-18'),
('222.333.444-55', 'Bruno Silva', 2700.25, '1998-01-12', '91.438.526/0001-42');

INSERT INTO Emprestimo (DataInicio, Devolvido, Vendedor_CPF, Cliente_CPF, Locadora_CNPJ, Filme_Id) VALUES
('2025-05-01 14:30:00', 0, '111.222.333-44', '123.456.789-10', '12.345.678/0001-95', 1),
('2025-07-15 10:00:00', 1, '555.666.777-88', '987.654.321-00', '47.892.113/0001-06', 2),
('2025-12-20 18:20:00', 0, '999.888.777-66', '741.852.963-11', '28.561.904/0001-71', 3),
('2025-03-12 09:15:00', 0, '444.555.666-77', '852.963.741-22', '63.770.245/0001-18', 4),
('2025-01-29 20:45:00', 1, '222.333.444-55', '159.357.456-33', '91.438.526/0001-42', 5);

INSERT INTO Multa (Valor_inicial, DataInicio, Locadora_CNPJ, Emprestimo_Id) VALUES
(15.50, '2025-05-06 12:00:00', '12.345.678/0001-95', 1),
(8.00,  '2025-05-07 15:30:00', '47.892.113/0001-06', 2),
(20.00, '2025-05-08 10:45:00', '28.561.904/0001-71', 3),
(12.75, '2025-05-09 18:00:00', '63.770.245/0001-18', 4),
(5.25,  '2025-05-10 09:20:00', '91.438.526/0001-42', 5);

INSERT INTO Cliente_da_Locadora (Locadora_CNPJ, Cliente_CPF) VALUES
('12.345.678/0001-95', '123.456.789-10'),
('47.892.113/0001-06', '987.654.321-00'),
('28.561.904/0001-71', '741.852.963-11'),
('63.770.245/0001-18', '852.963.741-22'),
('91.438.526/0001-42', '159.357.456-33');

SELECT * FROM Emprestimo;
SELECT Id, Titulo, Quantidade, Disponivel FROM Filme;