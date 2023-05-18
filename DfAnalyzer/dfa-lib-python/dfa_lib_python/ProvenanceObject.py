class ProvenanceObject(object):
    """
    This class defines a basic provenance object with
    a tag and a json representation.

    Attributes:
        - tag (str): ProvenanceObject tag.
    """

    def __init__(self, tag):
        self._tag = tag.lower()

    def get_specification(self, prefix="_"):
        """ Get the provenance json representation.

        Args:
            prefix (str): A prefix used to define which variables should be used.
        """
        json = {}
        for key in self.__dict__.keys():
            if key[0] != prefix:
                continue
            name = key.split(prefix)[1]
            value = self.__dict__[key]
            if(value is not None):
                if(len(value) > 0):
                    json[name] = value
                    if isinstance(value, str):
                        json[name] = value
        return json
