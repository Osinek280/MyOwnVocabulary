# FSRS-6

Reference: [official algorithm](https://github.com/open-spaced-repetition/awesome-fsrs/wiki/The-Algorithm)
and [py-fsrs v6.3.2](https://github.com/open-spaced-repetition/py-fsrs/blob/v6.3.2/fsrs/scheduler.py).
Numerical regression fixtures are generated independently by `tools/fsrs/generate_reference.py`.

## API and scheduling policy

`FSRS6.calculate(card, reviewedAt)` returns four proposals in `Again, Hard, Good, Easy` order.
`review(card, rating, reviewedAt)` returns the corresponding identical proposal.
Each proposal contains the complete new card and review log, without changing the input card.
Colors, translated rating labels and interval formatting belong to the UI.

Times are nonnegative UTC epoch milliseconds, supported on Android API 24 without java.time.
Elapsed days are **completed 24-hour periods**, matching the pinned Python reference, rather than
the planned interval or local-calendar date changes. Reviews before the last review are rejected.
Stability is in days; difficulty is in [1, 10]. New cards have no memory state or last review.

Defaults: retention 0.90, maximum 36500 days, learning steps 1/10 minutes, relearning step 10 minutes.
Empty step lists enable direct FSRS scheduling. Changing the step configuration is supported;
an old step beyond the new list graduates on a successful answer, or restarts on Again.
Again during Review counts as a lapse; repeated failures during learning do not add lapses.

The memory equations, short-term Hard stability floor, parameter bounds, and unfuzzed rounding
(nearest integer, ties to even) match py-fsrs 6.3.2. Unlike its random fuzz implementation, we use
a reproducible quantile derived from the seed, card id, review count and last review time.
Review intervals use bounded inclusive integer sampling and are ordered across ratings.
No overdue-time lower bound is imposed on fuzz. Caps, whole-day rounding and learning steps
can necessarily move the actual interval away from the mathematically exact target retention.
`intervalDays()` exposes the unrounded inverse of `forgettingCurve()` for checking that target.

## Persistence

`FsrsRepository` connects the module to vocabulary entries via Room:

```kotlin
val repository = FsrsRepository(database)
val choices = repository.preview(entryId, reviewedAt)
// Keep these proposals for the displayed choices, then save the selected proposal.
val card = repository.save(choices.single { it.choice == rating })
val dueCards = repository.due(now)
```

State and individual reviews are saved in one transaction. Saving twice, saving after another
review, saving for a deleted entry, or saving a proposal from another configuration is rejected.
`history()` returns chronological reviews, using insertion order to resolve equal timestamps.
The state and per-card history are deleted with their vocabulary entry; aggregate session history
continues to be handled separately. Migration 11 -> 12 adds tables without deleting existing data.

The quiz UI currently has no calls to this module and no four-rating answer flow. Integrate through
the repository when adding that flow; a binary correct/incorrect answer cannot uniquely determine
Hard/Good/Easy. Existing aggregate learning statistics are not silently converted into FSRS history.

## Migration from the removed experimental FSRS7 API

There were no callers or persisted v7 states in this project. `FSRS7`, the two stability fields,
integer phases, presentation fields in Grade and the unused pseudo-Alea generator were removed.
Use `FSRS6`, `MemoryState(stability, difficulty)` and typed `CardPhase` values.
If importing a v7 state from elsewhere, rebuild using actual ratings and timestamps; do not copy
the old long-term stability into the v6 state. `replay()` supports rebuilding under new v6 weights.
The database explicitly checks the stored algorithm version before interpreting its memory state.

## Checks

`FsrsTest` verifies 708 reference cases, retention inversion, time handling, learning cycles,
fuzz determinism/order/caps, validation, parameter ownership, replay and entity round trips.
`FsrsDatabaseTest` exercises transactions, stale previews, deletion, timestamp ties and migration
on an Android device/emulator. Run `:app:testDebugUnitTest` and `:app:connectedDebugAndroidTest`.
`python tools/fsrs/verify_migration.py` additionally checks the actual migration SQL against
Room's generated schema on host SQLite, verifies data preservation and tests foreign-key cascades.
