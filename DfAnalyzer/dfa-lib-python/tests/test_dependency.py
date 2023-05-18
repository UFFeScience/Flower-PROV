from dfa_lib_python.dependency import Dependency


def test_get_tags_pass():
    tags = ["task1", "task2"]
    ids = [1, 2]
    expected_result = [{"tag": x} for x in tags]
    dependency = Dependency(tags, ids)
    assert dependency.tags == expected_result


def test_set_tags_pass():
    tags = ["task1", "task2"]
    new_tags = ["task1", "task3"]
    ids = [1, 2]
    expected_result = [{"tag": x} for x in new_tags]
    dependency = Dependency(tags, ids)
    dependency.tags = new_tags
    assert dependency.tags == expected_result


def test_get_ids_pass():
    tags = ["task1", "task2"]
    ids = [1, 2]
    expected_result = [{"id": x} for x in ids]
    dependency = Dependency(tags, ids)
    assert dependency.ids == expected_result


def test_set_ids_pass():
    tags = ["task1", "task2"]
    new_ids = [1, 3]
    ids = [1, 2]
    expected_result = [{"id": x} for x in new_ids]
    dependency = Dependency(tags, ids)
    dependency.ids = new_ids
    assert dependency.ids == expected_result


def test_get_specification_pass():
    tags = ["task1", "task2"]
    ids = [1, 2]
    expected_result = {"tags": [{"tag": "task1"}, {"tag": "task2"}],
                       "ids": [{"id": 1}, {"id": 2}]}
    dependency = Dependency(tags, ids)
    assert dependency.get_specification() == expected_result
