from dfa_lib_python.extractor import Extractor
from dfa_lib_python.extractor_cartridge import ExtractorCartridge
from dfa_lib_python.extractor_extension import ExtractorExtension


def test_get_cartridge_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    extractor = Extractor(tag, cartridge, extension)
    assert extractor.cartridge == cartridge.value


def test_set_cartridge_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    new_cartridge = ExtractorCartridge.INDEXING
    extension = ExtractorExtension.FITS
    extractor = Extractor(tag, cartridge, extension)
    extractor.cartridge = new_cartridge
    assert extractor.cartridge == new_cartridge.value


def test_get_extension_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    extractor = Extractor(tag, cartridge, extension)
    assert extractor.extension == extension.value


def test_set_extension_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    new_extension = ExtractorExtension.FASTBIT
    extractor = Extractor(tag, cartridge, extension)
    extractor.extension = new_extension
    assert extractor.extension == new_extension.value


def test_get_set_dataset_tag_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    extractor = Extractor(tag, cartridge, extension)
    assert len(extractor.setTag) == 0


def test_set_dataset_tag_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    set_tag = "ds1"
    extractor = Extractor(tag, cartridge, extension)
    extractor.setTag = set_tag
    assert extractor.setTag == set_tag


def test_get_transformation_tag_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    extractor = Extractor(tag, cartridge, extension)
    assert len(extractor.transformationTag) == 0


def test_set_transformation_tag_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    transformation_tag = "tf1"
    extractor = Extractor(tag, cartridge, extension)
    extractor.transformationTag = transformation_tag
    assert extractor.transformationTag == transformation_tag


def test_get_dataflow_tag_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    extractor = Extractor(tag, cartridge, extension)
    assert len(extractor.dataflowTag) == 0


def test_set_dataflow_tag_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    dataflow_tag = "df1"
    extractor = Extractor(tag, cartridge, extension)
    extractor.dataflowTag = dataflow_tag
    assert extractor.dataflowTag == dataflow_tag


def test_get_specification_pass():
    tag = "ext1"
    cartridge = ExtractorCartridge.EXTRACTION
    extension = ExtractorExtension.FITS
    set_tag = "ds1"
    transformation_tag = "tf1"
    dataflow_tag = "df1"
    expected_specification = {"tag": tag, 
                              "cartridge": cartridge.value,
                              "extension": extension.value,
                              "setTag": set_tag,
                              "transformationTag": transformation_tag,
                              "dataflowTag": dataflow_tag
                              }
    extractor = Extractor(tag, cartridge, extension)
    extractor.setTag = set_tag
    extractor.transformationTag = transformation_tag
    extractor.dataflowTag = dataflow_tag
    assert extractor.get_specification() == expected_specification
