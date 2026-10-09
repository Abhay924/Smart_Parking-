import re


PLATE_PATTERN = re.compile(
    r"^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$"
)


def normalize_plate(plate_number: str) -> str:

    if not plate_number:
        raise ValueError("Plate number is empty")

    plate = re.sub(
        r"[^A-Za-z0-9]",
        "",
        plate_number
    ).upper()

    return plate


def validate_plate(plate_number: str) -> bool:
    return bool(
        PLATE_PATTERN.fullmatch(plate_number)
    )