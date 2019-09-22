CREATE TABLE IF NOT EXISTS Class(
	id			int(10)			AUTO_INCREMENT PRIMARY KEY,
	name		varchar(50),
	description	varchar(200)
);

CREATE TABLE IF NOT EXISTS Student(
	id			int(10)			AUTO_INCREMENT PRIMARY KEY,
	name		varchar(50),
	class_id	int(10),
	FOREIGN KEY (class_id)
        REFERENCES Class (id)
        ON UPDATE RESTRICT ON DELETE CASCADE
);

INSERT INTO Class(name, description) VALUES
	("Toan", "Hoc Toan"),
	("Ly", "Hoc Ly"),
	("Hoa", "Hoc Hoa");

INSERT INTO Student(name, class_id) VALUES
	("Dai", 1),
	("Hoang", 2),
	("Thong", 3),
	("Ly", 2),
	("Hai", 1),
	("Lai", 2),
	("Nhi", 3),
	("Linh", 3);
