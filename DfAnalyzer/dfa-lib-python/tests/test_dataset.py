from dfa_lib_python.dataset import DataSet
from dfa_lib_python.element import Element


def test_get_elements_pass():
    tag = "dstag"
    elements = [Element([1, 2, 3, 4])]
    expected_result = [elements[0].values]
    dataset = DataSet(tag, elements)
    assert dataset.elements == expected_result


def test_set_elements_pass():
    tag = "dstag"
    elements = [Element([1, 2, 3, 4])]
    new_elements = [Element([5, 6, 7, 8])]
    expected_result = [new_elements[0].values]
    dataset = DataSet(tag, elements)
    dataset.elements = new_elements
    assert dataset.elements == expected_result
