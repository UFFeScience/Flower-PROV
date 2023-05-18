from enum import Enum


class TaskStatus(Enum):
    """ This class is a enum with all the possibles task status.
    """
    READY = 'READY'
    RUNNING = 'RUNNING'
    FINISHED = 'FINISHED'
