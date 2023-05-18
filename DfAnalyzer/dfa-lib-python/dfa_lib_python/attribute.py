from .ProvenanceObject import ProvenanceObject
from .attribute_type import AttributeType


class Attribute(ProvenanceObject):
    """
    This class defines a dataflow attribute.
    
    Attributes:
        - name (str): Attribute name.
        - type (:obj:`AttributeType`): Attribute Type.
    """

    def __init__(self, tag, type):
        ProvenanceObject.__init__(self, "")
        self.name = tag
        self.type = type

    @property
    def name(self):
        """Get or set the attribute name."""
        return self._name

    @name.setter
    def name(self, name):
        assert isinstance(name, str), \
            "The name must be a string."
        self._name = name

    @property
    def type(self):
        """Get or set the attribute type."""
        return self._type

    @type.setter
    def type(self, type):
        assert isinstance(type, AttributeType), \
            "The type must be an instance of AttributeType."
        self._type = type.value

    def __repr__(self):
        """Return the string representation of an attribute."""
        return "{0}:{1}".format(self._name, self._type)
