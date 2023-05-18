from .ProvenanceObject import ProvenanceObject
from .attribute import Attribute
from .extractor import Extractor
from .set_type import SetType


class Set(ProvenanceObject):
    """
    This class defines a dataflow set.

    Attributes:
        - tag (:obj:`str`): Set Tag.
        - type (:obj:`SetType`): Set Type.
        - attributes (:obj:`list`): Set attributes.
        - extractors (:obj:`list`): Set extractors.
        - dependency (:obj:`str`, optional): Set dependency.
    """
    def __init__(self, tag, type, attributes, extractors=[], dependency=""):
        ProvenanceObject.__init__(self, tag)
        self.attributes = attributes
        self.type = type
        self.extractors = extractors
        self.dependency = dependency

    @property
    def type(self):
        """Get or set the Set type"""
        return self._type

    @type.setter
    def type(self, value):
        assert isinstance(value, SetType), \
            "The type must be valid."
        self._type = value.value

    def set_type(self, type):
        """Set dataset type"""
        self.type = type

    @property
    def attributes(self):
        """Get or set the Set Attributes."""
        return self._attributes

    @attributes.setter
    def attributes(self, attributes):
        assert isinstance(attributes, list), \
            "The attributes must be in a list."
        result = []
        for attribute in attributes:
            assert isinstance(attribute, Attribute), \
                "The attribute must be valid."
            result.append(attribute.get_specification())
        self._attributes = result

    def add_attribute(self, attribute):
        """ Add an attribute to Set.

        Args:
            - attribute (:obj:`Attribute`): An object of the Attribute class.
        """
        assert isinstance(attribute, Attribute), \
            "The attribute must be valid."
        self.attributes.append(attribute.get_specification())

    @property
    def extractors(self):
        """Get or set the Set Extractors."""
        return self._extractors

    @extractors.setter
    def extractors(self, extractors):
        assert isinstance(extractors, list), \
            "The extractors must be in a list."
        result = []
        for extractor in extractors:
            assert isinstance(extractor, Extractor), \
                "The extractor must be valid."
            result.append(extractor.get_specification())
        self._extractors = result

    def add_extractor(self, extractor):
        """ Add an extractor to Set.

        Args:
            - extractor (:obj:`Extractor`): An object of the Extractor class.
        """
        assert isinstance(extractor, Extractor), \
            "The extractor must be valid."
        self.extractor.append(extractor.get_specification())

    @property
    def dependency(self):
        """Get or set the Set dependency."""
        return self._dependency

    @dependency.setter
    def dependency(self, dependency):
        assert isinstance(dependency, str), \
            "The dependency must be a string."
        self._dependency = dependency
