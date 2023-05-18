from .ProvenanceObject import ProvenanceObject
from .element import Element


class DataSet(ProvenanceObject):
    """
    This class defines a dataflow dataset.
    
    Attributes:
        - tag (str): Dataset tag.
        - elements (:obj:`Element`): Dataset Elements
    """

    def __init__(self, tag, elements):
        ProvenanceObject.__init__(self, tag)
        self.elements = elements

    @property
    def elements(self):
        """Get or set elements."""
        return self._elements

    @elements.setter
    def elements(self, elements):
        assert isinstance(elements, list), \
            "The elements must be in a list."
        result = []
        for element in elements:
            assert isinstance(element, Element), "The element must be valid."
            result.append(element.values)
        self._elements = result
