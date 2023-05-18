from datetime import datetime
from dfa_lib_python.performance import Performance
from dfa_lib_python.method_type import MethodType


def test_get_start_time_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    performance = Performance(start_time, end_time)
    assert performance.startTime == start_time


def test_set_start_time_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')    
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    performance = Performance(start_time, end_time)
    new_start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    performance.startTime = new_start_time
    assert performance.startTime == new_start_time


def test_get_end_time_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    performance = Performance(start_time, end_time)
    assert performance.endTime == end_time


def test_set_end_time_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')    
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    performance = Performance(start_time, end_time)
    new_end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    performance.endTime = new_end_time
    assert performance.endTime == new_end_time


def test_get_method_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    method = MethodType.COMPUTATION
    performance = Performance(start_time, end_time, method=method)
    assert performance.method == method


def test_set_method_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    method = MethodType.COMPUTATION
    new_method = MethodType.EXTRACTION
    performance = Performance(start_time, end_time, method=method)
    performance.method = new_method
    assert performance.method == new_method.value


def test_get_description_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    description = "A normal task performance"
    performance = Performance(start_time, end_time, description=description)
    assert performance.description == description


def test_set_description_pass():
    start_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    end_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    description = "A simple description"
    new_description = "Another simple description"
    performance = Performance(start_time, end_time, description=description)
    performance.description = new_description
    assert performance.description == new_description