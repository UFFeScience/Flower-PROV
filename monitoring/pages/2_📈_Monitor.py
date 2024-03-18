import streamlit as st

import pandas as pd
import pymonetdb
import matplotlib.pyplot as plt
import seaborn as sns
import time

st.set_page_config(
    page_title="Flower-PROV Monitor",
    page_icon=None,
    layout="centered",
    initial_sidebar_state="auto",
    menu_items=None,
)
st.sidebar.title("Options")


def get_cursor_db():
    conn = pymonetdb.connect(
        database="dataflow_analyzer", port=50000, username="monetdb", password="monetdb"
    )
    cursor = conn.cursor()
    return conn, cursor


# Create a function to retrieve data from the database
def fetch_data(table="oservertrainingaggregation", experiment_id=0, server_id=0):
    conn, cursor = get_cursor_db()
    cursor.execute(
        f"SELECT * FROM {table} WHERE experiment_id={experiment_id} AND server_id={server_id}"
    )  # Modify to your table name and SQL query
    data = cursor.fetchall()
    columns = [desc[0] for desc in cursor.description]
    df = pd.DataFrame(data, columns=columns)
    conn.close()
    return df


def get_experiment_ids():
    conn, cursor = get_cursor_db()
    cursor.execute(f"SELECT DISTINCT experiment_id FROM iserverconfig")
    data = cursor.fetchall()
    experiment_ids = [int(row[0]) for row in data]
    conn.close()
    return experiment_ids


def get_server_ids(experiment_id):
    conn, cursor = get_cursor_db()
    cursor.execute(
        f"SELECT DISTINCT server_id FROM iserverconfig WHERE experiment_id={experiment_id}"
    )
    data = cursor.fetchall()
    experiment_ids = [int(row[0]) for row in data]
    conn.close()
    return experiment_ids


tables = {
    "Aggregated by server (training)": "oServerTrainingAggregation",
    "For each client (training)": "oClientTraining",
    "For each client (evaluation)": "oClientEvaluation",
    "Aggregated by server (evaluation)": "oServerEvaluationAggregation",
}

st.sidebar.subheader("Select Metric to Monitor")

selected_experiment_id = st.sidebar.selectbox(
    "Select experiment ID:", get_experiment_ids()
)
selected_table = st.sidebar.selectbox("Select type:", sorted(tables.keys()), index=1)
server_ids = get_server_ids(selected_experiment_id)
selected_server_id = 0
if len(server_ids) > 1:
    selected_server_id = st.sidebar.selectbox("Select server ID:", server_ids)


table = tables[selected_table]
data_df = fetch_data(table, selected_experiment_id)
data_df = data_df[data_df["server_id"] == selected_server_id]
columns = ["accuracy", "loss"]
if table == "oServerTrainingAggregation":
    columns += ["val_accuracy", "val_loss"]

selected_column = st.sidebar.selectbox("Select metric:", columns, index=0)


def plot_graph():
    empty = st.empty()
    sns.set_style("darkgrid")
    while True:
        if selected_column:
            fig, ax = plt.subplots(figsize=(10, 6))
            plt.title(
                f"{selected_column.capitalize()} {selected_table.lower()}", fontsize=25
            )
            if "Client" in table:
                sns.lineplot(
                    x=data_df["server_round"],
                    y=data_df[selected_column],
                    hue=data_df["client_id"].astype(int),
                    linestyle=":",
                    marker="o",
                )
            else:
                sns.lineplot(
                    x=data_df["server_round"],
                    y=data_df[selected_column],
                    linestyle=":",
                    marker="o",
                )
            plt.xlabel("Round", fontsize=15)
            ax.xaxis.get_major_locator().set_params(integer=True)
            plt.ylabel(f"{selected_column.capitalize()}", fontsize=15)
            with empty.container():
                plt.close()
                st.pyplot(fig)
        time.sleep(1)


plot_graph()
