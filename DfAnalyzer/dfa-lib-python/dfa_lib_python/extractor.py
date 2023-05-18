from .ProvenanceObject import ProvenanceObject
from .extractor_cartridge import ExtractorCartridge
from .extractor_extension import ExtractorExtension


class Extractor(ProvenanceObject):
    """
    This class represents and conceptual extractor.

    Attributes:
        - tag (str): Extractor tag.
        - cartridge (:obj:`ExtractorCartridge`): Extractor Cartridge.
        - extension (:obj:`ExtractorExtension`): Extractor Extension.
    """

    def __init__(self, tag, cartridge, extension):
        ProvenanceObject.__init__(self, tag)
        self.cartridge = cartridge
        self.extension = extension
        self.setTag = ""
        self.transformationTag = ""
        self.dataflowTag = ""

    @property
    def cartridge(self):
        """Get or set the extractor cartridge."""
        return self._cartridge

    @cartridge.setter
    def cartridge(self, cartridge):
        assert isinstance(cartridge, ExtractorCartridge), \
            "The parameter must be a extractor cartridge."
        self._cartridge = cartridge.value

    @property
    def extension(self):
        """Get or set the extractor extension."""
        return self._extension

    @extension.setter
    def extension(self, extension):
        assert isinstance(extension, ExtractorExtension), \
            "The parameter must be a extractor extension."
        self._extension = extension.value

    @property
    def setTag(self):
        """Get or set the dataset tag."""
        return self._setTag

    @setTag.setter
    def setTag(self, set_tag):
        assert isinstance(set_tag, str), \
            "The setTag must be a string."
        self._setTag = set_tag

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

    def add_cartridge(self, cartridge):
        self.cartridge = cartridge

    def add_extension(self, extension):
        self.extension = extension
