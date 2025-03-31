## Flower-PROV: Adding Provenance Features in Federated Learning

![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)  

```sh
 ________   __                                                    _______    _______        ___     ____   ____
|_   __  | [  |                                                  |_   __ \  |_   __ \     .'   `.  |_  _| |_  _|
  | |_ \_|  | |    .--.    _   _   __   .---.   _ .--.   ______    | |__) |   | |__) |   /  .-.  \   \ \   / /
  |  _|     | |  / .'`\ \ [ \ [ \ [  ] / /__\\ [ `/'`\] |______|   |  ___/    |  __ /    | |   | |    \ \ / /
 _| |_      | |  | \__. |  \ \/\ \/ /  | \__.,  | |               _| |_      _| |  \ \_  \  `-'  /     \ ' /
|_____|    [___]  '.__.'    \__/\__/    '.__.' [___]             |_____|    |____| |___|  `.___.'       \_/
```

## Motivation

Federated Learning (FL) has emerged as a privacy-preserving paradigm that enables distributed model training without sharing raw data. However, ensuring **traceability** in FL workflows **remains a challenge** due to the inherently **distributed nature of FL**. Data is spread across multiple clients—ranging from a few to thousands—without clear consumption/production relationships within the workflow.

Tracking and understanding model evolution in this scenario requires insights into the data derivation path and key evaluation metrics (_e.g._, accuracy, silhouette score) for both local and aggregated models. This information is essential not only for understanding and explaining the training process but also for dynamically adjusting the FL workflow.

Since each training round can take minutes to hours, the ability to monitor and fine-tune the process in real time is critical. Poor hyperparameter configurations can lead to wasted time and computational resources—especially in existing FL frameworks, where users often only evaluate model performance after complete training.

This project addresses these challenges by improving traceability and monitoring capabilities in FL workflows.

## 🌻 Flower-PROV: Provenance-Aware Federated Learning 🌻

**Flower-PROV** is an extension of the open-source [**Flower**](https://flower.ai/) Federated Learning (FL) framework, designed to integrate **provenance tracking** as a core component of FL workflows to enhance **reproducibility and analysis**.

## 🔍 Key Features

**Flower-PROV** enables the **automatic and distributed capture** of:
- **Retrospective provenance** (*r-prov*): Logs details about the actual FL workflow execution.
- **Prospective provenance** (*p-prov*): Represents the FL workflow specification.

The captured provenance data includes:

✅ Participating clients  
✅ Hyperparameter values  
✅ Accuracy metrics  
✅ Model versions and checkpoints  

Beyond simply collecting provenance data, **Flower-PROV** actively **uses** it to:
- **Dynamically adjust model hyperparameters** during training.
- **Enable clients to recover previously trained models** as a starting point for local training, avoiding redundant computations.

## Requirements

keras==2.12.0

matplotlib==3.7.1
pandas==2.0.1

Pillow==9.5.0

pymonetdb==1.6.4

streamlit==1.27.2

tensorflow==2.12.0

## 🎯 Installation  

### Software requirements

The following list of software has to be configured/installed for running Flower-PROV.

- [MonetDB](http://www.monetdb.org/Documentation/UserGuide/Tutorial) and [pymonetdb](https://pypi.org/project/pymonetdb/)
- [MongoDB](https://www.mongodb.com/) and [pymongo](https://pypi.org/project/pymongo/)
- [DfAnalyzer](https://github.com/dbpina/keras-prov/tree/main/DfAnalyzer)
- [dfa-lib-python](https://github.com/dbpina/keras-prov/tree/main/dfa-lib-python/)

##  🐳 Running an Example in a Docker Environment  🐳

We provide a pre-built Docker image that includes the DfAnalyzer, the python library and the provenance database (MonetDB):

```bash
docker pull nymeria0042/dfanalyzer
```

We also provide a `docker-compose.yaml` that we'll use to launch our containers.

This guide demonstrates how to run a Flower-PROV container using the CIFAR-10 dataset. We begin by splitting the dataset in a balanced manner using the `dataset-splitter` component. 

Navigate to the `dataset-splitter` directory and execute the following command:

```sh
python splitter.py --dataset_splitter_config_file config/dataset_splitter.cfg
```

This will create 5 folders - default - with the data that will be used by each client.

Next, we can start the DfAnalyzer container, which runs in the background to capture all provenance data for the experiment:

```sh
docker compose up dfanalyzer
```

Once the DfAnalyzer service is running, execute the prospective provenance script, which defines the structure and parameters to be captured. Additionally, start the MongoDB service, which stores the model weights for fault tolerance.

When it’s finished, we can start the server:

```sh
docker compose up server
```

Start the clients — five in this demonstration:

```sh
docker compose up client1 client2 client3 client4 client5
```

### Submiting queries 🔍

Once the experiment is running, you can submit queries to the provenance database (MonetDB) to monitor metrics and parameter/hyperparameters configurations.

First, we connect to the provenance database, running in the DfAnalyzer container:

```sh
 docker exec -it dfanalyzer mclient -u monetdb -d dataflow_analyzer
```

The default password is `monetdb`. 

Then, we can submit the queries, like:.

```sql
SELECT client_id FROM oClientTraining WHERE server_round = 5;
-- to see which clients were participating in round 5
```

```sql
SELECT server_round, accuracy FROM oServerTrainingAggregation ;
-- to see how the accuracy is evolving
```

```sql
SELECT server_round, dynamically_adjusted FROM oTrainingConfig;
--  to see in which rounds the dynamic adjust was trigged
```

```sql
SELECT server_round, insertion_time, weights_mongo_id, checkpoint_time FROM oServerTrainingAggregation;
-- to monitor the insertion of the checkpoints
```

### Provenance Graph

The user can also access `localhost:22000` to view the provenance graph and understand each step of the FL workflow:

![image](https://github.com/user-attachments/assets/b44186dd-3a5c-4bbb-a116-58d2d5028a83)


### Monitoring

To monitor the metrics, the user can run the streamlit app locally:

```streamlit run monitoring/Flower-PROV_Monitor.py```


## References

- Beutel, D. J., et al. [*Flower: A Friendly Federated Learning Framework.*](https://arxiv.org/abs/2007.14390), 2020.  
- Lopes, C., et al. [*Flower-PROV: Provenance-Aware Federated Learning.*](https://dblp.org/rec/conf/carla/LopesNBD023) CARLA 2023.  

## 📜 License  

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for more details.  
