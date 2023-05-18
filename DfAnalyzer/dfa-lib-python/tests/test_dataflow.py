from dfa_lib_python.dataflow import Dataflow
from dfa_lib_python.transformation import Transformation
from dfa_lib_python.set import Set
from dfa_lib_python.set_type import SetType
from dfa_lib_python.attribute import Attribute
from dfa_lib_python.attribute_type import AttributeType


def test_get_transformations_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.INPUT, attributes)]
    transformations = [Transformation("tf1", sets=sets)]
    expected_result = [x.get_specification() for x in transformations]
    dataflow = Dataflow("df", transformations=transformations)
    assert dataflow.transformations == expected_result


def test_set_transformations_pass():
    attributes = [Attribute("att1", AttributeType.TEXT)]
    sets = [Set("set1", SetType.INPUT, attributes)]
    transformations = [Transformation("tf1", sets=sets)]
    new_transformations = [Transformation("tf2", sets=sets)]
    expected_result = [x.get_specification() for x in new_transformations]
    dataflow = Dataflow("df", transformations=transformations)
    dataflow.transformations = new_transformations
    assert dataflow.transformations == expected_result


def test_get_specification_pass():
    tag = "df"
    sets = [Set("set1", SetType.INPUT,
            [Attribute("att1", AttributeType.TEXT)])]
    transformations = [Transformation(tag, sets)]
    expected_result = {"transformations": [x.get_specification()
                       for x in transformations], "tag": tag}
    dataflow = Dataflow("df", transformations=transformations)
    assert dataflow.get_specification() == expected_result
