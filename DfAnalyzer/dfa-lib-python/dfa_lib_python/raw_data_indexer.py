import os
import subprocess
from .extractor_cartridge import ExtractorCartridge


class RawDataIndexer(object):
    """
    This class defines a Raw Data Indexer.

    Attributes:
        - cartridge (:obj:`ExtractorCartridge`): An :obj:`ExtractorCartridge`.
        - extractor_tag (:obj:`str`): The tag of an extractor.
        - path (:obj:`str`): Desired path.
        - file_name_with_extracted_data (:obj:`str`): The file that has the extracted data.
        - attributes (:obj:`list`): A :obj:`list` containing :obj:`Attribute`.
        - extra_arguments (:obj:`str`, optional): A set of extra arguments.
    """
    def __init__(self, cartridge, extractor_tag, path,
                 file_name_with_extracted_data,
                 attributes, extra_arguments=""):
        assert isinstance(cartridge, ExtractorCartridge), \
            "The extractor cartridge type must be a valid."
        assert isinstance(attributes, list), \
            "The attributes type must be a list."
        self._cartridge = cartridge.value
        self._method = "INDEX"
        self._attributes = attributes
        self._tag = extractor_tag
        self._path = path
        self._file_name_with_extracted_data = file_name_with_extracted_data
        self._extra_arguments = extra_arguments

    def get_attributes(self):
        s = ","
        r = [str(x) for x in self._attributes]
        result = s.join(r)
        return "[{0}]".format(result)

    def get_command_line(self):
        """Return the commandline to execute RDI."""
        dfanalyzer_dir = os.environ.get('DFANALYZER_DIR')
        cartridge = self._cartridge
        method = self._method
        tag = self._tag
        path = self._path
        file_name_with_extracted_data = self._file_name_with_extracted_data
        attributes = self.get_attributes()
        extra_arguments = self._extra_arguments
        print(extra_arguments)
        return "{0}/bin/RDE {1}:{2} {3} {4} \
        {5} {6} {7}".format(dfanalyzer_dir,
                            cartridge,
                            method,
                            tag,
                            path,
                            file_name_with_extracted_data,
                            attributes,
                            extra_arguments)

    def run(self):
        """Execute the RDI."""
        subprocess.call(self.get_command_line())