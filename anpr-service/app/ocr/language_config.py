from dataclasses import dataclass
@dataclass(frozen=True)
class OCRLanguage:
    name: str
    code: str
    script: str

INDIC_OCR_LANGUAGES = (
    OCRLanguage("English", "eng", "Latin"),
    OCRLanguage("Hindi", "hin", "Devanagari"),
    OCRLanguage("Marathi", "mar", "Devanagari"),
    OCRLanguage("Bengali", "ben", "Bengali"),
    OCRLanguage("Gujarati", "guj", "Gujarati"),
    OCRLanguage("Punjabi", "pan", "Gurmukhi"),
    OCRLanguage("Kannada", "kan", "Kannada"),
    OCRLanguage("Malayalam", "mal", "Malayalam"),
    OCRLanguage("Odia", "ori", "Odia"),
    OCRLanguage("Tamil", "tam", "Tamil"),
    OCRLanguage("Telugu", "tel", "Telugu"),
)

def get_language_codes():
    return [language.code for language in INDIC_OCR_LANGUAGES]
def get_language_names():
    return [language.name for language in INDIC_OCR_LANGUAGES]
def get_languages_by_script(script: str):
    return [
        language
        for language in INDIC_OCR_LANGUAGES
        if language.script.lower() == script.lower()
    ]