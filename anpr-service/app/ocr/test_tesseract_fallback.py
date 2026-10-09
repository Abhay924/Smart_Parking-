from pathlib import Path
import cv2
import pytesseract

TESSERACT_PATH = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
IMAGE_PATH = Path("datasets/test/car4.jpg")

pytesseract.pytesseract.tesseract_cmd = TESSERACT_PATH

LANGUAGES = [
    "eng",
    "hin",
    "ben",
    "guj",
    "pan",
    "kan",
    "mal",
    "mar",
    "ori",
    "tam",
    "tel",
]

def preprocess_variants(image):
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    variants = {}

    variants["gray"] = gray

    variants["clahe"] = cv2.createCLAHE(
        clipLimit=2.0,
        tileGridSize=(8, 8)
    ).apply(gray)

    variants["otsu"] = cv2.threshold(
        gray,
        0,
        255,
        cv2.THRESH_BINARY + cv2.THRESH_OTSU
    )[1]

    variants["adaptive"] = cv2.adaptiveThreshold(
        gray,
        255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY,
        31,
        11,
    )

    return variants


def clean_text(text):
    return "".join(
        character
        for character in text.upper()
        if character.isalnum()
    )

def run_ocr(image, language, psm):
    config = f"--oem 1 --psm {psm}"

    data = pytesseract.image_to_data(
        image,
        lang=language,
        config=config,
        output_type=pytesseract.Output.DICT,
    )

    texts = []
    confidences = []

    for text, confidence in zip(
        data["text"],
        data["conf"]
    ):
        text = clean_text(text)

        if not text:
            continue

        try:
            confidence = float(confidence)
        except ValueError:
            continue

        if confidence < 0:
            continue

        texts.append(text)
        confidences.append(confidence)

    if not texts:
        return None

    combined_text = "".join(texts)
    average_confidence = sum(confidences) / len(confidences)

    return {
        "text": combined_text,
        "confidence": average_confidence,
        "parts": texts,
    }

def main():
    if not IMAGE_PATH.exists():
        raise FileNotFoundError(
            f"Image not found: {IMAGE_PATH}"
        )

    image = cv2.imread(str(IMAGE_PATH))

    if image is None:
        raise ValueError(
            f"Unable to read image: {IMAGE_PATH}"
        )

    print("=" * 80)
    print("SMART PARKING - TESSERACT INDIC FALLBACK TEST")
    print("=" * 80)
    print(f"Image: {IMAGE_PATH}")
    print()

    variants = preprocess_variants(image)
    results = []

    for variant_name, variant_image in variants.items():
        print(f"\nPREPROCESSING: {variant_name}")
        print("-" * 80)
        for language in LANGUAGES:
            for psm in [6, 7, 8, 13]:

                result = run_ocr(
                    variant_image,
                    language,
                    psm,
                )

                if result is None:
                    continue

                results.append({
                    "variant": variant_name,
                    "language": language,
                    "psm": psm,
                    "text": result["text"],
                    "confidence": result["confidence"],
                })

                print(
                    f"{language:4} | "
                    f"PSM {psm:2} | "
                    f"{result['confidence']:6.2f} | "
                    f"{result['text']}"
                )

    print("\n")
    print("=" * 80)
    print("TOP RESULTS")
    print("=" * 80)

    results.sort(
        key=lambda result: result["confidence"],
        reverse=True,
    )

    for result in results[:20]:
        print(
            f"{result['confidence']:6.2f} | "
            f"{result['language']:4} | "
            f"PSM {result['psm']:2} | "
            f"{result['variant']:10} | "
            f"{result['text']}"
        )

    print("=" * 80)

if __name__ == "__main__":
    main()