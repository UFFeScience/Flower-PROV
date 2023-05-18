from .ProvenanceObject import ProvenanceObject


class File(ProvenanceObject):
    """
    This class defines a file.

    Attributes:
        - path (str): File path.
        - name (str): File name.
    """

    def __init__(self, path, name):
        ProvenanceObject.__init__(self, name)
        self._path = path
        self._name = name
