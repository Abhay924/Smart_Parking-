from pathlib import Path

import cv2

from app.ocr.indic_fallback import IndicFallbackOCR
from app.ocr.candidate_validator import extract_best_plate_candidate
from app.ocr.candidate_scorer import score_candidates, decide


CROP_DIR = Path("datasets/debug_crops")

TARGET_IMAGES = [
    "car4_plate_crop.jpg",
    "car5_plate_crop.jpg",
]

TOP_OCR_RESULTS = 20
TOP_SCORED_CANDIDATES = 15


def process_image(ocr, image_path):
    print("\n" + "=" * 100)
    print(f"IMAGE: {image_path.name}")
    print("=" * 100)

    if not image_path.exists():
        print(f"ERROR: Image not found: {image_path}")
        return

    image = cv2.imread(str(image_path))

    if image is None:
        print("ERROR: Unable to read image")
        return

    height, width = image.shape[:2]
    print(f"Image size: {width} x {height}")

    try:
        candidates = ocr.recognize(image)
    except Exception as exc:
        print(f"OCR ERROR: {exc}")
        print("Decision: MANUAL_REVIEW")
        return

    if not candidates:
        print("No OCR candidates found.")
        print("Decision: MANUAL_REVIEW")
        return

    print(f"\nTotal raw OCR candidates: {len(candidates)}")
    print("\nTOP RAW OCR RESULTS")
    print("-" * 100)

    for candidate in ocr.top(candidates, TOP_OCR_RESULTS):
        print(
            f"OCR={candidate.confidence:6.2f} | "
            f"{candidate.language:10} | "
            f"{candidate.preprocessing:12} | "
            f"PSM={candidate.psm:2} | "
            f"Text={candidate.text}"
        )

    print("\nPLATE FORMAT VALIDATION")
    print("-" * 100)

    validation_results = []

    for candidate in candidates:
        validation = extract_best_plate_candidate(candidate.text)

        if validation is None:
            continue

        validation_results.append(
            (
                candidate,
                validation,
            )
        )

    if not validation_results:
        print("No alphanumeric plate candidates extracted.")
        print("Decision: MANUAL_REVIEW")
        return

    seen = set()

    for candidate, validation in validation_results:
        key = (
            validation.normalized,
            candidate.language,
            candidate.preprocessing,
            candidate.psm,
        )

        if key in seen:
            continue

        seen.add(key)

        print(
            f"Plate={validation.normalized:15} | "
            f"Format={validation.format_score:.3f} | "
            f"Valid={validation.valid} | "
            f"OCR={candidate.confidence:.2f} | "
            f"Language={candidate.language}"
        )

    print("\nSCORED PLATE CANDIDATES")
    print("-" * 100)

    try:
        scored = score_candidates(candidates)
    except Exception as exc:
        print(f"SCORING ERROR: {exc}")
        print("Decision: MANUAL_REVIEW")
        return

    if not scored:
        print("No usable plate candidates found.")
        print("Decision: MANUAL_REVIEW")
        return

    for candidate in scored[:TOP_SCORED_CANDIDATES]:
        print(
            f"{candidate.final_score:.3f} | "
            f"Plate={candidate.plate:15} | "
            f"OCR={candidate.ocr_confidence:6.2f} | "
            f"Format={candidate.format_score:.3f} | "
            f"Agreement={candidate.agreement_score:.3f} | "
            f"Valid={candidate.valid_format} | "
            f"{candidate.language:10} | "
            f"{candidate.preprocessing:12}"
        )

    decision = decide(scored)

    print("\nFINAL EXPERIMENTAL DECISION")
    print("-" * 100)
    print(f"Status : {decision['status']}")
    print(f"Plate  : {decision['plate']}")
    print(f"Score  : {decision['score']:.3f}")
    print(f"Reason : {decision['reason']}")

    print(
        "\nNOTE: This is an experimental OCR decision. "
        "Do not use it alone to open a parking gate."
    )


def main():
    print("=" * 100)
    print("SMART PARKING - MULTILINGUAL OCR AND PLATE VALIDATION")
    print("=" * 100)

    ocr = IndicFallbackOCR()

    for image_name in TARGET_IMAGES:
        process_image(ocr, CROP_DIR / image_name)

    print("\n" + "=" * 100)
    print("TEST COMPLETE")
    print("=" * 100)


if __name__ == "__main__":
    main()