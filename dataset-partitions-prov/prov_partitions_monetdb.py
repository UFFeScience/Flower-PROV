import pymonetdb
import configparser

config = configparser.ConfigParser()
config.read("config.cfg")

monetdb_config = config["MonetDB Settings"]
conn = pymonetdb.connect(
    username=monetdb_config["username"],
    password=monetdb_config["password"],
    hostname=monetdb_config["hostname"],
    port=monetdb_config["port"],
    database=monetdb_config["database"],
)

partitions_settings = config["Partitions Settings"]
path = partitions_settings["path"]
num_partitions = int(partitions_settings["number_of_partitions"])

tables = ["test", "train"]

for partition in range(num_partitions):
    for table in tables:
        cursor = conn.cursor()
        table_name = "dataset_{}_partition{}".format(table, partition)
        path_partition = "{}/partition_{}/y_{}/labels.txt".format(
            path, partition, table
        )
        cursor.execute(
            "CREATE TABLE {} (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, path_image VARCHAR(99) NOT NULL, class int NOT NULL)".format(
                table_name
            )
        )
        conn.commit()
        cursor.execute(
            "COPY INTO {} (path_image, class) FROM '{}' (path_image, class)  USING DELIMITERS ',', E'\n'; ".format(
                table_name, path_partition
            )
        )
        cursor.close()

conn.close()
