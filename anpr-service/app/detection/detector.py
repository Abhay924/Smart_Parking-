from pathlib import Path

import cv2
from fast_alpr import ALPR


class PlateDetector:
    def __init__(self):
        self.alpr = ALPR(
            detector_model="yolo-v9-t-384-license-plate-end2end",
            ocr_model="cct-s-v2-global-model",
            ocr_device="cpu",
        )

    def detect(self, image_path: str):
        path = Path(image_path)

        if not path.exists():
            raise FileNotFoundError(f"Image not found: {image_path}")

        image = cv2.imread(str(path))

        if image is None:
            raise ValueError(f"Unable to read image: {image_path}")

        return self.alpr.predict(image)