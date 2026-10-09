from dataclasses import dataclass
from typing import List

import cv2
import pytesseract

from app.ocr.language_config import (
    INDIC_OCR_LANGUAGES,
)


TESSERACT_PATH = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

pytesseract.pytesseract.tesseract_cmd = TESSERACT_PATH


@dataclass
class OCRCandidate:
    text: str
    confidence: float
    language: str
    script: str
    preprocessing: str
    psm: int


class IndicFallbackOCR:

    def __init__(
        self,
        languages=None,
        psms=None,
    ):
        self.languages = (
            list(languages)
            if languages
            else list(INDIC_OCR_LANGUAGES)
        )

        self.psms = (
            list(psms)
            if psms
            else [6, 7, 11, 13]
        )

    @staticmethod
    def preprocess(image):
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

        gray = cv2.cvtColor(
            upscaled,
            cv2.COLOR_BGR2GRAY,
        )

        variants["4x_gray"] = gray

        clahe = cv2.createCLAHE(
            clipLimit=2.0,
            tileGridSize=(8, 8),
        )

        variants["4x_clahe"] = clahe.apply(gray)

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

        variants["4x_sharp"] = sharpened

        otsu = cv2.threshold(
            gray,
            0,
            255,
            cv2.THRESH_BINARY + cv2.THRESH_OTSU,
        )[1]

        variants["4x_otsu"] = otsu

        adaptive = cv2.adaptiveThreshold(
            gray,
            255,
            cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
            cv2.THRESH_BINARY,
            31,
            11,
        )

        variants["4x_adaptive"] = adaptive

        return variants

    @staticmethod
    def normalize_text(text: str) -> str:
        return " ".join(text.split())

    @staticmethod
    def calculate_confidence(confidences):
        values = []

        for value in confidences:
            try:
                value = float(value)
            except (TypeError, ValueError):
                continue

            if value >= 0:
                values.append(value)

        if not values:
            return 0.0

        return sum(values) / len(values)

    def _run_single(
        self,
        image,
        language,
        preprocessing,
        psm,
    ):
        config = f"--oem 1 --psm {psm}"

        data = pytesseract.image_to_data(
            image,
            lang=language.code,
            config=config,
            output_type=pytesseract.Output.DICT,
        )

        texts = []
        confidences = []

        for text, confidence in zip(
            data["text"],
            data["conf"],
        ):
            text = self.normalize_text(text)

            if not text:
                continue

            try:
                confidence = float(confidence)
            except (TypeError, ValueError):
                continue

            if confidence < 0:
                continue

            texts.append(text)
            confidences.append(confidence)

        if not texts:
            return None

        combined_text = " ".join(texts)

        confidence = self.calculate_confidence(
            confidences
        )

        return OCRCandidate(
            text=combined_text,
            confidence=confidence,
            language=language.name,
            script=language.script,
            preprocessing=preprocessing,
            psm=psm,
        )

    def recognize(self, image) -> List[OCRCandidate]:

        if image is None:
            raise ValueError("OCR image cannot be None")

        if len(image.shape) == 2:
            image = cv2.cvtColor(
                image,
                cv2.COLOR_GRAY2BGR,
            )

        variants = self.preprocess(image)

        candidates = []

        for preprocessing_name, variant in variants.items():

            if len(variant.shape) == 2:
                ocr_image = cv2.cvtColor(
                    variant,
                    cv2.COLOR_GRAY2BGR,
                )
            else:
                ocr_image = variant

            for language in self.languages:

                for psm in self.psms:

                    try:
                        result = self._run_single(
                            ocr_image,
                            language,
                            preprocessing_name,
                            psm,
                        )

                        if result is not None:
                            candidates.append(result)

                    except Exception:
                        continue

        candidates.sort(
            key=lambda candidate: candidate.confidence,
            reverse=True,
        )

        return candidates

    @staticmethod
    def best(candidates):

        if not candidates:
            return None

        return max(
            candidates,
            key=lambda candidate: candidate.confidence,
        )

    @staticmethod
    def top(candidates, limit=20):

        return candidates[:limit]