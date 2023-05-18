from .ProvenanceObject import ProvenanceObject


class Dependency(ProvenanceObject):
    """
    This class represents a dependency of a task.
    
    Attributes:
        - tags (list): Tags of the dependent tasks.
        - ids (list): Ids of the dependent tasks.
    """

    def __init__(self, tags, ids):
        ProvenanceObject.__init__(self, "")
        self.tags = tags
        self.ids = ids

    @property
    def tags(self):
        """Get or set the dependency tags."""
        return self._tags

    @tags.setter
    def tags(self, tags):
        assert isinstance(tags, list), \
            "The tags must be in a list."
        result = []
        for tag in tags:
            result.append({"tag": tag})
        self._tags = result

    @property
    def ids(self):
        """Get or set the dependency ids."""
        return self._ids

    @ids.setter
    def ids(self, ids):
        assert isinstance(ids, list), \
            "The ids must be in a list."
        result = []
        for id in ids:
            result.append({"id": id})
        self._ids = result
