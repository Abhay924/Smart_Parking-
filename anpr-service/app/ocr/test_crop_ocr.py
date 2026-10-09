from pathlib import Path

import cv2

from app.detection.detector import PlateDetector
from app.ocr.recognizer import PlateRecognizer


CROP_DIR = Path("datasets/debug_crops")


def make_variants(image):
    variants = {}

    variants["original"] = image

    variants["2x"] = cv2.resize(
        image,
        None,
        fx=2,
        fy=2,
        interpolation=cv2.INTER_CUBIC,
    )

    variants["4x"] = cv2.resize(
        image,
        None,
        fx=4,
        fy=4,
        interpolation=cv2.INTER_CUBIC,
    )

    upscaled = variants["4x"]

    gray = cv2.cvtColor(upscaled, cv2.COLOR_BGR2GRAY)

    variants["4x_gray"] = cv2.cvtColor(
        gray,
        cv2.COLOR_GRAY2BGR,
    )

    clahe = cv2.createCLAHE(
        clipLimit=2.0,
        tileGridSize=(8, 8),
    )

    clahe_image = clahe.apply(gray)

    variants["4x_clahe"] = cv2.cvtColor(
        clahe_image,
        cv2.COLOR_GRAY2BGR,
    )

    blurred = cv2.GaussianBlur(
        gray,
        (3, 3),
        0,
    )

    sharpened = cv2.addWeighted(
        gray,
        1.8,
        blurred,
        -0.8,
        0,
    )

    variants["4x_sharp"] = cv2.cvtColor(
        sharpened,
        cv2.COLOR_GRAY2BGR,
    )

    otsu = cv2.threshold(
        gray,
        0,
        255,
        cv2.THRESH_BINARY + cv2.THRESH_OTSU,
    )[1]

    variants["4x_otsu"] = cv2.cvtColor(
        otsu,
        cv2.COLOR_GRAY2BGR,
    )

    adaptive = cv2.adaptiveThreshold(
        gray,
        255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY,
        31,
        11,
    )

    variants["4x_adaptive"] = cv2.cvtColor(
        adaptive,
        cv2.COLOR_GRAY2BGR,
    )

    return variants


def run_ocr(detector, image):
    """
    Run CCT-S v2 OCR directly on an already-cropped plate.
    """
    return detector.alpr.ocr.predict(image)

    if result is None:
        return None

    return result
def extract_ocr_result(result):
    if result is None:
        return None

    text = getattr(result, "text", None)
    confidence = getattr(result, "confidence", None)

    if not text:
        return None

    if isinstance(confidence, (list, tuple)):
        values = [
            float(value)
            for value in confidence
            if value is not None
        ]

        confidence_value = (
            sum(values) / len(values)
            if values
            else 0.0
        )

    elif isinstance(confidence, (int, float)):
        confidence_value = float(confidence)

    else:
        confidence_value = 0.0

    plate = "".join(
        character
        for character in str(text).upper()
        if character.isalnum()
    )

    return {
        "plate": plate,
        "confidence": confidence_value,
    }


def main():
    detector = PlateDetector()

    crops = [
        CROP_DIR / "car4_plate_crop.jpg",
        CROP_DIR / "car5_plate_crop.jpg",
    ]

    print("=" * 90)
    print("SMART PARKING - DIRECT CCT-S CROP OCR TEST")
    print("=" * 90)

    results = []

    for crop_path in crops:
        print(f"\nIMAGE: {crop_path.name}")
        print("-" * 90)

        if not crop_path.exists():
            print(f"ERROR: Missing {crop_path}")
            continue

        image = cv2.imread(str(crop_path))

        if image is None:
            print("ERROR: Unable to read image")
            continue

        print(
            f"Original size: "
            f"{image.shape[1]} x {image.shape[0]}"
        )

        variants = make_variants(image)

        for variant_name, variant in variants.items():

            try:
                raw_result = run_ocr(
                    detector,
                    variant,
                )

                result = extract_ocr_result(
                    raw_result
                )

                if result is None:
                    print(
                        f"{variant_name:15} | "
                        f"NO OCR RESULT"
                    )
                    continue

                print(
                    f"{variant_name:15} | "
                    f"{result['plate']:15} | "
                    f"confidence="
                    f"{result['confidence']:.4f}"
                )

                results.append(
                    {
                        "image": crop_path.name,
                        "variant": variant_name,
                        "plate": result["plate"],
                        "confidence": result["confidence"],
                    }
                )

            except Exception as exc:
                print(
                    f"{variant_name:15} | "
                    f"ERROR: {exc}"
                )

    print("\n")
    print("=" * 90)
    print("BEST RESULTS")
    print("=" * 90)

    results.sort(
        key=lambda item: item["confidence"],
        reverse=True,
    )

    for result in results[:20]:
        print(
            f"{result['image']:25} | "
            f"{result['variant']:15} | "
            f"{result['plate']:15} | "
            f"{result['confidence']:.4f}"
        )

    print("=" * 90)


if __name__ == "__main__":
    main()