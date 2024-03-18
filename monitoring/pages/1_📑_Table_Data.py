import streamlit as st
import pymonetdb
import pandas as pd

# Connect to the MonetDB database
conn = pymonetdb.connect(
    database="dataflow_analyzer", port=50000, username="monetdb", password="monetdb"
)
cursor = conn.cursor()
# Create a sidebar for user inputs
st.sidebar.title("Options")


# Create a function to retrieve data from the database
def fetch_data(table="oservertrainingaggregation"):
    cursor.execute(f"SELECT * FROM {table}")

    data = cursor.fetchall()
    columns = [desc[0] for desc in cursor.description]
    df = pd.DataFrame(data, columns=columns)
    return df


tables = [
    "iServerConfig",
    "iLoadGlobalWeights",
    "oLoadGlobalWeights",
    "iDatasetLoad",
    "iStrategy",
    "iModelConfig",
    "iOptimizerConfig",
    "iLossConfig",
    "oServerTrainingAggregation",
    "oClientTraining",
    "iTrainingConfig",
    "oTrainingConfig",
    "iEvaluationConfig",
    "oClientEvaluation",
    "oServerEvaluationAggregation",
]

# Load data from the database

# Allow the user to select which attributes to plot
st.sidebar.subheader("Select Attributes to Plot")

selected_table = st.sidebar.selectbox("Select table:", tables, index=0)


data_df = fetch_data(selected_table)


selected_columns = st.sidebar.multiselect(
    "Select column:", data_df.columns, default=data_df.columns.tolist()
)
filtered_df = data_df[selected_columns]

# Visualize table
st.subheader(f"Table {selected_table}:")
st.write(filtered_df)
