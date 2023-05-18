from enum import Enum


class MethodType(Enum):
    """ This class is a enum with all the possibles performance method types.
    """
    COMPUTATION = 'COMPUTATION'
    INSTRUMENTATION = 'INSTRUMENTATION'
    EXTRACTION = 'EXTRACTION'
    PROVENANCE = 'PROVENANCE'
    INDEXING = 'INDEXING'
