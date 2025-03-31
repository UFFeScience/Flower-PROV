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

![image](https://github.com/user-attachments/assets/e83def03-c5a1-4802-8207-4fad38d0e579)


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

## 🎯 Installation  

##  🐳 Running an Example in a Docker Environment  🐳


## References

- Beutel, D. J., et al. [*Flower: A Friendly Federated Learning Framework.*](https://arxiv.org/abs/2007.14390), 2020.  
- Lopes, G., et al. [*Flower-PROV: Provenance-Aware Federated Learning.*](https://dblp.org/rec/conf/carla/LopesNBD023) CARLA 2023.  

## 📜 License  

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for more details.  


# Flower-PROV

Repository with code for Flower-PROV framework, which extends Flower framework architecture adding provenance capabilities and possibility of dynamic-tuning and fault tolerance.

To monitor, run ```streamlit run monitoring/Flower-PROV_Monitor.py```
