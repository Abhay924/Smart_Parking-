import re
import unicodedata
from dataclasses import dataclass


@dataclass(frozen=True)
class PlateValidation:
    normalized: str
    format_score: float
    digit_score: float
    length_score: float
    pattern_score: float
    valid: bool


# Common Indian registration patterns.
#
# Examples:
# UP32AB1234
# MH20DV2366
# RJ14CV0002
# HR26DK8337
#
# Also supports BH-series:
# 24BH1234AB
# 25BH1234C
#
# This is intentionally conservative.
PATTERNS = [
    re.compile(r"^[A-Z]{2}\d{1,2}[A-Z]{1,3}\d{1,4}$"),
    re.compile(r"^\d{2}BH\d{4}[A-Z]{1,2}$"),
]


# OCR can sometimes return Unicode digits.
# Convert common Unicode decimal digits to ASCII.
def normalize_digits(text: str) -> str:
    result = []

    for character in text:
        try:
            if character.isdigit():
                result.append(str(unicodedata.digit(character)))
            else:
                result.append(character)
        except (TypeError, ValueError):
            result.append(character)

    return "".join(result)


def normalize_latin(text: str) -> str:
    text = normalize_digits(text.upper())

    # Keep only ASCII letters and digits.
    return "".join(
        character
        for character in text
        if ("A" <= character <= "Z")
        or ("0" <= character <= "9")
    )


def extract_alphanumeric_tokens(text: str):
    text = normalize_digits(text.upper())

    tokens = re.findall(
        r"[A-Z0-9]{4,12}",
        text,
    )

    return tokens


def calculate_length_score(value: str) -> float:
    length = len(value)

    if 8 <= length <= 10:
        return 1.0

    if length in (7, 11):
        return 0.6

    if length in (6, 12):
        return 0.3

    return 0.0


def calculate_digit_score(value: str) -> float:
    digits = sum(
        character.isdigit()
        for character in value
    )

    if 3 <= digits <= 6:
        return 1.0

    if digits in (2, 7):
        return 0.5

    return 0.0


def calculate_pattern_score(value: str) -> float:
    for pattern in PATTERNS:
        if pattern.fullmatch(value):
            return 1.0

    # Partial structural match.
    if re.match(
        r"^[A-Z]{1,2}\d{1,2}[A-Z0-9]{2,8}$",
        value,
    ):
        return 0.5

    return 0.0


def validate_plate(text: str) -> PlateValidation:
    normalized = normalize_latin(text)

    if not normalized:
        return PlateValidation(
            normalized="",
            format_score=0.0,
            digit_score=0.0,
            length_score=0.0,
            pattern_score=0.0,
            valid=False,
        )

    length_score = calculate_length_score(
        normalized
    )

    digit_score = calculate_digit_score(
        normalized
    )

    pattern_score = calculate_pattern_score(
        normalized
    )

    format_score = (
        length_score * 0.30
        + digit_score * 0.25
        + pattern_score * 0.45
    )

    valid = (
        pattern_score == 1.0
        and length_score >= 0.6
        and digit_score >= 0.5
    )

    return PlateValidation(
        normalized=normalized,
        format_score=format_score,
        digit_score=digit_score,
        length_score=length_score,
        pattern_score=pattern_score,
        valid=valid,
    )


def extract_best_plate_candidate(text: str):
    direct = validate_plate(text)

    candidates = []

    if direct.normalized:
        candidates.append(direct)

    for token in extract_alphanumeric_tokens(text):
        validation = validate_plate(token)

        if validation.normalized:
            candidates.append(validation)

    if not candidates:
        return None

    return max(
        candidates,
        key=lambda candidate: candidate.format_score,
    )