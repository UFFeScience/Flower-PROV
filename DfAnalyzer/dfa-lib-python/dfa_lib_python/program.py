from .ProvenanceObject import ProvenanceObject


class Program(ProvenanceObject):
    """
    This class defines a program.

    Attributes:
        - name (str): Program name.
        - path (str): Program path.
    """

    def __init__(self, name, path):
        ProvenanceObject.__init__(self, "")
        self.path = path
        self.name = name
        self.transformationTag = ""
        self.dataflowTag = ""

    @property
    def name(self):
        """Get or set the program name."""
        return self._name

    @name.setter
    def name(self, name):
        assert isinstance(name, str), \
            "The name must be a string instance."
        self._name = name

    @property
    def path(self):
        """Get or set the program path."""
        return self._path

    @path.setter
    def path(self, path):
        assert isinstance(path, str), \
            "The path must be a string instance."
        self._path = path

    @property
    def transformationTag(self):
        """Get or set the transformation tag."""
        return self._transformationTag

    @transformationTag.setter
    def transformationTag(self, transformation_tag):
        assert isinstance(transformation_tag, str), \
            "The transformationTag must be a string."
        self._transformationTag = transformation_tag

    @property
    def dataflowTag(self):
        """Get or set the dataflow tag."""
        return self._dataflowTag

    @dataflowTag.setter
    def dataflowTag(self, dataflow_tag):
        assert isinstance(dataflow_tag, str), \
            "The dataflowTag must be a string."
        self._dataflowTag = dataflow_tag
