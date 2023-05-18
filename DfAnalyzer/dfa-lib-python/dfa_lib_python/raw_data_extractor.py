import os
import subprocess
from .extractor_cartridge import ExtractorCartridge


class RawDataExtractor(object):
    """
    This class defines a Raw Data Extractor.

    Attributes:
        - cartridge (:obj:`ExtractorCartridge`): An :obj:`ExtractorCartridge`.
        - command_line (:obj:`str`): A command line to be executed.
        - attributes (:obj:`list`): A :obj:`list` containing :obj:`Attribute`.
    """
    def __init__(self, cartridge, command_line, attributes):
        self._command_line = command_line
        assert isinstance(cartridge, ExtractorCartridge), \
            "The attributes type must be a list."
        self._cartridge = cartridge.value
        self._method = "EXTRACT"
        self._command = command_line
        self._tag = "extractor"
        self._path = "."
        assert isinstance(attributes, list), \
            "The attributes type must be a list."
        self._attributes = attributes

    def get_attributes(self):
        s = ","
        r = [str(x) for x in self._attributes]
        result = s.join(r)
        return "[{0}]".format(result)

    def get_command_line(self):
        """Return the commandline to execute RDE."""
        dfanalyzer_dir = os.environ.get('DFANALYZER_DIR')
        cartridge = self._cartridge
        method = self._method
        tag = self._tag
        path = self._path
        command = self._command
        attributes = self.get_attributes()
        return "{0}/bin/RDE {1}:{2} {3} {4} \{5}\ {6}".format(dfanalyzer_dir,
                                                              cartridge,
                                                              method,
                                                              tag,
                                                              path,
                                                              command,
                                                              attributes)

    def run(self):
        """Execute the RDE."""
        subprocess.call(self.get_command_line())