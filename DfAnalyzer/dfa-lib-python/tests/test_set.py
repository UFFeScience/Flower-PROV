from dfa_lib_python.set import Set
from dfa_lib_python.set_type import SetType
from dfa_lib_python.attribute import Attribute
from dfa_lib_python.attribute_type import AttributeType


def test_get_type_pass():
    tag = "set1"
    type = SetType.INPUT
    attributes = [Attribute("att1", AttributeType.TEXT)]
    set = Set(tag, type, attributes)
    assert set.type == type.value


def test_set_type_pass():
    tag = "set1"
    type = SetType.INPUT
    new_type = SetType.OUTPUT
    attributes = [Attribute("att1", AttributeType.TEXT)]
    set = Set(tag, type, attributes)
    set.type = new_type
    assert set.type == new_type.value


def test_get_attributes_pass():
    tag = "set1"
    type = SetType.INPUT
    attributes = [Attribute("att1", AttributeType.TEXT)]
    expected_result = [x.get_specification() for x in attributes]
    set = Set(tag, type, attributes)
    assert set.attributes == expected_result


def test_set_attributes_pass():
    tag = "set1"
    type = SetType.INPUT
    attributes = [Attribute("att1", AttributeType.TEXT)]
    new_attributes = [Attribute("att1", AttributeType.NUMERIC)]
    expected_result = [x.get_specification() for x in new_attributes]
    set = Set(tag, type, attributes)
    set.attributes = new_attributes
    assert set.attributes == expected_result


def test_get_dependency_pass():
    tag = "set1"
    type = SetType.INPUT
    dependency = "dependency"
    attributes = [Attribute("att1", AttributeType.TEXT)]
    set = Set(tag, type, attributes, dependency=dependency)
    assert set.dependency == dependency


def test_set_dependency_pass():
    tag = "set1"
    type = SetType.INPUT
    dependency = "dependency"
    new_dependency = "another dependency"
    attributes = [Attribute("att1", AttributeType.TEXT)]
    set = Set(tag, type, attributes, dependency=dependency)
    set.dependency = new_dependency
    assert set.dependency == new_dependency


def test_get_specification_pass():
    tag = "set1"
    type = SetType.INPUT
    attributes = [Attribute("att1", AttributeType.TEXT)]
    expected_result = {"attributes": [attributes[0].get_specification()],
                       "tag": tag, "type": type.value}
    set = Set(tag, type, attributes)
    assert set.get_specification() == expected_result
