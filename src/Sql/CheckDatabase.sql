USE locadora;

SELECT * FROM Locadora;
SELECT Cliente.* FROM Cliente LEFT OUTER JOIN Cliente_Da_Locadora ON Cliente.CPF = Cliente_Da_Locadora.Cliente_CPF WHERE Cliente_Da_locadora.Locadora_CNPJ='12.345.678/0001-95';
SELECT * FROM Filme;
SELECT Vendedor.* FROM Vendedor LEFT OUTER JOIN locadora ON Vendedor.Locadora_CNPJ = Locadora.CNPJ WHERE Locadora.CNPJ='12.345.678/0001-95';
