from dfa_lib_python.transformation import Transformation
from dfa_lib_python.set import Set
from dfa_lib_python.set_type import SetType
from dfa_lib_python.attribute import Attribute
from dfa_lib_python.attribute_type import AttributeType


def test_get_sets_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.INPUT, attributes)]
    expected_result = [x.get_specification() for x in sets]
    transformation = Transformation("tf1", sets=sets)
    assert transformation.sets == expected_result


def test_set_sets_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.INPUT, attributes)]
    new_sets = [Set("set1", SetType.INPUT, attributes),
                Set("set2", SetType.OUTPUT, attributes)]
    expected_result = [x.get_specification() for x in new_sets]
    transformation = Transformation("tf1", sets=sets)
    transformation.sets = new_sets
    assert transformation.sets == expected_result


def test_get_input_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.INPUT, attributes)]
    expected_result = [sets[0].get_specification()]
    transformation = Transformation("tf1", sets=sets)
    assert transformation.input == expected_result


def test_set_input_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.INPUT, attributes)]
    input = [Set("set4", SetType.INPUT, attributes)]
    expected_result = [x.get_specification() for x in input]
    transformation = Transformation("tf1", sets=sets)
    transformation.input = input
    assert transformation.input == expected_result


def test_get_output_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.OUTPUT, attributes)]
    expected_result = [sets[0].get_specification()]
    transformation = Transformation("tf1", sets=sets)
    assert transformation.output == expected_result


def test_set_output_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.OUTPUT, attributes)]
    output = [Set("set4", SetType.OUTPUT, attributes)]
    expected_result = [x.get_specification() for x in output]
    transformation = Transformation("tf1", sets=sets)
    transformation.output = output
    assert transformation.output == expected_result


def test_get_specification_pass():
    tag = "tf1"
    sets = [Set("set1", SetType.INPUT,
            [Attribute("att1", AttributeType.TEXT)])]
    expected_result = {"sets": [x.get_specification() for x in sets],
                       "tag": tag}
    transformation = Transformation(tag, sets)
    assert transformation.get_specification() == expected_result
