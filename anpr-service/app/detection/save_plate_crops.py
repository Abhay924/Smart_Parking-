from pathlib import Path

import cv2

from app.detection.detector import PlateDetector


INPUT_DIR = Path("datasets/test")
OUTPUT_DIR = Path("datasets/debug_crops")

TARGET_IMAGES = [
    "car4.jpg",
    "car5.jpg",
]


def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    detector = PlateDetector()

    print("=" * 80)
    print("SMART PARKING - PLATE CROP DIAGNOSTIC")
    print("=" * 80)

    for image_name in TARGET_IMAGES:
        image_path = INPUT_DIR / image_name

        print(f"\nProcessing: {image_name}")
        print("-" * 80)

        if not image_path.exists():
            print(f"ERROR: Image not found: {image_path}")
            continue

        image = cv2.imread(str(image_path))

        if image is None:
            print("ERROR: Unable to read image")
            continue

        detections = detector.detect(str(image_path))

        if not detections:
            print("No plate detected")
            continue

        best_detection = None
        best_confidence = 0.0

        for detection in detections:
            detection_data = getattr(detection, "detection", None)

            if detection_data is None:
                continue

            confidence = float(
                getattr(detection_data, "confidence", 0.0)
            )

            if confidence > best_confidence:
                best_confidence = confidence
                best_detection = detection_data

        if best_detection is None:
            print("No valid detection found")
            continue

        bounding_box = getattr(best_detection, "bounding_box", None)

        if bounding_box is None:
            print("Bounding box not available")
            continue

        print(f"Detection confidence: {best_confidence:.4f}")
        print(f"Bounding box: {bounding_box}")

        x1 = int(getattr(bounding_box, "x1", 0))
        y1 = int(getattr(bounding_box, "y1", 0))
        x2 = int(getattr(bounding_box, "x2", 0))
        y2 = int(getattr(bounding_box, "y2", 0))

        height, width = image.shape[:2]

        x1 = max(0, min(x1, width - 1))
        y1 = max(0, min(y1, height - 1))
        x2 = max(0, min(x2, width))
        y2 = max(0, min(y2, height))

        if x2 <= x1 or y2 <= y1:
            print("Invalid bounding box")
            continue

        crop = image[y1:y2, x1:x2]

        if crop.size == 0:
            print("Empty crop")
            continue

        crop_name = image_path.stem + "_plate_crop.jpg"
        crop_path = OUTPUT_DIR / crop_name

        cv2.imwrite(str(crop_path), crop)

        print(f"Crop size: {crop.shape[1]} x {crop.shape[0]}")
        print(f"Saved: {crop_path}")

        # Upscaled diagnostic version
        upscale = cv2.resize(
            crop,
            None,
            fx=4,
            fy=4,
            interpolation=cv2.INTER_CUBIC,
        )

        upscale_name = image_path.stem + "_plate_crop_4x.jpg"
        upscale_path = OUTPUT_DIR / upscale_name

        cv2.imwrite(str(upscale_path), upscale)

        print(f"4x crop saved: {upscale_path}")

    print("\n")
    print("=" * 80)
    print("DONE")
    print("=" * 80)
    print(f"Crops directory: {OUTPUT_DIR}")


if __name__ == "__main__":
    main()