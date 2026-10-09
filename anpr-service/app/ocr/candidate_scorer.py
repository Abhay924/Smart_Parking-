from collections import defaultdict
from dataclasses import dataclass

from app.ocr.candidate_validator import (
    extract_best_plate_candidate,
)

@dataclass(frozen=True)
class ScoredCandidate:
    plate: str
    language: str
    script: str
    preprocessing: str
    psm: int
    ocr_confidence: float
    format_score: float
    agreement_score: float
    final_score: float
    valid_format: bool


def calculate_agreement(
    plate: str,
    candidates,
) -> float:

    if not plate:
        return 0.0

    matches = 0
    total = 0

    for candidate in candidates:
        validation = extract_best_plate_candidate(
            candidate.text
        )

        if validation is None:
            continue

        total += 1

        if validation.normalized == plate:
            matches += 1

    if total == 0:
        return 0.0

    ratio = matches / total

    if ratio >= 0.60:
        return 1.0

    if ratio >= 0.40:
        return 0.75

    if ratio >= 0.20:
        return 0.50

    return 0.0


def score_candidates(candidates):

    extracted = []

    for candidate in candidates:

        validation = extract_best_plate_candidate(
            candidate.text
        )

        if validation is None:
            continue

        if not validation.normalized:
            continue

        extracted.append(
            (
                candidate,
                validation,
            )
        )

    if not extracted:
        return []

    # Group candidates by normalized plate.
    groups = defaultdict(list)

    for candidate, validation in extracted:
        groups[validation.normalized].append(
            (candidate, validation)
        )

    scored = []

    for plate, group in groups.items():

        group_candidates = [
            item[0]
            for item in group
        ]

        agreement_score = calculate_agreement(
            plate,
            candidates,
        )

        for candidate, validation in group:

            ocr_score = min(
                max(candidate.confidence / 100.0, 0.0),
                1.0,
            )

            final_score = (
                ocr_score * 0.45
                + validation.format_score * 0.35
                + agreement_score * 0.20
            )

            scored.append(
                ScoredCandidate(
                    plate=plate,
                    language=candidate.language,
                    script=candidate.script,
                    preprocessing=candidate.preprocessing,
                    psm=candidate.psm,
                    ocr_confidence=candidate.confidence,
                    format_score=validation.format_score,
                    agreement_score=agreement_score,
                    final_score=final_score,
                    valid_format=validation.valid,
                )
            )

    scored.sort(
        key=lambda candidate: candidate.final_score,
        reverse=True,
    )

    return scored


def decide(scored_candidates):

    if not scored_candidates:
        return {
            "status": "MANUAL_REVIEW",
            "plate": None,
            "score": 0.0,
            "reason": "No valid OCR candidate",
        }

    best = scored_candidates[0]

    if (
        best.valid_format
        and best.final_score >= 0.80
        and best.agreement_score >= 0.50
    ):
        return {
            "status": "ACCEPT",
            "plate": best.plate,
            "score": best.final_score,
            "reason": "Strong format and OCR agreement",
        }

    return {
        "status": "MANUAL_REVIEW",
        "plate": best.plate,
        "score": best.final_score,
        "reason": "OCR confidence or candidate agreement insufficient",
    }