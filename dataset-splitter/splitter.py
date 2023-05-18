from argparse import ArgumentParser
from configparser import ConfigParser
from itertools import cycle
from logging import FileHandler, Formatter, getLevelName, Logger, StreamHandler
from math import ceil
from matplotlib import pyplot
from numpy import arange, ndarray
from pandas import DataFrame
from pathlib import Path
from PIL import Image
from random import seed, shuffle
from re import findall
from shutil import copy, rmtree
from typing import Any, Optional
from os import environ
environ["TF_CPP_MIN_LOG_LEVEL"] = "3"
from keras.datasets import cifar10


class DatasetSplitter:

    def __init__(self,
                 dataset_splitter_config_file: Path) -> None:
        # Dataset Splitter's Config File.
        self.dataset_splitter_config_file = dataset_splitter_config_file
        # Dataset Splitter's Config File Settings.
        self.general_settings = None
        self.cifar10_dataset_settings = None
        # Other Attributes.
        self.logger = None
        self.x_train_folder = None
        self.y_train_folder = None
        self.y_train_labels_file = None
        self.x_test_folder = None
        self.y_test_folder = None
        self.y_test_labels_file = None
        self.number_of_classes = None

    @staticmethod
    def parse_config_section(config_parser: ConfigParser,
                             section_name: str) -> dict:
        parsed_section = {key: value for key, value in config_parser[section_name].items()}
        for key, value in parsed_section.items():
            if value == "None":
                parsed_section[key] = None
            elif value in ["True", "Yes"]:
                parsed_section[key] = True
            elif value in ["False", "No"]:
                parsed_section[key] = False
            elif value.isdigit():
                parsed_section[key] = int(value)
            elif value.replace(".", "", 1).isdigit():
                parsed_section[key] = float(value)
            elif not findall(r"%\(.*?\)s+", value) and findall(r"\[.*?]+", value):
                aux_list = value.replace("[", "").replace("]", "").replace(" ", "").split(",")
                for index, item in enumerate(aux_list):
                    if item.isdigit():
                        aux_list[index] = int(item)
                    elif item.replace(".", "", 1).isdigit():
                        aux_list[index] = float(item)
                parsed_section[key] = aux_list
            elif not findall(r"%\(.*?\)s+", value) and findall(r"\(.*?\)+", value):
                aux_list = value.replace("(", "").replace(")", "").replace(" ", "").split(",")
                for index, item in enumerate(aux_list):
                    if item.isdigit():
                        aux_list[index] = int(item)
                    elif item.replace(".", "", 1).isdigit():
                        aux_list[index] = float(item)
                parsed_section[key] = tuple(aux_list)
            elif not findall(r"%\(.*?\)s+", value) and findall(r"\{.*?}+", value):
                aux_dict = {}
                aux_list = value.replace("{", "").replace("}", "").replace(" ", "").split(",")
                for item in aux_list:
                    pair_item = item.split(":")
                    pair_key = pair_item[0]
                    pair_value = pair_item[1]
                    if pair_value == "None":
                        pair_value = None
                    elif pair_value in ["True", "Yes"]:
                        pair_value = True
                    elif pair_value in ["False", "No"]:
                        pair_value = False
                    elif pair_value.isdigit():
                        pair_value = int(value)
                    elif pair_value.replace(".", "", 1).isdigit():
                        pair_value = float(value)
                    aux_dict.update({pair_key: pair_value})
                parsed_section[key] = aux_dict
        return parsed_section

    def set_attribute(self,
                      attribute_name: str,
                      attribute_value: Any) -> None:
        setattr(self, attribute_name, attribute_value)

    def get_attribute(self,
                      attribute_name: str) -> Any:
        return getattr(self, attribute_name)

    def parse_dataset_splitter_config_file(self) -> None:
        # Get Dataset Splitter's Config File.
        dataset_splitter_config_file = self.get_attribute("dataset_splitter_config_file")
        # Init ConfigParser Object.
        cp = ConfigParser()
        cp.optionxform = str
        cp.read(filenames=dataset_splitter_config_file, encoding="utf-8")
        # Parse 'General Settings' and Set Attributes.
        general_settings = self.parse_config_section(cp, "General Settings")
        self.set_attribute("general_settings", general_settings)
        # If Logging is Enabled...
        if general_settings["enable_logging"]:
            # Parse 'Logging Settings' and Set Attributes.
            logging_settings = self.parse_config_section(cp, "Logging Settings")
            self.set_attribute("logging_settings", logging_settings)
        # Parse 'Dataset Partitions Settings' and Set Attributes.
        dataset_partitions_settings = self.parse_config_section(cp, "Dataset Partitions Settings")
        self.set_attribute("dataset_partitions_settings", dataset_partitions_settings)
        # If Input Dataset is CIFAR-10...
        if general_settings["dataset_name"] == "CIFAR-10":
            # Parse 'CIFAR-10 Dataset Settings' and Set Attributes.
            cifar10_dataset_settings = self.parse_config_section(cp, "CIFAR-10 Dataset Settings")
            self.set_attribute("cifar10_dataset_settings", cifar10_dataset_settings)
        # Unbind ConfigParser Object (Garbage Collector).
        del cp

    def load_logger(self) -> Optional[Logger]:
        logger = None
        general_settings = self.get_attribute("general_settings")
        if general_settings["enable_logging"]:
            logger_name = "DatasetSplitter"
            logging_settings = self.get_attribute("logging_settings")
            logger = Logger(name=logger_name,
                            level=logging_settings["level"])
            formatter = Formatter(fmt=logging_settings["format"],
                                  datefmt=logging_settings["date_format"])
            if logging_settings["log_to_file"]:
                file_parents_path = findall("(.*/)", logging_settings["file_name"])
                if file_parents_path:
                    Path(file_parents_path[0]).mkdir(parents=True, exist_ok=True)
                file_handler = FileHandler(filename=logging_settings["file_name"],
                                           mode=logging_settings["file_mode"],
                                           encoding=logging_settings["encoding"])
                file_handler.setLevel(logger.getEffectiveLevel())
                file_handler.setFormatter(formatter)
                logger.addHandler(file_handler)
            if logging_settings["log_to_console"]:
                console_handler = StreamHandler()
                console_handler.setLevel(logger.getEffectiveLevel())
                console_handler.setFormatter(formatter)
                logger.addHandler(console_handler)
        return logger

    def log_message(self,
                    message: str,
                    message_level: str) -> None:
        logger = self.get_attribute("logger")
        if logger and getLevelName(logger.getEffectiveLevel()) != "NOTSET":
            if message_level == "DEBUG":
                logger.debug(msg=message)
            elif message_level == "INFO":
                logger.info(msg=message)
            elif message_level == "WARNING":
                logger.warning(msg=message)
            elif message_level == "ERROR":
                logger.error(msg=message)
            elif message_level == "CRITICAL":
                logger.critical(msg=message)

    def wipe_folder_content(self,
                            folder: Path,
                            content_to_delete: list) -> None:
        if folder.exists() and content_to_delete:
            message = "Wiping the '{0}' Folder Content...".format(folder)
            self.log_message(message, "INFO")
            for folder_item in folder.iterdir():
                for item in content_to_delete:
                    if item in str(folder_item):
                        if folder_item.is_dir():
                            message = "'{0}' Folder Deleted Successfully!".format(folder_item)
                            rmtree(folder_item)
                        elif folder_item.is_file():
                            message = "'{0}' File Deleted Successfully!".format(folder_item)
                            folder_item.unlink()
                        self.log_message(message, "INFO")
            message = "The '{0}' Folder Content Was Wiped Successfully!".format(folder)
            self.log_message(message, "INFO")

    @staticmethod
    def fetch_cifar_10_dataset() -> tuple:
        # Fetch the CIFAR-10 Dataset, An Popular Multi-Class Classification Colored 32x32 Images Dataset.
        (x_train, y_train), (x_test, y_test) = cifar10.load_data()
        return x_train, y_train, x_test, y_test

    @staticmethod
    def save_nd_array_to_image_file(image: ndarray,
                                    output_file: Path) -> None:
        file_parents_path = findall("(.*/)", str(output_file))
        if file_parents_path:
            Path(file_parents_path[0]).mkdir(parents=True, exist_ok=True)
        image = Image.fromarray(obj=image)
        image.save(fp=output_file)
        del image

    @staticmethod
    def append_label_to_text_file(image_label: str,
                                  output_file: Path) -> None:
        file_parents_path = findall("(.*/)", str(output_file))
        if file_parents_path:
            Path(file_parents_path[0]).mkdir(parents=True, exist_ok=True)
        with open(file=output_file, mode="a") as y_phase_labels_file:
            y_phase_labels_file.write(image_label + "\n")

    def save_cifar_10_dataset(self,
                              x_train: ndarray,
                              y_train: ndarray,
                              x_test: ndarray,
                              y_test: ndarray) -> None:
        # Get CIFAR-10 Dataset Settings.
        cifar10_dataset_settings = self.get_attribute("cifar10_dataset_settings")
        # Get CIFAR-10's Root Folder.
        root_folder = Path(cifar10_dataset_settings["root_folder"])
        # Set Training (x_train, y_train) and Testing (x_test, y_test) Folders Paths.
        x_train_folder = root_folder.joinpath("x_train")
        y_train_folder = root_folder.joinpath("y_train")
        x_test_folder = root_folder.joinpath("x_test")
        y_test_folder = root_folder.joinpath("y_test")
        # Initialize the Shared Index for All Training and Testing Examples.
        example_index = 0
        # Save the Training Examples.
        message = "Saving the CIFAR-10's {0} Training Examples...".format(len(x_train))
        self.log_message(message, "INFO")
        message = "Shape of x_train: {0} | Shape of y_train: {1}".format(x_train.shape, y_train.shape)
        self.log_message(message, "INFO")
        for train_index in range(0, len(x_train)):
            # Set the X_Train Current Example File Name (Training Image File).
            image_file = str(example_index) + ".png"
            # Save the X_Train Current Example (Training Image File).
            x_train_image_nd_array = x_train[train_index]
            x_train_image_output_file = x_train_folder.joinpath(image_file)
            self.save_nd_array_to_image_file(x_train_image_nd_array, x_train_image_output_file)
            # Save the Y_Train Current Example (Training Image Label).
            y_train_image_label = int(y_train[train_index][0])
            y_train_image_file_and_label = image_file + ", " + str(y_train_image_label)
            y_train_labels_file = y_train_folder.joinpath("labels.txt")
            self.append_label_to_text_file(y_train_image_file_and_label, y_train_labels_file)
            example_index += 1
        # Save the Testing Examples.
        message = "Saving the CIFAR-10's {0} Testing Examples...".format(len(x_test))
        self.log_message(message, "INFO")
        message = "Shape of x_test: {0} | Shape of y_test: {1}".format(x_test.shape, y_test.shape)
        self.log_message(message, "INFO")
        for test_index in range(0, len(x_test)):
            # Set the X_Test Current Example File Name (Testing Image File).
            image_file = str(example_index) + ".png"
            # Save the X_Test Current Example (Testing Image File).
            x_test_image_nd_array = x_test[test_index]
            x_test_image_output_file = x_test_folder.joinpath(image_file)
            self.save_nd_array_to_image_file(x_test_image_nd_array, x_test_image_output_file)
            # Save the Y_Test Current Example (Testing Image Label).
            y_test_image_label = int(y_test[test_index][0])
            y_test_image_file_and_label = image_file + ", " + str(y_test_image_label)
            y_test_labels_file = y_test_folder.joinpath("labels.txt")
            self.append_label_to_text_file(y_test_image_file_and_label, y_test_labels_file)
            example_index += 1

    def fetch_and_save_input_dataset(self) -> None:
        general_settings = self.get_attribute("general_settings")
        if general_settings["fetch_dataset_from_source"]:
            message = "Fetching the Input Dataset..."
            self.log_message(message, "INFO")
            if general_settings["dataset_name"] == "CIFAR-10":
                root_folder = Path(self.get_attribute("cifar10_dataset_settings")["root_folder"])
                content_to_delete = ["x_train", "y_train", "x_test", "y_test"]
                self.wipe_folder_content(root_folder,
                                         content_to_delete)
                x_train, y_train, x_test, y_test = self.fetch_cifar_10_dataset()
                self.save_cifar_10_dataset(x_train, y_train, x_test, y_test)
                del x_train, y_train, x_test, y_test
            message = "The Input Dataset Was Fetched Successfully!"
            self.log_message(message, "INFO")

    def set_input_dataset_training_and_testing_paths(self) -> None:
        general_settings = self.get_attribute("general_settings")
        if general_settings["dataset_name"] == "CIFAR-10":
            cifar10_dataset_settings = self.get_attribute("cifar10_dataset_settings")
            root_folder = Path(cifar10_dataset_settings["root_folder"])
            x_train_folder = root_folder.joinpath("x_train")
            self.set_attribute("x_train_folder", x_train_folder)
            y_train_folder = root_folder.joinpath("y_train")
            self.set_attribute("y_train_folder", y_train_folder)
            y_train_labels_file = y_train_folder.joinpath("labels.txt")
            self.set_attribute("y_train_labels_file", y_train_labels_file)
            x_test_folder = root_folder.joinpath("x_test")
            self.set_attribute("x_test_folder", x_test_folder)
            y_test_folder = root_folder.joinpath("y_test")
            self.set_attribute("y_test_folder", y_test_folder)
            y_test_labels_file = y_test_folder.joinpath("labels.txt")
            self.set_attribute("y_test_labels_file", y_test_labels_file)
            number_of_classes = cifar10_dataset_settings["number_of_classes"]
            self.set_attribute("number_of_classes", number_of_classes)

    def generate_labels_images_dict(self,
                                    y_phase: str) -> dict:
        labels_images_dict = {}
        y_phase_labels_file = None
        examples_fraction = None
        if y_phase == "train":
            y_phase_labels_file = self.get_attribute("y_train_labels_file")
            examples_fraction = self.get_attribute("dataset_partitions_settings")["training_examples_fraction"]
        elif y_phase == "test":
            y_phase_labels_file = self.get_attribute("y_test_labels_file")
            examples_fraction = self.get_attribute("dataset_partitions_settings")["testing_examples_fraction"]
        number_of_examples = sum(1 for _ in open(file=y_phase_labels_file, mode="r"))
        lines_fraction = ceil(number_of_examples * examples_fraction)
        message = "Sampling {0} {1}ing Examples (Out of {2})..." \
            .format(lines_fraction,
                    y_phase.capitalize(),
                    number_of_examples)
        self.log_message(message, "INFO")
        with open(file=y_phase_labels_file, mode="r") as labels_file:
            lines = [next(labels_file) for _ in range(lines_fraction)]
            for line in lines:
                split_line = line.rstrip().split(", ")
                label = split_line[1]
                image_file = split_line[0]
                if label in labels_images_dict:
                    labels_images_dict[label].append(image_file)
                else:
                    labels_images_dict[label] = [image_file]
        return labels_images_dict

    def partition_labels_images_dict(self,
                                     labels_images_dict: dict) -> list:
        dataset_partitions_settings = self.get_attribute("dataset_partitions_settings")
        number_of_partitions = dataset_partitions_settings["number_of_partitions"]
        partition_mode = dataset_partitions_settings["partition_mode"]
        shuffle_x = dataset_partitions_settings["shuffle_x"]
        shuffle_y = dataset_partitions_settings["shuffle_y"]
        shuffle_seed = dataset_partitions_settings["shuffle_seed"]
        labels_images_dicts_list = []
        partitions_ids_list = []
        for partition_id in range(0, number_of_partitions):
            partitions_ids_list.append(partition_id)
            labels_images_dicts_list.append({})
        if partition_mode == "most_possibly_balanced":
            if shuffle_y:
                keys = list(labels_images_dict.keys())
                seed(shuffle_seed)
                shuffle(keys)
                labels_shuffled_images_dict = dict()
                for key in keys:
                    labels_shuffled_images_dict.update({key: labels_images_dict[key]})
                labels_images_dict = labels_shuffled_images_dict
            partitions_round_robin = cycle(partitions_ids_list)
            for label in labels_images_dict:
                images_list = list(labels_images_dict[label])
                if shuffle_x:
                    seed(shuffle_seed)
                    shuffle(images_list)
                while images_list:
                    selected_image = images_list.pop()
                    selected_partition = next(partitions_round_robin)
                    if label in labels_images_dicts_list[selected_partition]:
                        labels_images_dicts_list[selected_partition][label].append(selected_image)
                    else:
                        labels_images_dicts_list[selected_partition][label] = [selected_image]
        return labels_images_dicts_list

    def copy_images_files_to_partition(self,
                                       partition_id: int,
                                       partition_labels_images_dict: dict,
                                       x_phase: str) -> None:
        dataset_partitions_settings = self.get_attribute("dataset_partitions_settings")
        output_root_folder = Path(dataset_partitions_settings["output_root_folder"])
        output_root_folder.mkdir(parents=True, exist_ok=True)
        partition_folder = Path(output_root_folder).joinpath("partition_" + str(partition_id))
        partition_folder.mkdir(exist_ok=True)
        x_phase_folder = None
        x_phase_source_folder = None
        if x_phase == "train":
            x_phase_folder = partition_folder.joinpath("x_train")
            x_phase_source_folder = Path(self.get_attribute("x_train_folder"))
        elif x_phase == "test":
            x_phase_folder = partition_folder.joinpath("x_test")
            x_phase_source_folder = Path(self.get_attribute("x_test_folder"))
        x_phase_folder.mkdir(exist_ok=True)
        num_images = 0
        for label in partition_labels_images_dict:
            images = partition_labels_images_dict[label]
            num_images += len(images)
            for image in images:
                source_file = x_phase_source_folder.joinpath(image)
                destination_file = x_phase_folder.joinpath(image)
                copy(src=source_file,
                     dst=destination_file)
        if num_images == 1:
            message = "'{0}' Folder Filled With {1} Example (Image File).".format(x_phase_folder, num_images)
        else:
            message = "'{0}' Folder Filled With {1} Examples (Image Files).".format(x_phase_folder, num_images)
        self.log_message(message, "INFO")

    def generate_images_labels_file_to_partition(self,
                                                 partition_id: int,
                                                 partition_labels_images_dict: dict,
                                                 y_phase: str) -> None:
        dataset_partitions_settings = self.get_attribute("dataset_partitions_settings")
        shuffle_seed = dataset_partitions_settings["shuffle_seed"]
        output_root_folder = Path(dataset_partitions_settings["output_root_folder"])
        output_root_folder.mkdir(parents=True, exist_ok=True)
        partition_folder = Path(output_root_folder).joinpath("partition_" + str(partition_id))
        partition_folder.mkdir(exist_ok=True)
        y_phase_folder = None
        if y_phase == "train":
            y_phase_folder = partition_folder.joinpath("y_train")
        elif y_phase == "test":
            y_phase_folder = partition_folder.joinpath("y_test")
        y_phase_folder.mkdir(exist_ok=True)
        images_labels_file = y_phase_folder.joinpath("labels.txt")
        num_images = 0
        image_label_list = []
        for label in partition_labels_images_dict:
            images = partition_labels_images_dict[label]
            num_images += len(images)
            for image in images:
                image_label = image + ", " + label
                image_label_list.append(image_label)
        # Shuffle the Image-Label Pairs to Avoid Consecutive Images With the Same Label.
        # Ordered Labels Usually Degrade the Learning Process of a Classification Model.
        # (e.g., Validation Loss Tends to Increase Over Steps).
        seed(shuffle_seed)
        shuffle(image_label_list)
        with open(file=images_labels_file, mode="a") as y_phase_labels_file:
            for image_label in image_label_list:
                y_phase_labels_file.write(image_label + "\n")
        if num_images == 0:
            message = "'{0}' Folder Filled With An Empty Labels Text File.".format(y_phase_folder)
        else:
            message = "'{0}' Folder Filled With A Labels Text File.".format(y_phase_folder)
        self.log_message(message, "INFO")

    @staticmethod
    def build_partitions_classification_graph(y_phase: str,
                                              y_labels_images_dicts_list: list,
                                              number_of_classes: int,
                                              partition_mode: str,
                                              plot_graph: bool,
                                              save_graph: bool,
                                              output_root_folder: Path) -> None:
        # Generate DataFrame's Data.
        data = []
        max_y = 0
        for partition_id in range(0, len(y_labels_images_dicts_list)):
            partition_classes_examples_list = [0] * number_of_classes
            partition_labels_images_dict = y_labels_images_dicts_list[partition_id]
            for label in partition_labels_images_dict:
                number_of_examples = len(partition_labels_images_dict[label])
                partition_classes_examples_list[int(label)] = number_of_examples
                if number_of_examples > max_y:
                    max_y = number_of_examples
            partition_data = ["Partition {0}".format(partition_id)] + partition_classes_examples_list
            data.append(partition_data)
        # Generate DataFrame's Columns.
        columns = ["Partitions"]
        for class_index in range(0, number_of_classes):
            columns.append("Class {0}".format(class_index))
        # Create DataFrame.
        df = DataFrame(data=data,
                       columns=columns)
        # Set Y Axis Ticks.
        y_ticks = arange(start=0,
                         stop=(max_y + 1),
                         step=ceil(0.1 * max_y))
        # Plot Grouped Bar Chart.
        df.plot(x="Partitions",
                rot=0,
                kind="bar",
                stacked=False)
        pyplot.suptitle(t="{0}ing Dataset Distribution".format(y_phase.capitalize()),
                        fontsize=14,
                        weight="bold",
                        horizontalalignment="center",
                        verticalalignment="top")
        pyplot.title(label="Partition Mode: {0}".format(partition_mode.replace("_", " ").title()),
                     fontsize=10,
                     loc="center")
        pyplot.yticks(ticks=y_ticks)
        pyplot.ylim(bottom=0,
                    top=ceil(1.1 * max_y))
        pyplot.ylabel(ylabel="Number of Examples",
                      fontsize=10,
                      weight="bold")
        pyplot.xlabel(xlabel="Partitions",
                      fontsize=10,
                      weight="bold")
        pyplot.legend(loc="center left",
                      bbox_to_anchor=(1, 0.5))
        pyplot.tight_layout()
        if save_graph:
            pyplot.savefig(fname=output_root_folder.joinpath(y_phase + "ing_dataset_distribution_graph.png"),
                           dpi=300,
                           facecolor="white",
                           edgecolor="white")
        if plot_graph:
            pyplot.show()

    def split_input_dataset(self) -> None:
        dataset_partitions_settings = self.get_attribute("dataset_partitions_settings")
        # Get the Number of Partitions to Split the Input Dataset.
        number_of_partitions = dataset_partitions_settings["number_of_partitions"]
        # Get the Partition Mode to Use for Splitting the Input Dataset.
        partition_mode = dataset_partitions_settings["partition_mode"]
        # Get the Partitions' Data Distribution Graphs Boolean Settings.
        plot_data_distribution_graphs = dataset_partitions_settings["plot_data_distribution_graphs"]
        save_data_distribution_graphs = dataset_partitions_settings["save_data_distribution_graphs"]
        # Get the Dataset Partitions' Output Root Folder.
        output_root_folder = Path(dataset_partitions_settings["output_root_folder"])
        # Wipe Output Root Folder's Content (If Any).
        content_to_delete = ["partition_", "_dataset_distribution_graph"]
        self.wipe_folder_content(output_root_folder,
                                 content_to_delete)
        message = "Splitting the Input Dataset into {0} Partitions, Using the '{1}' Partition Mode..." \
            .format(number_of_partitions,
                    partition_mode)
        self.log_message(message, "INFO")
        # Get Input Dataset's Type.
        dataset_type = self.get_attribute("general_settings")["dataset_type"]
        if dataset_type == "multi_class_image_classification":
            # Get Input Dataset's Number of Classes.
            number_of_classes = self.get_attribute("number_of_classes")
            # Set the X and Y Phases to Training.
            x_phase = "train"
            y_phase = "train"
            # Generate the Input Dataset's Y_Train Dict (K:Labels, V:Images Paths).
            y_train_labels_images_dict = self.generate_labels_images_dict(y_phase)
            # Split the Input Dataset's Y_Train Dict into N Training Partitions.
            y_train_labels_images_dicts_list = self.partition_labels_images_dict(y_train_labels_images_dict)
            # Iterate the N Training Partitions Generated.
            for partition_id in range(0, len(y_train_labels_images_dicts_list)):
                partition_labels_images_dict = y_train_labels_images_dicts_list[partition_id]
                # Copy their Respective Images Files (X_Train).
                self.copy_images_files_to_partition(partition_id,
                                                    partition_labels_images_dict,
                                                    x_phase)
                # Generate their Respective Images Labels Files (Y_Train).
                self.generate_images_labels_file_to_partition(partition_id,
                                                              partition_labels_images_dict,
                                                              y_phase)
            if plot_data_distribution_graphs or save_data_distribution_graphs:
                # Build the Graph of the N Training Partitions Generated.
                self.build_partitions_classification_graph(y_phase,
                                                           y_train_labels_images_dicts_list,
                                                           number_of_classes,
                                                           partition_mode,
                                                           plot_data_distribution_graphs,
                                                           save_data_distribution_graphs,
                                                           output_root_folder)
            # Set the X and Y Phases to Testing.
            x_phase = "test"
            y_phase = "test"
            # Generate the Input Dataset's Y_Test Dict (K:Labels, V:Images Paths).
            y_test_labels_images_dict = self.generate_labels_images_dict(y_phase)
            # Split the Input Dataset's Y_Test Dict into N Testing Partitions.
            y_test_labels_images_dicts_list = self.partition_labels_images_dict(y_test_labels_images_dict)
            # Iterate the N Testing Partitions Generated.
            for partition_id in range(0, len(y_test_labels_images_dicts_list)):
                partition_labels_images_dict = y_test_labels_images_dicts_list[partition_id]
                # Copy their Respective Images Files (X_Test).
                self.copy_images_files_to_partition(partition_id,
                                                    partition_labels_images_dict,
                                                    x_phase)
                # Generate their Respective Images Labels Files (Y_Test).
                self.generate_images_labels_file_to_partition(partition_id,
                                                              partition_labels_images_dict,
                                                              y_phase)
            if plot_data_distribution_graphs or save_data_distribution_graphs:
                # Build the Graph of the N Testing Partitions Generated.
                self.build_partitions_classification_graph(y_phase,
                                                           y_test_labels_images_dicts_list,
                                                           number_of_classes,
                                                           partition_mode,
                                                           plot_data_distribution_graphs,
                                                           save_data_distribution_graphs,
                                                           output_root_folder)
        message = "The Input Dataset Was Split Successfully!"
        self.log_message(message, "INFO")


def main() -> None:
    # Begin.
    # Parse Dataset Splitter Arguments.
    ag = ArgumentParser(description="Dataset Splitter Arguments")
    ag.add_argument("--dataset_splitter_config_file",
                    type=Path,
                    required=True,
                    help="Dataset Splitter's Config File (no default)")
    parsed_args = ag.parse_args()
    # Get Dataset Splitter Arguments.
    dataset_splitter_config_file = Path(parsed_args.dataset_splitter_config_file)
    # Init Dataset Splitter Object.
    ds = DatasetSplitter(dataset_splitter_config_file)
    # Parse Dataset Splitter Config File.
    ds.parse_dataset_splitter_config_file()
    # Instantiate and Set Logger.
    logger = ds.load_logger()
    ds.set_attribute("logger", logger)
    # Fetch and Save Input Dataset (If 'fetch_dataset_from_source' is True).
    ds.fetch_and_save_input_dataset()
    # Set Training (x_train, y_train) and Testing (x_test, y_test) Input Folders and Files Paths.
    ds.set_input_dataset_training_and_testing_paths()
    # Split the Input Dataset.
    ds.split_input_dataset()
    # Unbind Objects (Garbage Collector).
    del ag
    del ds
    # End.
    exit(0)


if __name__ == "__main__":
    main()

