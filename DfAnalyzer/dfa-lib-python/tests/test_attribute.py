from dfa_lib_python.attribute import Attribute
from dfa_lib_python.attribute_type import AttributeType


def test_get_name_pass():
    name = "att1"
    type = AttributeType.TEXT
    attribute = Attribute(name, type)
    assert attribute.name == name


def test_set_name_pass():
    name = "att1"
    new_name = "att-modified"
    type = AttributeType.TEXT
    attribute = Attribute(name, type)
    attribute.name = new_name
    assert attribute.name == new_name


def test_get_attribute_type_pass():
    name = "att1"
    type = AttributeType.TEXT
    attribute = Attribute(name, type)
    assert attribute.type == type.value


def test_set_attribute_type_pass():
    name = "att1"
    type = AttributeType.TEXT
    new_type = AttributeType.NUMERIC
    attribute = Attribute(name, type)
    attribute.type = new_type
    assert attribute.type == new_type.value


def test_get_specification_pass():
    name = "att1"
    type = AttributeType.TEXT
    expected_specification = {"name": name, "type": type.value}
    attribute = Attribute(name, type)
    assert attribute.get_specification() == expected_specification
