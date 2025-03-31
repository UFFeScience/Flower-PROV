## Flower-PROV: Adding Provenance Features in Federated Learning

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

## Overview
Repositório para os artefatos de código e documentação desenvolvidos para o Trabalho de Conclusão de Curso (TCC) do curso de Sistemas de Informação do Instituto de Computação da Universidade Federal Fluminense (IC/UFF). O trabalho tem como título "AkôFlow - Workflow Científico em nuvem com Kubernetes".

## Autores
1. Wesley Ferreira - @ovvesley 
2. Daniel de Oliveira - @danielcmo (orientador)

## Workflow Científico em nuvem com Kubernetes

### AkôFlow - Ferramenta para execução de Workflow Científico em Kubernetes

O AkôFlow é uma ferramenta para execução de workflows científicos em Kubernetes. Ele utiliza a API do Kubernetes para criar e gerenciar recursos de execução de workflows, como pods e jobs. O AkôFlow é uma ferramenta de linha de comando que permite a execução de workflows científicos de forma distribuída e paralela em um cluster Kubernetes.

### Documentação
O AkôFlow possui uma documentação completa que descreve como instalar, configurar e utilizar a ferramenta. A documentação está disponível em [https://akoflow.com/docs](https://akoflow.com/docs).


# Flower-PROV

Repository with code for Flower-PROV framework, which extends Flower framework architecture adding provenance capabilities and possibility of dynamic-tuning and fault tolerance.

To monitor, run ```streamlit run monitoring/Flower-PROV_Monitor.py```
