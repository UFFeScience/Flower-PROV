from argparse import ArgumentParser
from configparser import ConfigParser
from flwr.common import Metrics, NDArrays, Parameters, ndarrays_to_parameters
from flwr.server import Server, ServerConfig, SimpleClientManager, start_server
from flwr.server.strategy import FedAvg, FedAvgM, Strategy
from logging import FileHandler, Formatter, getLevelName, Logger, StreamHandler
from pathlib import Path
from pymonetdb import connect
from random import choice
from re import findall
from time import perf_counter
from typing import Any, List, Optional, Tuple

from dfa_lib_python.dataflow import Dataflow
from dfa_lib_python.transformation import Transformation
from dfa_lib_python.attribute import Attribute
from dfa_lib_python.attribute_type import AttributeType
from dfa_lib_python.set import Set
from dfa_lib_python.set_type import SetType
from dfa_lib_python.task import Task
from dfa_lib_python.dataset import DataSet
from dfa_lib_python.element import Element
from dfa_lib_python.task_status import TaskStatus
from dfa_lib_python.extractor_extension import ExtractorExtension
from dfa_lib_python.dependency import Dependency
import time

from pymongo import MongoClient
from bson.binary import Binary
import pickle


dataflow_tag = "flower-df"


class FlowerServer:
    def __init__(self, experiment_id: int, server_config_file: Path) -> None:
        # Server's ID and Config File.
        self.experiment_id = experiment_id
        self.server_id = None
        self.server_config_file = server_config_file
        # Server's Config File Settings.
        self.general_settings = None
        self.logging_settings = None
        self.fl_settings = None
        self.ssl_settings = None
        self.grpc_settings = None
        self.training_hyper_parameters_settings = None
        self.testing_hyper_parameters_settings = None
        self.hyper_parameters_dynamic_adjustment_settings = None
        self.adjustments_policies_settings = None
        self.training_hyper_parameters_dynamic_adjustment_settings = None
        self.testing_hyper_parameters_dynamic_adjustment_settings = None
        self.monetdb_settings = None
        # Other Attributes.
        self.logger = None
        self.flower_server = None
        self.flower_server_config = None
        self.fl_round = None
        self.fit_config = None
        self.evaluate_config = None
        self.initial_global_model_parameters = None
        self.global_model_parameters = None

    @staticmethod
    def parse_config_section(config_parser: ConfigParser, section_name: str) -> dict:
        parsed_section = {
            key: value for key, value in config_parser[section_name].items()
        }
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
                aux_list = (
                    value.replace("[", "").replace("]", "").replace(" ", "").split(",")
                )
                for index, item in enumerate(aux_list):
                    if item.isdigit():
                        aux_list[index] = int(item)
                    elif item.replace(".", "", 1).isdigit():
                        aux_list[index] = float(item)
                parsed_section[key] = aux_list
            elif not findall(r"%\(.*?\)s+", value) and findall(r"\(.*?\)+", value):
                aux_list = (
                    value.replace("(", "").replace(")", "").replace(" ", "").split(",")
                )
                for index, item in enumerate(aux_list):
                    if item.isdigit():
                        aux_list[index] = int(item)
                    elif item.replace(".", "", 1).isdigit():
                        aux_list[index] = float(item)
                parsed_section[key] = tuple(aux_list)
            elif not findall(r"%\(.*?\)s+", value) and findall(r"\{.*?}+", value):
                aux_dict = {}
                aux_list = (
                    value.replace("{", "").replace("}", "").replace(" ", "").split(",")
                )
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

    def set_attribute(self, attribute_name: str, attribute_value: Any) -> None:
        setattr(self, attribute_name, attribute_value)

    def get_attribute(self, attribute_name: str) -> Any:
        return getattr(self, attribute_name)

    def parse_flower_server_config_file(self) -> None:
        # Get Server's Config File.
        server_config_file = self.get_attribute("server_config_file")
        # Init ConfigParser Object.
        cp = ConfigParser()
        cp.optionxform = str
        cp.read(filenames=server_config_file, encoding="utf-8")
        # Parse 'General Settings' and Set Attributes.
        general_settings = self.parse_config_section(cp, "General Settings")
        self.set_attribute("general_settings", general_settings)
        # If Logging is Enabled...
        if general_settings["enable_logging"]:
            # Parse 'Logging Settings' and Set Attributes.
            logging_settings = self.parse_config_section(cp, "Logging Settings")
            self.set_attribute("logging_settings", logging_settings)
        # Parse 'FL Settings' and Set Attributes.
        fl_settings = self.parse_config_section(cp, "FL Settings")

        checkpoints_settings = cp["Checkpoints Settings"]
        self.set_attribute("checkpoints_settings", checkpoints_settings)

        monetdb_settings = cp["MonetDB Settings"]
        self.set_attribute("monetdb_settings", monetdb_settings)

        mongodb_settings = cp["MongoDB Settings"]
        self.set_attribute("mongodb_settings", mongodb_settings)

        conn = self.get_connection_monetdb()
        cursor = conn.cursor()

        cursor.execute(f"SELECT check_max_experiment_id();")

        conn.commit()

        max_experiment_id = cursor.fetchone()[0]
        self.server_id = 0

        if max_experiment_id != None:

            self.experiment_id = max_experiment_id + 1

            cursor.execute(f"SELECT check_ending_fl({self.experiment_id - 1});")

            conn.commit()
            ending_fl = cursor.fetchone()[0]

            if ending_fl == False:
                cursor.execute(f"SELECT check_max_server_id({self.experiment_id-1});")
                conn.commit()
                server_id = cursor.fetchone()[0]
                self.server_id = server_id + 1
                # cursor.execute(f"SELECT check_last_round_fl({self.experiment_id - 1});")
                # conn.commit()
                # last_round = cursor.fetchone()[0]
                db = self.get_connection_mongodb()
                result = list(
                    db.checkpoints.find(
                        {"experiment_id": {"$eq": self.experiment_id - 1}}
                    )
                    .sort([("round", -1), ("server_id", -1)])
                    .limit(1)
                )
                if result:
                    cursor.execute(
                        f"SELECT get_num_checkpoints({self.experiment_id-1})"
                    )
                    # get count for checkpoints saved to mongodb and multiply by checkpoints_frequency to get the last round
                    num_checkpoints = cursor.fetchone()[0]
                    num_rounds = num_checkpoints * int(
                        checkpoints_settings["checkpoint_frequency"]
                    )
                    fl_settings["num_rounds"] -= int(num_rounds)
                self.experiment_id -= 1
        else:
            self.experiment_id = 0

        cursor.close()
        conn.close()

        attributes = ["action", "min_clients_per_checkpoint"]
        to_dfanalyzer = [checkpoints_settings.get(attr, None) for attr in attributes]

        attributes = [
            "num_rounds",
            "round_timeout_in_seconds",
            "accept_rounds_containing_failures",
            "enable_ssl",
            "enable_dynamic_adjustment",
            "server_aggregation_strategy",
            "fraction_fit",
            "fraction_evaluate",
            "min_fit_clients",
            "min_evaluate_clients",
            "min_available_clients",
        ]

        to_dfanalyzer += [fl_settings.get(attr, None) for attr in attributes]

        t1 = Task(1, dataflow_tag, "ServerConfig")

        t1.begin()

        self.set_attribute("fl_settings", fl_settings)

        # If SSL is Enabled...
        if fl_settings["enable_ssl"]:
            # Parse 'SSL Settings' and Set Attributes.
            ssl_settings = self.parse_config_section(cp, "SSL Settings")
            self.set_attribute("ssl_settings", ssl_settings)
        # Parse 'gRPC Settings' and Set Attributes.
        grpc_settings = self.parse_config_section(cp, "gRPC Settings")
        self.set_attribute("grpc_settings", grpc_settings)

        t1_input = DataSet(
            "iServerConfig",
            [
                Element(
                    [
                        self.experiment_id,
                        self.server_id,
                        str(grpc_settings["grpc_listen_ip_address"])
                        + str(grpc_settings["grpc_listen_port"]),
                        grpc_settings["grpc_max_message_length_in_bytes"],
                    ]
                    + to_dfanalyzer
                )
            ],
        )
        t1.add_dataset(t1_input)
        t1_output = DataSet("oServerConfig", [Element([])])
        t1.add_dataset(t1_output)
        t1.end()
        # Parse 'Training Hyper-parameters Settings' and Set Attributes.
        training_hyper_parameters_settings = self.parse_config_section(
            cp, "Training Hyper-parameters Settings"
        )
        self.set_attribute(
            "training_hyper_parameters_settings", training_hyper_parameters_settings
        )
        # Parse 'Testing Hyper-parameters Settings' and Set Attributes.
        testing_hyper_parameters_settings = self.parse_config_section(
            cp, "Testing Hyper-parameters Settings"
        )
        self.set_attribute(
            "testing_hyper_parameters_settings", testing_hyper_parameters_settings
        )
        # If Dynamic Adjustment of Hyper-parameters is Enabled...
        if fl_settings["enable_hyper_parameters_dynamic_adjustment"]:
            # Parse 'Hyper-parameters Dynamic Adjustment Settings' and Set Attributes.
            hyper_parameters_dynamic_adjustment_settings = self.parse_config_section(
                cp, "Hyper-parameters Dynamic Adjustment Settings"
            )
            self.set_attribute(
                "hyper_parameters_dynamic_adjustment_settings",
                hyper_parameters_dynamic_adjustment_settings,
            )
            # Parse 'Adjustments Policies Settings' and Set Attributes.
            adjustments_policies_settings = self.parse_config_section(
                cp, "Adjustments Policies Settings"
            )
            self.set_attribute(
                "adjustments_policies_settings", adjustments_policies_settings
            )
            # If Dynamic Adjustment of Training Hyper-parameters is Enabled...
            if hyper_parameters_dynamic_adjustment_settings[
                "dynamically_adjust_training_hyper_parameters"
            ]:
                # Parse 'Training Hyper-parameters Dynamic Adjustment Settings' and Set Attributes.
                training_hyper_parameters_dynamic_adjustment_settings = (
                    self.parse_config_section(
                        cp, "Training Hyper-parameters Dynamic Adjustment Settings"
                    )
                )
                self.set_attribute(
                    "training_hyper_parameters_dynamic_adjustment_settings",
                    training_hyper_parameters_dynamic_adjustment_settings,
                )
            # If Dynamic Adjustment of Testing Hyper-parameters is Enabled...
            if hyper_parameters_dynamic_adjustment_settings[
                "dynamically_adjust_testing_hyper_parameters"
            ]:
                # Parse 'Testing Hyper-parameters Dynamic Adjustment Settings' and Set Attributes.
                testing_hyper_parameters_dynamic_adjustment_settings = (
                    self.parse_config_section(
                        cp, "Testing Hyper-parameters Dynamic Adjustment Settings"
                    )
                )
                self.set_attribute(
                    "testing_hyper_parameters_dynamic_adjustment_settings",
                    testing_hyper_parameters_dynamic_adjustment_settings,
                )
            # If MonetDB is the Hyper-parameters Adjustments Eligibility Controller...
            # if (
            #     hyper_parameters_dynamic_adjustment_settings[
            #         "adjustments_eligibility_controller"
            #     ]
            #     == "MonetDB"
            # ):

            #     ###########################
            #     # -------- MODIFIED --------
            #     # Parse 'MonetDB Settings' and Set Attributes.
            #     monetdb_settings = cp["MonetDB Settings"]
            #     self.set_attribute("monetdb_settings", monetdb_settings)
            #     ###########################

        # Unbind ConfigParser Object (Garbage Collector).
        del cp

    def load_logger(self) -> Optional[Logger]:
        logger = None
        general_settings = self.get_attribute("general_settings")
        if general_settings["enable_logging"]:
            logger_name = "FlowerServer_" + str(self.get_attribute("experiment_id"))
            logging_settings = self.get_attribute("logging_settings")
            logger = Logger(name=logger_name, level=logging_settings["level"])
            formatter = Formatter(
                fmt=logging_settings["format"], datefmt=logging_settings["date_format"]
            )
            if logging_settings["log_to_file"]:
                file_parents_path = findall("(.*/)", logging_settings["file_name"])
                if file_parents_path:
                    Path(file_parents_path[0]).mkdir(parents=True, exist_ok=True)
                file_handler = FileHandler(
                    filename=logging_settings["file_name"],
                    mode=logging_settings["file_mode"],
                    encoding=logging_settings["encoding"],
                )
                file_handler.setLevel(logger.getEffectiveLevel())
                file_handler.setFormatter(formatter)
                logger.addHandler(file_handler)
            if logging_settings["log_to_console"]:
                console_handler = StreamHandler()
                console_handler.setLevel(logger.getEffectiveLevel())
                console_handler.setFormatter(formatter)
                logger.addHandler(console_handler)
        return logger

    def log_message(self, message: str, message_level: str) -> None:
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

    def load_initial_global_model_parameters(self, id_weights_mongo):
        """Server-side parameter initialization. A powerful mechanism which can be used, for example:
        \n - To resume the training from a previously saved checkpoint;
        \n - To implement hybrid approaches, such as to fine-tune a pre-trained model using federated learning.
        \n If no parameters are returned, the server will randomly select one client and ask its parameters.
        """

        starting_time = time.ctime()
        loading_parameters_start = perf_counter()
        t2 = Task(2, dataflow_tag, "LoadGlobalWeights")
        t2.begin()

        params = None
        if self.server_id != 0:

            connection = self.get_connection_monetdb()
            cursor = connection.cursor()
            cursor.execute(f"SELECT check_ending_fl({self.experiment_id});")
            connection.commit()
            ending_fl = cursor.fetchone()[0]

            if ending_fl == False:

                # cursor.execute(f"SELECT check_last_round_fl({self.experiment_id});")
                # connection.commit()
                db = self.get_connection_mongodb()
                # last_round = cursor.fetchone()[0]
                # pesos = db.checkpoints.find_one({"$and": [{"round": {"$eq": last_round}}, {"experiment_id": {"$eq": self.experiment_id}}]})
                pesos = list(
                    db.checkpoints.find({"experiment_id": {"$eq": self.experiment_id}})
                    .sort([("round", -1), ("server_id", -1)])
                    .limit(1)
                )
                if pesos:
                    params = pickle.loads(pesos[0]["global_weights"])
                    message = "[Server {0} | FL Round {1}] Loaded Global Weights from Server {2} - round {3}.".format(
                        self.get_attribute("server_id"),
                        self.get_attribute("fl_round"),
                        self.server_id - 1,
                        pesos[0]["round"],
                    )
                    self.log_message(message, "INFO")
                else:
                    message = "[Server {0} | FL Round {1}] No Global Weights Found for Server {2}.".format(
                        self.get_attribute("server_id"),
                        self.get_attribute("fl_round"),
                        self.server_id - 1,
                    )
                    self.log_message(message, "INFO")

            cursor.close()
            connection.close()
        if id_weights_mongo:
            db = self.get_connection_mongodb()
            pesos = db.checkpoints.find_one({"_id": {"$eq": id_weights_mongo}})
            if pesos:
                params = pickle.loads(pesos["global_weights"])
                message = "[Server {0} | FL Round {1}] Loaded Global Weights from Server {2} - round {3}.".format(
                    self.get_attribute("server_id"),
                    self.get_attribute("fl_round"),
                    pesos["server_id"],
                    pesos["round"],
                )
                self.log_message(message, "INFO")
            else:
                message = "[Server {0} | FL Round {1}] No Global Weights Found.".format(
                    self.get_attribute("server_id"), self.get_attribute("fl_round")
                )
                self.log_message(message, "INFO")
        ending_time = time.ctime()
        loading_parameters_end = perf_counter() - loading_parameters_start

        to_dfanalyzer = [
            self.get_attribute("experiment_id"),
            self.get_attribute("server_id"),
            starting_time,
            ending_time,
            loading_parameters_end,
        ]
        t2_input = DataSet("iLoadGlobalWeights", [Element(to_dfanalyzer)])
        t2.add_dataset(t2_input)
        to_dfanalyzer = [bool(params)]
        t2_output = DataSet("oLoadGlobalWeights", [Element(to_dfanalyzer)])
        t2.add_dataset(t2_output)
        t2.end()

        return ndarrays_to_parameters(params) if params else None

    def load_initial_fit_config(self) -> dict:
        training_hyper_parameters_settings = self.get_attribute(
            "training_hyper_parameters_settings"
        )
        fit_config = {"fl_round": 0, "server_id": 0}
        fit_config.update(training_hyper_parameters_settings)
        # Log the Initial Training Configuration (If Logger is Enabled for "DEBUG" Level).
        message = "[Server {0} | FL Round {1}] Initial Fit Config: {2}".format(
            self.get_attribute("server_id"), fit_config["fl_round"], fit_config
        )
        self.log_message(message, "DEBUG")
        return fit_config

    def load_initial_evaluate_config(self) -> dict:
        testing_hyper_parameters_settings = self.get_attribute(
            "testing_hyper_parameters_settings"
        )
        evaluate_config = {"fl_round": 0, "server_id": 0}
        evaluate_config.update(testing_hyper_parameters_settings)
        # Log the Initial Testing Configuration (If Logger is Enabled for "DEBUG" Level).
        message = "[Server {0} | FL Round {1}] Initial Evaluate Config: {2}".format(
            self.get_attribute("server_id"),
            evaluate_config["fl_round"],
            evaluate_config,
        )
        self.log_message(message, "DEBUG")
        return evaluate_config

    def get_grpc_listen_ip_address_and_port(self) -> str:
        grpc_settings = self.get_attribute("grpc_settings")
        return (
            grpc_settings["grpc_listen_ip_address"]
            + ":"
            + str(grpc_settings["grpc_listen_port"])
        )

    def get_grpc_max_message_length_in_bytes(self) -> int:
        return self.get_attribute("grpc_settings")["grpc_max_message_length_in_bytes"]

    @staticmethod
    def instantiate_simple_client_manager() -> SimpleClientManager:
        return SimpleClientManager()

    # @staticmethod
    def evaluate_fn(
        self, fl_round: int, global_model_parameters: NDArrays, evaluate_config: dict
    ) -> Optional[Metrics]:
        """Server-side (Centralized) evaluation function called by Flower after every training round.
        \nRequires a server-side dataset to evaluate the newly aggregated model without sending it to the Clients.
        \nThe 'losses_centralized' and 'metrics_centralized' will only contain values using this centralized evaluation.
        \nAlternative: Client-side (Federated) evaluation."""

        self.global_model_parameters = global_model_parameters
        return None

    def is_enabled_hyper_parameters_dynamic_adjustment(self, phase: str) -> bool:
        hyper_parameters_dynamic_adjustment_settings = self.get_attribute(
            "hyper_parameters_dynamic_adjustment_settings"
        )
        if hyper_parameters_dynamic_adjustment_settings:
            if phase == "train":
                return hyper_parameters_dynamic_adjustment_settings[
                    "dynamically_adjust_training_hyper_parameters"
                ]
            elif phase == "test":
                return hyper_parameters_dynamic_adjustment_settings[
                    "dynamically_adjust_testing_hyper_parameters"
                ]
        return False

    def execute_random_eligibility(self, phase: str) -> bool:
        random_eligibility_start = perf_counter()
        is_fl_round_eligible = choice([True, False])
        random_eligibility_end = perf_counter() - random_eligibility_start
        message = "[Server {0}] Finished Executing the Random Eligibility ({1}ing Phase) in {2} Seconds.".format(
            self.get_attribute("server_id"), phase.capitalize(), random_eligibility_end
        )
        self.log_message(message, "INFO")
        return is_fl_round_eligible

    def execute_monetdb_eligibility_query(self, phase: str) -> bool:
        monetdb_eligibility_query_start = perf_counter()
        monetdb_settings = self.get_attribute("monetdb_settings")
        adjustments_eligibility_query = None
        if phase == "train":
            adjustments_eligibility_query = monetdb_settings[
                "training_adjustments_eligibility_query"
            ]
        elif phase == "test":
            adjustments_eligibility_query = monetdb_settings[
                "testing_adjustments_eligibility_query"
            ]
        if adjustments_eligibility_query is None:
            return False

        connection = self.get_connection_monetdb()
        cursor = connection.cursor()

        result = None
        tries = 0
        fl_round = self.get_attribute("fl_round")
        while not result:
            query = f"""SELECT check_if_last_round_is_already_recorded({self.experiment_id},{self.server_id},{fl_round})"""
            cursor.execute(operation=query)
            result = cursor.fetchone()
            connection.commit()

            if result:
                result = result[-1]
            tries += 1
            time.sleep(0.05)

        if result:
            cursor.execute(operation=adjustments_eligibility_query.format(fl_round))
            query_result = int(cursor.fetchone()[0])
        else:
            query_result = 0

        cursor.close()
        connection.close()
        is_fl_round_eligible = True if query_result == 1 else False
        monetdb_eligibility_query_end = perf_counter() - monetdb_eligibility_query_start
        message = "[Server {0}] Finished Executing the MonetDB Eligibility Query ({1}ing Phase) in {2} Seconds.".format(
            self.get_attribute("server_id"),
            phase.capitalize(),
            monetdb_eligibility_query_end,
        )
        self.log_message(message, "INFO")
        return is_fl_round_eligible

    def is_fl_round_eligible_for_hyper_parameters_dynamic_adjustment(
        self, phase: str
    ) -> bool:
        hyper_parameters_dynamic_adjustment_settings = self.get_attribute(
            "hyper_parameters_dynamic_adjustment_settings"
        )
        initial_round_candidate_for_adjustments = (
            hyper_parameters_dynamic_adjustment_settings[
                "initial_round_candidate_for_adjustments"
            ]
        )
        fl_round = self.get_attribute("fl_round")
        if fl_round < initial_round_candidate_for_adjustments:
            return False
        adjustments_eligibility_controller = (
            hyper_parameters_dynamic_adjustment_settings[
                "adjustments_eligibility_controller"
            ]
        )
        if adjustments_eligibility_controller == "Random":
            return self.execute_random_eligibility(phase)
        if adjustments_eligibility_controller == "MonetDB":
            return self.execute_monetdb_eligibility_query(phase)

    def adjust_hyper_parameter_value(
        self, old_value: Any, adjustment_policy: str
    ) -> Any:
        adjusted_value = None
        adjustment_operation_text = self.get_attribute("adjustments_policies_settings")[
            adjustment_policy
        ]
        if "boolean" in adjustment_policy:
            if adjustment_operation_text == "Flip":
                adjusted_value = not old_value
        elif "numerical" in adjustment_policy:
            factor = findall(r"[-+]?\d*\.?\d+|[-+]?\d+", adjustment_operation_text)[0]
            if factor.isdigit():
                factor = int(factor)
            elif factor.replace(".", "", 1).isdigit():
                factor = float(factor)
            operation_text = " ".join(findall(r"[a-zA-Z]+", adjustment_operation_text))
            if operation_text == "Increment by":
                adjusted_value = old_value + factor
            elif operation_text == "Decrement by":
                adjusted_value = old_value - factor
            elif operation_text == "Multiply by":
                adjusted_value = old_value * factor
            elif operation_text == "Divide by":
                adjusted_value = old_value / factor

            if type(old_value) == int:
                adjusted_value = int(adjusted_value)

        return adjusted_value

    def dynamically_adjust_hyper_parameters(self, phase: str, config: dict) -> dict:
        config_name = None
        hyper_parameters_to_adjust = None
        if phase == "train":
            config_name = "Fit Config"
            hyper_parameters_to_adjust = self.get_attribute(
                "training_hyper_parameters_dynamic_adjustment_settings"
            )["to_adjust"]
        elif phase == "test":
            config_name = "Evaluate Config"
            hyper_parameters_to_adjust = self.get_attribute(
                "testing_hyper_parameters_dynamic_adjustment_settings"
            )["to_adjust"]
        adjustments_policies_settings = self.get_attribute(
            "adjustments_policies_settings"
        )
        if hyper_parameters_to_adjust:
            for (
                hyper_parameter,
                adjustment_policy,
            ) in hyper_parameters_to_adjust.items():
                if (
                    hyper_parameter in config
                    and adjustment_policy in adjustments_policies_settings
                ):
                    hyper_parameter_old_value = config[hyper_parameter]
                    hyper_parameter_new_value = self.adjust_hyper_parameter_value(
                        hyper_parameter_old_value, adjustment_policy
                    )
                    config.update({hyper_parameter: hyper_parameter_new_value})
            # Log the Dynamic Configuration Adjustment Notice (If Logger is Enabled for "INFO" Level).
            message = "[Server {0} | FL Round {1}] {2} Dynamically Adjusted (Eligible FL Round).".format(
                self.get_attribute("server_id"),
                self.get_attribute("fl_round"),
                config_name,
            )
            self.log_message(message, "INFO")
        return config

    def get_connection_mongodb(self):
        client = MongoClient(
            host=self.mongodb_settings["hostname"],
            port=int(self.mongodb_settings["port"]),
        )
        return client.flowerprov

    def get_connection_monetdb(self):
        connection = connect(
            hostname=self.monetdb_settings["hostname"],
            port=self.monetdb_settings["port"],
            username=self.monetdb_settings["username"],
            password=self.monetdb_settings["password"],
            database=self.monetdb_settings["database"],
        )
        return connection

    def on_fit_config_fn(self, fl_round: int) -> Optional[dict]:
        """Training configuration function called by Flower before each training round."""
        # Update the Current FL Round (Necessary Workaround on Flower v1.1.0).
        starting_time = time.ctime()

        self.set_attribute("fl_round", fl_round)
        # Log the Current FL Round (If Logger is Enabled for "INFO" Level).
        message = "[Server {0}] Current FL Round: {1}".format(
            self.get_attribute("server_id"), self.get_attribute("fl_round")
        )
        self.log_message(message, "INFO")
        # Get the Training Configuration.
        fit_config = self.get_attribute("fit_config")
        # Update the Training Configuration's Current FL Round.
        fit_config.update(
            {
                "fl_round": self.get_attribute("fl_round"),
                "experiment_id": self.get_attribute("experiment_id"),
                "server_id": self.get_attribute("server_id"),
            }
        )
        # Dynamically Adjust the Training Configuration's Hyper-parameters (If Enabled and Eligible).
        dynamically_adjusted = False
        if self.is_enabled_hyper_parameters_dynamic_adjustment("train"):
            if self.is_fl_round_eligible_for_hyper_parameters_dynamic_adjustment(
                "train"
            ):
                fit_config = self.dynamically_adjust_hyper_parameters(
                    "train", fit_config
                )
                dynamically_adjusted = True

        # Store the Training Configuration Changes.
        self.set_attribute("fit_config", fit_config)
        # Log the Training Configuration (If Logger is Enabled for "DEBUG" Level).
        message = "[Server {0} | FL Round {1}] Fit Config: {2}".format(
            self.get_attribute("server_id"), fit_config["fl_round"], fit_config
        )
        self.log_message(message, "DEBUG")
        # Replace All Values of None Type to "None" String (Necessary Workaround on Flower v1.1.0).
        fit_config = {k: ("None" if v is None else v) for k, v in fit_config.items()}

        t8 = Task(8 + 6 * (fl_round - 1), dataflow_tag, "TrainingConfig")
        if fl_round == 1:
            t8.add_dependency(
                Dependency(
                    [
                        "serverconfig",
                        "loadglobalweights",
                        "strategy",
                        "serverevaluationaggregation",
                    ],
                    ["1", "2", "3", "0"],
                )
            )
        else:
            t8.add_dependency(
                Dependency(
                    [
                        "serverconfig",
                        "loadglobalweights",
                        "strategy",
                        "serverevaluationaggregation",
                    ],
                    ["1", "2", "3", str(13 + 6 * (fl_round - 2))],
                )
            )

        t8.begin()

        attributes = [
            "shuffle",
            "batch_size",
            "initial_epoch",
            "epochs",
            "steps_per_epoch",
            "validation_split",
            "validation_batch_size",
        ]

        to_dfanalyzer = [
            self.get_attribute("experiment_id"),
            self.get_attribute("server_id"),
            fl_round,
            starting_time,
            time.ctime(),
        ] + [fit_config.get(attr, 0) for attr in attributes]

        t8_input = DataSet("iTrainingConfig", [Element(to_dfanalyzer)])

        t8.add_dataset(t8_input)
        t8_output = DataSet(
            "oTrainingConfig",
            [
                Element(
                    [
                        self.get_attribute("experiment_id"),
                        self.get_attribute("server_id"),
                        fl_round,
                        dynamically_adjusted,
                    ]
                )
            ],
        )
        t8.add_dataset(t8_output)
        t8.end()

        mongodb = {
            "hostname": self.mongodb_settings["hostname"],
            "port": self.mongodb_settings["port"],
        }

        monetdb = {
            "hostname": self.monetdb_settings["hostname"],
            "port": self.monetdb_settings["port"],
            "username": self.monetdb_settings["username"],
            "password": self.monetdb_settings["password"],
            "database": self.monetdb_settings["database"],
        }

        fit_config.update(
            {
                "action": self.checkpoints_settings["action"],
                "checkpoint_frequency": self.checkpoints_settings[
                    "checkpoint_frequency"
                ],
                "experiment_id": self.get_attribute("experiment_id"),
                "monetdb_hostname": monetdb["hostname"],
                "monetdb_port": monetdb["port"],
                "monetdb_username": monetdb["username"],
                "monetdb_password": monetdb["password"],
                "monetdb_database": monetdb["database"],
                "mongodb_hostname": mongodb["hostname"],
                "mongodb_port": mongodb["port"],
            }
        )
        # Return the Training Configuration to be Sent to All Participating Clients.
        return fit_config

    def on_evaluate_config_fn(self, fl_round: int) -> Optional[dict]:
        """Testing configuration function called by Flower before each testing round."""
        # Update the Current FL Round (Necessary Workaround on Flower v1.1.0).
        self.set_attribute("fl_round", fl_round)
        # Log the Current FL Round (If Logger is Enabled for "INFO" Level).
        message = "[Server {0}] Current FL Round: {1}".format(
            self.get_attribute("server_id"), self.get_attribute("fl_round")
        )
        self.log_message(message, "INFO")
        # Get the Testing Configuration.
        evaluate_config = self.get_attribute("evaluate_config")
        # Update the Testing Configuration's Current FL Round.
        evaluate_config.update(
            {
                "fl_round": self.get_attribute("fl_round"),
                "experiment_id": self.get_attribute("experiment_id"),
                "server_id": self.get_attribute("server_id"),
            }
        )
        # Dynamically Adjust the Testing Configuration's Hyper-parameters (If Enabled and Eligible).
        if self.is_enabled_hyper_parameters_dynamic_adjustment("test"):
            if self.is_fl_round_eligible_for_hyper_parameters_dynamic_adjustment(
                "test"
            ):
                evaluate_config = self.dynamically_adjust_hyper_parameters(
                    "test", evaluate_config
                )
        # Store the Testing Configuration Changes.
        self.set_attribute("evaluate_config", evaluate_config)
        # Log the Testing Configuration (If Logger is Enabled for "DEBUG" Level).
        message = "[Server {0} | FL Round {1}] Evaluate Config: {2}".format(
            self.get_attribute("server_id"),
            evaluate_config["fl_round"],
            evaluate_config,
        )
        self.log_message(message, "DEBUG")
        # Replace All Values of None Type to "None" String (Necessary Workaround on Flower v1.1.0).
        evaluate_config = {
            k: ("None" if v is None else v) for k, v in evaluate_config.items()
        }
        # Return the Testing Configuration to be Sent to All Participating Clients.

        t11 = Task(
            11 + 6 * (fl_round - 1),
            dataflow_tag,
            "EvaluationConfig",
            dependency=Task(
                10 + 6 * (fl_round - 1), dataflow_tag, "ServerTrainingAggregation"
            ),
        )
        t11.begin()
        attributes = ["batch_size", "steps"]
        to_dfanalyzer = [
            self.get_attribute("experiment_id"),
            self.get_attribute("server_id"),
            fl_round,
        ] + [evaluate_config.get(attr, 0) for attr in attributes]

        t11_input = DataSet("iEvaluationConfig", [Element(to_dfanalyzer)])
        t11.add_dataset(t11_input)
        t11_output = DataSet("oEvaluationConfig", [Element([])])
        t11.add_dataset(t11_output)
        t11.end()

        mongodb = {
            "hostname": self.mongodb_settings["hostname"],
            "port": self.mongodb_settings["port"],
        }

        monetdb = {
            "hostname": self.monetdb_settings["hostname"],
            "port": self.monetdb_settings["port"],
            "username": self.monetdb_settings["username"],
            "password": self.monetdb_settings["password"],
            "database": self.monetdb_settings["database"],
        }

        evaluate_config.update(
            {
                "action": self.checkpoints_settings["action"],
                "checkpoint_frequency": self.checkpoints_settings[
                    "checkpoint_frequency"
                ],
                "experiment_id": self.get_attribute("experiment_id"),
                "monetdb_hostname": monetdb["hostname"],
                "monetdb_port": monetdb["port"],
                "monetdb_username": monetdb["username"],
                "monetdb_password": monetdb["password"],
                "monetdb_database": monetdb["database"],
                "mongodb_hostname": mongodb["hostname"],
                "mongodb_port": mongodb["port"],
            }
        )

        # Return the Testing Configuration to be Sent to All Participating Clients.
        return evaluate_config

    def save_checkpoint(self, payload):
        starting_time = perf_counter()
        db = self.get_connection_mongodb()
        _id = db.checkpoints.insert_one(payload)
        mongo_id = _id.inserted_id
        insertion_time = perf_counter() - starting_time

        return mongo_id, insertion_time

    def fit_metrics_aggregation_fn(
        self, training_metrics: List[Tuple[int, Metrics]]
    ) -> Optional[Metrics]:
        """Metrics aggregation function called by Flower after every training round."""
        t10 = Task(
            10 + 6 * (self.get_attribute("fl_round") - 1),
            dataflow_tag,
            "ServerTrainingAggregation",
            dependency=Task(
                9 + 6 * (self.get_attribute("fl_round") - 1),
                dataflow_tag,
                "ClientTraining",
            ),
        )
        starting_time = time.ctime()
        t10.begin()

        # Get the Total Number of Participating Clients.
        total_num_clients = len(training_metrics)
        # Get the Training Metrics Names.
        metrics_names_list = list(training_metrics[0][1].keys())
        # Multiply Each Training Metrics Value of Each Participating Client
        # By His Number of Training Examples (Client's Contribution).
        metrics_products_list = []
        for metric_name in metrics_names_list:
            metric_product = [
                num_examples * metric[metric_name]
                for num_examples, metric in training_metrics
            ]
            metrics_products_list.append(metric_product)
        # Get the Total Number of Training Examples (of All Participating Clients).
        total_num_examples = sum([num_examples for num_examples, _ in training_metrics])
        # Aggregate the Training Metrics (Weighted Average).
        aggregated_metrics = {}
        for metric_index in range(0, len(metrics_names_list)):
            metric_name = metrics_names_list[metric_index]
            weighted_average_metric = (
                sum(metrics_products_list[metric_index]) / total_num_examples
            )
            aggregated_metrics[metric_name] = weighted_average_metric

        # Log the Aggregated Training Metrics (If Logger is Enabled for "INFO" Level).
        message = "[Server {0} | FL Round {1} | {2}] Aggregated Training Metrics (Weighted Average): {3}".format(
            self.get_attribute("server_id"),
            self.get_attribute("fl_round"),
            "".join(
                [
                    str(total_num_clients),
                    " Clients" if total_num_clients > 1 else " Client",
                ]
            ),
            aggregated_metrics,
        )
        self.log_message(message, "INFO")

        ### Check client loss

        fl_round = self.get_attribute("fl_round")
        experiment_id = self.get_attribute("experiment_id")
        server_id = self.get_attribute("server_id")
        client_loss = None
        mongo_id = None
        insertion_time = None
        loaded_weights = False
        consistent = None
        checkpoint_time = None
        if not self.checkpoints_settings["action"].lower().startswith("no"):
            checkpoint_time_start = perf_counter()
            if total_num_clients < int(
                self.checkpoints_settings["min_clients_per_checkpoint"]
            ):
                client_loss = True
            else:
                client_loss = False

            result = None
            connection = self.get_connection_monetdb()
            cursor = connection.cursor()
            checkpoint_frequency = int(
                self.checkpoints_settings["checkpoint_frequency"]
            )
            if (
                fl_round > 2
                and self.checkpoints_settings["action"] == "rollback"
                and (fl_round % checkpoint_frequency == 0)
            ):
                message = f"Checking for rollback in round {fl_round}"
                self.log_message(message, "INFO")
                if not client_loss:
                    query = f"""SELECT get_last_round_load_checkpoint({experiment_id}, {server_id});"""
                    cursor.execute(operation=query)
                    result = cursor.fetchone()
                    last_round_checkpoint = result[0] if result[0] else 1
                    query = f"""SELECT get_client_loss_between_rounds({experiment_id}, {server_id}, {last_round_checkpoint});"""
                    cursor.execute(operation=query)
                    client_loss_between_rounds = bool(cursor.fetchone()[0])
                    if client_loss_between_rounds:
                        db = self.get_connection_mongodb()
                        # pesos = db.checkpoints.find_one({"$and": [{"round": {"$eq": last_round}}, {"experiment_id": {"$eq": experiment_id}}, {"server_id": {"$eq": server_id}}]})
                        pesos = list(
                            db.checkpoints.find(
                                {
                                    "$and": [
                                        {"experiment_id": {"$eq": experiment_id}},
                                        {
                                            "server_id": {"$eq": server_id},
                                            "consistent": {"$eq": True},
                                        },
                                    ]
                                }
                            )
                            .sort([("round", -1)])
                            .limit(1)
                        )
                        if pesos:
                            # params = pickle.loads([p for p in pesos][0]["global_weights"])
                            params = pickle.loads(pesos[0]["global_weights"])

                            message = f"ROLLBACK! Using weights from round {pesos[0]['round']} in round {fl_round}"
                            self.log_message(message, "INFO")
                            self.set_attribute(
                                "global_model_parameters",
                                params,
                            )
                            loaded_weights = True

                        else:
                            message = (
                                f"Couldn't find valid checkpoint for round {fl_round}"
                            )
                            self.log_message(message, "INFO")
                            message = f"Inserting checkpoint for round {fl_round}"
                            self.log_message(message, "INFO")
                            consistent = False
                            checkpoints = {
                                "round": self.get_attribute("fl_round"),
                                "experiment_id": self.get_attribute("experiment_id"),
                                "server_id": self.get_attribute("server_id"),
                                "global_weights": Binary(
                                    pickle.dumps(
                                        self.global_model_parameters, protocol=4
                                    )
                                ),
                                "consistent": consistent,
                            }

                            mongo_id, insertion_time = self.save_checkpoint(checkpoints)

                    else:
                        message = (
                            f"Inserting consistent checkpoint for round {fl_round}"
                        )
                        self.log_message(message, "INFO")
                        consistent = True
                        checkpoints = {
                            "round": self.get_attribute("fl_round"),
                            "experiment_id": self.get_attribute("experiment_id"),
                            "server_id": self.get_attribute("server_id"),
                            "global_weights": Binary(
                                pickle.dumps(self.global_model_parameters, protocol=4)
                            ),
                            "consistent": consistent,
                        }

                        mongo_id, insertion_time = self.save_checkpoint(checkpoints)

                else:
                    message = f"Client missing at round {self.fl_round}! Waiting return to execute rollback."
                    message = f"Inserting checkpoint for round {fl_round}"
                    consistent = False
                    self.log_message(message, "INFO")
                    checkpoints = {
                        "round": self.get_attribute("fl_round"),
                        "experiment_id": self.get_attribute("experiment_id"),
                        "server_id": self.get_attribute("server_id"),
                        "global_weights": Binary(
                            pickle.dumps(self.global_model_parameters, protocol=4)
                        ),
                        "consistent": consistent,
                    }

                    mongo_id, insertion_time = self.save_checkpoint(checkpoints)

                cursor.close()
                connection.close()

            elif self.checkpoints_settings["action"] == "different_models" and (
                fl_round % checkpoint_frequency == 0
            ):
                query = f"""SELECT get_last_round_write_checkpoint({experiment_id}, {server_id});"""
                cursor.execute(operation=query)
                result = cursor.fetchone()
                last_round_checkpoint = result[0] if result[0] else 1
                query = f"""SELECT get_client_loss_between_rounds({experiment_id}, {server_id}, {last_round_checkpoint});"""
                cursor.execute(operation=query)
                client_loss_between_rounds = bool(cursor.fetchone()[0])
                if not client_loss_between_rounds and not client_loss:
                    consistent = True
                    message = f"Inserting checkpoint for round {fl_round}"
                    self.log_message(message, "INFO")
                    checkpoints = {
                        "round": self.get_attribute("fl_round"),
                        "experiment_id": self.get_attribute("experiment_id"),
                        "server_id": self.get_attribute("server_id"),
                        "global_weights": Binary(
                            pickle.dumps(self.global_model_parameters, protocol=4)
                        ),
                        "consistent": consistent,
                    }

                    mongo_id, insertion_time = self.save_checkpoint(checkpoints)

            elif fl_round % checkpoint_frequency == 0:
                consistent = None
                message = f"Inserting checkpoint for round {fl_round}"
                self.log_message(message, "INFO")
                checkpoints = {
                    "round": self.get_attribute("fl_round"),
                    "experiment_id": self.get_attribute("experiment_id"),
                    "server_id": self.get_attribute("server_id"),
                    "global_weights": Binary(
                        pickle.dumps(self.global_model_parameters, protocol=4)
                    ),
                    "consistent": consistent,
                }

                mongo_id, insertion_time = self.save_checkpoint(checkpoints)

            checkpoint_time = perf_counter() - checkpoint_time_start

        to_dfanalyzer = [
            self.get_attribute("experiment_id"),
            self.get_attribute("server_id"),
            self.get_attribute("fl_round"),
            total_num_clients,
            client_loss,
            total_num_examples,
            aggregated_metrics["sparse_categorical_accuracy"],
            aggregated_metrics["loss"],
            aggregated_metrics["val_sparse_categorical_accuracy"],
            aggregated_metrics["val_loss"],
            mongo_id,
            consistent,
            loaded_weights,
            insertion_time,
            checkpoint_time,
            aggregated_metrics["fit_time"],
            starting_time,
            time.ctime(),
        ]

        t10_output = DataSet("oServerTrainingAggregation", [Element(to_dfanalyzer)])
        t10.add_dataset(t10_output)
        t10.end()

        # Return the Aggregated Training Metrics.
        return aggregated_metrics

    def evaluate_metrics_aggregation_fn(
        self, testing_metrics: List[Tuple[int, Metrics]]
    ) -> Optional[Metrics]:
        """Metrics aggregation function called by Flower after every testing round."""
        # Get the Total Number of Participating Clients.
        t13 = Task(
            13 + 6 * (self.get_attribute("fl_round") - 1),
            dataflow_tag,
            "ServerEvaluationAggregation",
            dependency=Task(
                12 + 6 * (self.get_attribute("fl_round") - 1),
                dataflow_tag,
                "clientevaluation",
            ),
        )

        starting_time = time.ctime()
        t13.begin()

        total_num_clients = len(testing_metrics)
        # Get the Testing Metrics Names.
        metrics_names_list = list(testing_metrics[0][1].keys())
        # Multiply Each Testing Metrics Value of Each Participating Client
        # By His Number of Testing Examples (Client's Contribution).
        metrics_products_list = []
        for metric_name in metrics_names_list:
            metric_product = [
                num_examples * metric[metric_name]
                for num_examples, metric in testing_metrics
            ]
            metrics_products_list.append(metric_product)
        # Get the Total Number of Testing Examples (of All Participating Clients).
        total_num_examples = sum([num_examples for num_examples, _ in testing_metrics])
        # Aggregate the Testing Metrics (Weighted Average).
        aggregated_metrics = {}
        for metric_index in range(0, len(metrics_names_list)):
            metric_name = metrics_names_list[metric_index]
            weighted_average_metric = (
                sum(metrics_products_list[metric_index]) / total_num_examples
            )
            aggregated_metrics[metric_name] = weighted_average_metric

        # Log the Aggregated Testing Metrics (If Logger is Enabled for "INFO" Level).
        message = "[Server {0} | FL Round {1} | {2}] Aggregated Testing Metrics (Weighted Average): {3}".format(
            self.get_attribute("server_id"),
            self.get_attribute("fl_round"),
            "".join(
                [
                    str(total_num_clients),
                    " Clients" if total_num_clients > 1 else " Client",
                ]
            ),
            aggregated_metrics,
        )
        self.log_message(message, "INFO")

        ### Check client loss
        fl_round = self.get_attribute("fl_round")
        experiment_id = self.get_attribute("experiment_id")
        server_id = self.get_attribute("server_id")
        client_loss = None
        # if (fl_round%int(self.checkpoints_settings["checkpoint_frequency"]) == 0):
        #     message = f"Checking client loss in round {fl_round}"
        #     self.log_message(message, "INFO")
        #     connection = self.get_connection_monetdb()
        #     cursor = connection.cursor()
        #     if fl_round > 1:
        #         result = None
        #         tries = 0
        #         while not result:
        #             query = f"""SELECT check_if_last_round_is_already_recorded_evaluation({experiment_id}, {server_id}, {fl_round})"""
        #             cursor.execute(operation=query)
        #             result = cursor.fetchone()

        #             if result:
        #                 result = result[-1]
        #             tries += 1
        #             time.sleep(0.05)

        #         if result:
        #             query = f"""SELECT check_client_loss_evaluation({experiment_id}, {server_id}, {fl_round}, { int(self.checkpoints_settings["min_clients_per_checkpoint"])})"""
        #             cursor.execute(operation=query)
        #             client_loss = bool(cursor.fetchone()[0])
        #         cursor.close()
        #         connection.close()

        to_dfanalyzer = [
            self.get_attribute("experiment_id"),
            self.get_attribute("server_id"),
            self.get_attribute("fl_round"),
            client_loss,
            total_num_clients,
            total_num_examples,
            aggregated_metrics["sparse_categorical_accuracy"],
            aggregated_metrics["loss"],
            aggregated_metrics["evaluate_time"],
            starting_time,
            time.ctime(),
        ]

        t13_output = DataSet("oServerEvaluationAggregation", [Element(to_dfanalyzer)])
        t13.add_dataset(t13_output)
        t13.end()

        # if fl_round > 2 and self.checkpoints_settings["action"]=="rollback" and (fl_round%int(self.checkpoints_settings["checkpoint_frequency"]) == 0):
        #     message = f"Checking for rollback in round {fl_round}"
        #     self.log_message(message, "INFO")
        #     connection = self.get_connection_monetdb()
        #     cursor = connection.cursor()

        #     query = f"""SELECT get_last_round_with_all_clients_fit({experiment_id}, {server_id})"""
        #     cursor.execute(operation=query)
        #     last_round = int(cursor.fetchone()[0])
        #     if fl_round != last_round:
        #         # query = f"""SELECT check_client_loss_fit({experiment_id}, {fl_round}, { int(self.checkpoints_settings["min_clients_per_checkpoint"])})"""
        #         # cursor.execute(operation=query)
        #         # client_loss = bool(cursor.fetchone()[0])

        #         if not client_loss:
        #             db = self.get_connection_mongodb()
        #             # pesos = db.checkpoints.find_one({"$and": [{"round": {"$eq": last_round}}, {"experiment_id": {"$eq": experiment_id}}, {"server_id": {"$eq": server_id}}]})
        #             pesos = db.checkpoints.find({"$and": [{"experiment_id": {"$eq": experiment_id}}, {"server_id": {"$eq": server_id}}]}).sort([("round", -1)]).limit(1)
        #             params = pickle.loads(pesos["global_weights"])
        #             if params:
        #                 message = f"ROLLBACK! Using weights from round {pesos['round']} in round {fl_round}"
        #                 self.log_message(message, "INFO")
        #                 self.set_attribute(
        #                     "global_model_parameters",
        #                     params,
        #                 )

        #             else:
        #                 message = f"Couldn't find valid checkpoint for round {fl_round}"
        #                 self.log_message(message, "INFO")
        #                 last_round = None
        #         else:
        #             message = f"Client missing in {fl_round}! Waiting return to execute rollback."
        #             self.log_message(message, "INFO")

        #     cursor.close()
        #     connection.close()

        # Return the Aggregated Testing Metrics.
        return aggregated_metrics

    def instantiate_server_aggregation_strategy(self) -> Strategy:
        # Get Server Config File.
        server_config_file = self.get_attribute("server_config_file")
        # Init ConfigParser Object.
        cp = ConfigParser()
        cp.optionxform = str
        cp.read(filenames=server_config_file, encoding="utf-8")
        fl_settings = self.get_attribute("fl_settings")
        server_aggregation_strategy = None
        t3 = Task(3, dataflow_tag, "Strategy")

        t3.begin()
        if fl_settings["server_aggregation_strategy"] == "FedAvg":
            # FedAvg - Federated Averaging Aggregation Strategy.
            server_aggregation_strategy = FedAvg(
                fraction_fit=fl_settings["fraction_fit"],
                fraction_evaluate=fl_settings["fraction_evaluate"],
                min_fit_clients=fl_settings["min_fit_clients"],
                min_evaluate_clients=fl_settings["min_evaluate_clients"],
                min_available_clients=fl_settings["min_available_clients"],
                evaluate_fn=self.evaluate_fn,
                on_fit_config_fn=self.on_fit_config_fn,
                on_evaluate_config_fn=self.on_evaluate_config_fn,
                accept_failures=fl_settings["accept_rounds_containing_failures"],
                initial_parameters=self.get_attribute(
                    "initial_global_model_parameters"
                ),
                fit_metrics_aggregation_fn=self.fit_metrics_aggregation_fn,
                evaluate_metrics_aggregation_fn=self.evaluate_metrics_aggregation_fn,
            )
            t3_input = DataSet("iStrategy", [Element([0, 0])])
            t3.add_dataset(t3_input)

        elif fl_settings["server_aggregation_strategy"] == "FedAvgM":
            # Parse 'FedAvgM Settings'.
            fed_avg_m_settings = self.parse_config_section(cp, "FedAvgM Settings")
            # FedAvgM - Federated Averaging with Server Momentum Aggregation Strategy.
            server_aggregation_strategy = FedAvgM(
                fraction_fit=fl_settings["fraction_fit"],
                fraction_evaluate=fl_settings["fraction_evaluate"],
                min_fit_clients=fl_settings["min_fit_clients"],
                min_evaluate_clients=fl_settings["min_evaluate_clients"],
                min_available_clients=fl_settings["min_available_clients"],
                evaluate_fn=self.evaluate_fn,
                on_fit_config_fn=self.on_fit_config_fn,
                on_evaluate_config_fn=self.on_evaluate_config_fn,
                accept_failures=fl_settings["accept_rounds_containing_failures"],
                initial_parameters=self.get_attribute(
                    "initial_global_model_parameters"
                ),
                fit_metrics_aggregation_fn=self.fit_metrics_aggregation_fn,
                evaluate_metrics_aggregation_fn=self.evaluate_metrics_aggregation_fn,
                server_learning_rate=fed_avg_m_settings["server_learning_rate"],
                server_momentum=fed_avg_m_settings["server_momentum"],
            )

            attributes = ["server_learning_rate", "server_momentum"]
            to_dfanalyzer = [fed_avg_m_settings.get(attr, None) for attr in attributes]
            t3_input = DataSet("iStrategy", [Element(to_dfanalyzer)])
            t3.add_dataset(t3_input)
        t3_output = DataSet("oStrategy", [Element([])])
        t3.add_dataset(t3_output)
        t3.end()
        # Unbind ConfigParser Object (Garbage Collector).
        del cp
        return server_aggregation_strategy

    def instantiate_flower_server(self) -> Server:
        # Instantiate Simple Client Manager.
        simple_client_manager = self.instantiate_simple_client_manager()
        # Instantiate Server's Aggregation Strategy.
        server_aggregation_strategy = self.instantiate_server_aggregation_strategy()
        # Instantiate Flower Server.
        flower_server = Server(
            client_manager=simple_client_manager, strategy=server_aggregation_strategy
        )
        return flower_server

    def instantiate_flower_server_config(self) -> ServerConfig:
        fl_settings = self.get_attribute("fl_settings")
        # Instantiate Flower Server's Config.
        flower_server_config = ServerConfig(
            num_rounds=fl_settings["num_rounds"],
            round_timeout=fl_settings["round_timeout_in_seconds"],
        )
        return flower_server_config

    def get_ssl_certificates(self) -> Optional[Tuple[bytes]]:
        fl_settings = self.get_attribute("fl_settings")
        ssl_certificates = None
        if fl_settings["enable_ssl"]:
            ssl_settings = self.get_attribute("ssl_settings")
            prefix_path = Path(
                "./FlowerServer_" + str(self.get_attribute("experiment_id"))
            )
            ca_certificate_bytes = prefix_path.joinpath(
                ssl_settings["ca_certificate_file"]
            ).read_bytes()
            server_certificate_bytes = prefix_path.joinpath(
                ssl_settings["server_certificate_file"]
            ).read_bytes()
            server_rsa_private_key_bytes = prefix_path.joinpath(
                ssl_settings["server_rsa_private_key_file"]
            ).read_bytes()
            ssl_certificates = (
                ca_certificate_bytes,
                server_certificate_bytes,
                server_rsa_private_key_bytes,
            )
        return ssl_certificates

    def start_flower_server(self) -> None:
        # Get gRPC Server's Listen IP Address and Port.
        grpc_listen_ip_address_and_port = self.get_grpc_listen_ip_address_and_port()
        # Get Flower Server.
        flower_server = self.get_attribute("flower_server")
        # Get Flower Server's Config.
        flower_server_config = self.get_attribute("flower_server_config")
        # Get gRPC Max Message Length (in Bytes).
        grpc_max_message_length_in_bytes = self.get_grpc_max_message_length_in_bytes()
        # Get Secure Socket Layer (SSL) Certificates (SSL-Enabled Secure Connection).
        ssl_certificates = self.get_ssl_certificates()
        # Start Flower Server.
        start_server(
            server_address=grpc_listen_ip_address_and_port,
            server=flower_server,
            config=flower_server_config,
            grpc_max_message_length=grpc_max_message_length_in_bytes,
            certificates=ssl_certificates,
        )


def main() -> None:
    # Begin.

    # Parse Flower Server Arguments.
    ag = ArgumentParser(description="Flower Server Arguments")
    ag.add_argument(
        "--server_id", type=int, required=True, help="Server ID (no default)"
    )
    ag.add_argument(
        "--server_config_file",
        type=Path,
        required=True,
        help="Server Config File (no default)",
    )
    ag.add_argument(
        "--id_weights_mongo",
        type=str,
        required=False,
        default=None,
    )
    parsed_args = ag.parse_args()
    # Get Flower Server Arguments.
    experiment_id = int(parsed_args.server_id)
    server_config_file = Path(parsed_args.server_config_file)
    # Init FlowerServer Object.
    fs = FlowerServer(experiment_id, server_config_file)
    # Parse Flower Server Config File.
    fs.parse_flower_server_config_file()
    # Instantiate and Set Logger.
    logger = fs.load_logger()
    fs.set_attribute("logger", logger)
    # Load and Set Initial Global Model Parameters.

    id_weights_mongo = parsed_args.id_weights_mongo

    initial_global_model_parameters = fs.load_initial_global_model_parameters(
        id_weights_mongo
    )

    fs.set_attribute("initial_global_model_parameters", initial_global_model_parameters)
    # Load and Set Initial Fit Config.
    fit_config = fs.load_initial_fit_config()
    fs.set_attribute("fit_config", fit_config)
    # Load and Set Initial Evaluate Config.
    evaluate_config = fs.load_initial_evaluate_config()
    fs.set_attribute("evaluate_config", evaluate_config)
    # Instantiate and Set Flower Server.
    flower_server = fs.instantiate_flower_server()
    fs.set_attribute("flower_server", flower_server)
    # Instantiate and Set Flower Server's Config.
    flower_server_config = fs.instantiate_flower_server_config()
    fs.set_attribute("flower_server_config", flower_server_config)
    # Start Flower Server.
    fs.start_flower_server()
    # Unbind Objects (Garbage Collector).
    del ag
    del fs
    # End.
    exit(0)


if __name__ == "__main__":
    main()
