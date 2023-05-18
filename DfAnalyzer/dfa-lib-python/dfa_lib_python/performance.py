from .ProvenanceObject import ProvenanceObject
from .method_type import MethodType


class Performance(ProvenanceObject):
    """
    This class defines a task performance.

    Attributes:
        - start_time (:obj:`str`): time when the task has started
        - end_time (:obj:`str`): time when the task has ended
        - method (:obj:`MethodType`, optional): method use to measure
        - description (:obj:`str`, optional): description of the performance measure
    """
    def __init__(self, start_time, end_time, method="", description=""):
        ProvenanceObject.__init__(self, "")
        self._startTime = start_time
        self._endTime = end_time
        self._method = method
        self._description = description

    @property
    def startTime(self):
        """Get or set the start time."""
        return self._startTime

    @startTime.setter
    def startTime(self, start_time):
        assert isinstance(start_time, str), \
            "The start time must be a string."
        self._startTime = start_time

    @property
    def endTime(self):
        """Get or set the end time."""
        return self._endTime

    @endTime.setter
    def endTime(self, end_time):
        assert isinstance(end_time, str), \
            "The end time must be a string."
        self._endTime = end_time

    @property
    def method(self):
        """Get or set the method type."""
        return self._method

    @method.setter
    def method(self, method):
        assert isinstance(method, MethodType), \
            "The performance method must be a MethodType object."
        self._method = method.value

    @property
    def description(self):
        """Get or set the performance description."""
        return self._description

    @description.setter
    def description(self, description):
        assert isinstance(description, str), \
            "The performance description must be a string."
        self._description = description
