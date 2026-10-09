from typing import Any
class PlateRecognizer:
    @staticmethod
    def extract(result: Any):

        if not result:
            return None
        best = None
        best_score = 0.0
        for detection in result:
            ocr = getattr(detection, "ocr", None)

            if ocr is None:
                continue
            text = getattr(ocr, "text", None)
            confidence = getattr(ocr, "confidence", None)

            if not text:
                continue

            ocr_confidence = (
                PlateRecognizer._calculate_confidence(
                    confidence
                )
            )

            detection_confidence = float(
                getattr(detection.detection, "confidence", 0.0)
            )

            overall_confidence = min(
                detection_confidence,
                ocr_confidence
            )

            candidate = {
                "plate_number": PlateRecognizer._normalize(text),
                "confidence": overall_confidence,
                "detection_confidence": detection_confidence,
                "ocr_confidence": ocr_confidence,
            }

            if overall_confidence > best_score:
                best = candidate
                best_score = overall_confidence

        return best

    @staticmethod
    def _calculate_confidence(confidence):

        if confidence is None:
            return 0.0

        if isinstance(confidence, (int, float)):
            return float(confidence)

        if isinstance(confidence, (list, tuple)):
            values = [
                float(value)
                for value in confidence
                if value is not None
            ]

            if not values:
                return 0.0

            return sum(values) / len(values)

        return 0.0

    @staticmethod
    def _normalize(text: str) -> str:

        return "".join(
            character
            for character in text.upper()
            if character.isalnum()
        )