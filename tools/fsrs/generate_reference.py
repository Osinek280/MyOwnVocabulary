"""Generate numerical/scheduling fixtures with the independent official py-fsrs 6.3.2.

python -m pip install --no-deps --target app/build/fsrs-reference fsrs==6.3.2
python tools/fsrs/generate_reference.py
Only the resulting CSV is needed by the Kotlin tests; Python is not an app dependency.
"""
import csv
import importlib.metadata
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

root = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(root / "app/build/fsrs-reference"))
from fsrs import Card, Rating, Scheduler, State

assert importlib.metadata.version("fsrs") == "6.3.2"
day = 86_400_000
epoch = datetime(2026, 1, 1, tzinfo=timezone.utc)
last_ms = int(epoch.timestamp() * 1000)
rows = []


def add(mode, phase, step, stability, difficulty, elapsed_ms, retention=0.9, maximum=36500):
    options = {"enable_fuzzing": False, "desired_retention": retention, "maximum_interval": maximum}
    if mode == "empty":
        options.update(learning_steps=[], relearning_steps=[])
    elif mode == "single":
        options.update(learning_steps=[timedelta(minutes=1)], relearning_steps=[timedelta(minutes=10)])
    scheduler = Scheduler(**options)
    state = {"Added": State.Learning, "Learning": State.Learning,
             "Review": State.Review, "ReLearning": State.Relearning}[phase]
    before = Card(card_id=1, state=state, step=step, stability=stability, difficulty=difficulty,
                  due=epoch, last_review=None if phase == "Added" else epoch)
    when = epoch + timedelta(milliseconds=elapsed_ms)
    for rating in Rating:
        result, _ = scheduler.review_card(before, rating, review_datetime=when)
        duration = int((result.due - when).total_seconds() * 1000)
        result_phase = {State.Learning: "Learning", State.Review: "Review",
                        State.Relearning: "ReLearning"}[result.state]
        rows.append([mode, phase, step if step is not None else "", stability or "", difficulty or "",
                     last_ms, elapsed_ms, rating.value, retention, maximum,
                     result.stability, result.difficulty, result_phase,
                     result.step if result.step is not None else "", duration])


for mode in ("default", "empty", "single"):
    add(mode, "Added", 0, None, None, 0)
    for phase, step in (("Learning", 0), ("Learning", 1), ("Learning", 4),
                        ("ReLearning", 0), ("ReLearning", 4), ("Review", None)):
        for elapsed in (0, 600000, day - 1, day, 30 * day):
            add(mode, phase, step, 10.0, 5.0, elapsed)

for stability in (0.001, 0.212, 10.0, 1000.0, 100000.0):
    for difficulty in (1.0, 5.0, 10.0):
        for elapsed in (0, day - 1, day, 7 * day, 1000 * day):
            add("default", "Review", None, stability, difficulty, elapsed)

for retention in (0.8, 0.95, 0.99):
    for maximum in (1, 10, 36500):
        add("empty", "Review", None, 100.0, 5.0, 30 * day, retention, maximum)

output = root / "app/src/test/resources/fsrs6-reference.csv"
output.parent.mkdir(parents=True, exist_ok=True)
with output.open("w", newline="", encoding="utf-8") as file:
    writer = csv.writer(file, lineterminator="\n")
    writer.writerow(["mode", "phase", "step", "stability", "difficulty", "last_ms", "elapsed_ms", "rating",
                     "retention", "maximum", "next_stability", "next_difficulty", "next_phase", "next_step",
                     "duration_ms"])
    writer.writerows(rows)
print(f"Wrote {len(rows)} independent reference cases to {output}")
