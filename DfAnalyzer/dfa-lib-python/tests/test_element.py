from dfa_lib_python.element import Element


def test_get_values_pass():
    values = [1, 2, 3, 4]
    expected_values = ["1", "2", "3", "4"]
    element = Element(values)
    assert element.values == expected_values


def test_set_values_pass():
    values = [1, 2, 3, 4]
    new_values = [5, 6, 7, 8]
    expected_values =  ["5", "6", "7", "8"]
    element = Element(values)
    element.values = new_values
    assert element.values == expected_values
