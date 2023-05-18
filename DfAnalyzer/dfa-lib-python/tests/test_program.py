from dfa_lib_python.program import Program


def test_get_name_pass():
    path = "/home/vinicius/projects/tcc"
    name = "test_program.py"
    program = Program(name, path)
    assert program.name == name


def test_set_name_pass():
    path = "/home/vinicius/projects/tcc"
    name = "test_program.py"
    new_name = "new_program.py"
    program = Program(name, path)
    program.name = new_name
    assert program.name == new_name


def test_get_path_pass():
    path = "/home/vinicius/projects/tcc"
    tag = "test_program.py"
    program = Program(tag, path)
    assert program.path == path


def test_set_path_pass():
    path = "/home/vinicius/projects/tcc"
    new_path = "/home/vinicius/projects/nottcc"
    tag = "test_program.py"
    program = Program(tag, path)
    program.path = new_path
    assert program.path == new_path


def test_get_transformation_tag_pass():
    path = "/home/vinicius/projects/tcc"
    tag = "test_program.py"
    program = Program(tag, path)
    assert len(program.transformationTag) == 0


def test_set_transformation_tag_pass():
    path = "/home/vinicius/projects/tcc"
    tag = "test_program.py"
    transformation_tag = "tf1"
    program = Program(tag, path)
    program.transformationTag = transformation_tag
    assert program.transformationTag == transformation_tag


def test_get_dataflow_tag_pass():
    path = "/home/vinicius/projects/tcc"
    tag = "test_program.py"
    program = Program(tag, path)
    assert len(program.dataflowTag) == 0


def test_set_dataflow_tag_pass():
    path = "/home/vinicius/projects/tcc"
    tag = "test_program.py"
    dataflow_tag = "df1"
    program = Program(tag, path)
    program.dataflowTag = dataflow_tag
    assert program.dataflowTag == dataflow_tag


def test_get_specification_pass():
    name = "program.py"
    path = "/home/vinicius/projects/tcc"
    expected_specification = {"name": name, "path": path}
    program = Program(name, path)
    assert program.get_specification() == expected_specification
