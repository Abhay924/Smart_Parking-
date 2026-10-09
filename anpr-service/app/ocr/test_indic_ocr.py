from pathlib import Path

import cv2

from app.ocr.indic_fallback import IndicFallbackOCR


CROP_DIR = Path("datasets/debug_crops")

TARGET_IMAGES = [
    "car4_plate_crop.jpg",
    "car5_plate_crop.jpg",
]


def main():
    print("=" * 100)
    print("SMART PARKING - GENERIC INDIC MULTILINGUAL OCR TEST")
    print("=" * 100)

    ocr = IndicFallbackOCR()

    for image_name in TARGET_IMAGES:
        image_path = CROP_DIR / image_name

        print(f"\nIMAGE: {image_name}")
        print("-" * 100)

        if not image_path.exists():
            print(f"ERROR: File not found: {image_path}")
            continue

        image = cv2.imread(str(image_path))

        if image is None:
            print("ERROR: Unable to read image")
            continue

        print(
            f"Size: "
            f"{image.shape[1]} x {image.shape[0]}"
        )

        candidates = ocr.recognize(image)

        if not candidates:
            print("NO OCR CANDIDATES")
            continue

        print("\nTOP RESULTS")
        print("-" * 100)

        for candidate in ocr.top(candidates, 30):
            print(
                f"{candidate.confidence:6.2f} | "
                f"{candidate.language:10} | "
                f"{candidate.script:12} | "
                f"PSM {candidate.psm:2} | "
                f"{candidate.preprocessing:12} | "
                f"{candidate.text}"
            )

        best = ocr.best(candidates)

        if best is None:
            print("\nBEST CANDIDATE")
            print("-" * 100)
            print("No valid OCR candidate found")
            continue

        print("\nBEST CANDIDATE")
        print("-" * 100)

        print(f"Text           : {best.text}")
        print(f"Confidence     : {best.confidence:.2f}")
        print(f"Language       : {best.language}")
        print(f"Script         : {best.script}")
        print(f"Preprocessing  : {best.preprocessing}")
        print(f"PSM            : {best.psm}")

    print("\n")
    print("=" * 100)
    print("TEST COMPLETE")
    print("=" * 100)


if __name__ == "__main__":
    main()