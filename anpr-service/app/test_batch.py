from pathlib import Path
from app.detection.detector import PlateDetector
from app.ocr.recognizer import PlateRecognizer

TEST_DIR = Path("datasets/test")
def main():
    detector = PlateDetector()
    images = sorted(TEST_DIR.glob("*.jpg"))

    if not images:
        print("No JPG images found in datasets/test")
        return

    print("=" * 70)
    print("SMART PARKING - ANPR BATCH TEST")
    print("=" * 70)

    results = []

    for image_path in images:
        print(f"\nTesting: {image_path.name}")
        print("-" * 70)

        try:
            detections = detector.detect(str(image_path))
            result = PlateRecognizer.extract(detections)

            if result is None:
                print("Plate       : NOT DETECTED")
                print("Status      : FAIL")

                results.append({
                    "image": image_path.name,
                    "plate": None,
                    "confidence": 0.0,
                    "status": "FAIL",
                })
                continue

            confidence = result["confidence"]
            if confidence >= 0.85:
                status = "PASS"
            elif confidence >= 0.70:
                status = "REVIEW"
            else:
                status = "FAIL"

            print(f"Plate       : {result['plate_number']}")
            print(f"Detection   : {result['detection_confidence']:.4f}")
            print(f"OCR         : {result['ocr_confidence']:.4f}")
            print(f"Overall     : {confidence:.4f}")
            print(f"Status      : {status}")
            results.append({
                "image": image_path.name,
                "plate": result["plate_number"],
                "confidence": confidence,
                "status": status,
            })

        except Exception as exc:
            print(f"ERROR       : {exc}")
            results.append({
                "image": image_path.name,
                "plate": None,
                "confidence": 0.0,
                "status": "ERROR",
            })

    print("\n")
    print("=" * 70)
    print("SUMMARY")
    print("=" * 70)

    for result in results:
        print(
            f"{result['image']:12} | "
            f"{str(result['plate']):15} | "
            f"{result['confidence']:.4f} | "
            f"{result['status']}"
        )

    total = len(results)
    passed = sum(r["status"] == "PASS" for r in results)
    review = sum(r["status"] == "REVIEW" for r in results)
    failed = sum(r["status"] == "FAIL" for r in results)

    print("-" * 70)
    print(f"TOTAL  : {total}")
    print(f"PASS   : {passed}")
    print(f"REVIEW : {review}")
    print(f"FAIL   : {failed}")

    if total:
        print(f"PASS % : {(passed / total) * 100:.2f}%")
    print("=" * 70)

if __name__ == "__main__":
    main()