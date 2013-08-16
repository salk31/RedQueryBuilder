
CREATE TABLE People
(
"id" int,
LastName varchar(40),
FirstName varchar(40),
Address varchar(60),
City varchar(50)
);

INSERT INTO People ("id", LastName, FirstName, Address, City) 
VALUES (123, 'Smith', 'John', 'House', 'Wigan');
INSERT INTO People ("id", LastName, FirstName, Address, City) 
VALUES (124, 'Smith', 'Jane', 'House', 'Wigan');