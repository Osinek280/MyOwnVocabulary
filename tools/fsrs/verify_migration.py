"""Check the actual migration SQL against Room's generated schema using host SQLite.

Run :app:kspDebugKotlin first, then python tools/fsrs/verify_migration.py.
Android transaction/concurrency tests remain in FsrsDatabaseTest.
"""
import re
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parents[2]
base = root / "app/src/main/java/com/example/myownvocabulary/data"
source = (base / "AppDatabase.kt").read_text(encoding="utf-8")
generated = (root / "app/build/generated/ksp/debug/kotlin/com/example/myownvocabulary/data/AppDatabase_Impl.kt")
room = generated.read_text(encoding="utf-8").split("override fun createAllTables", 1)[1].split(
    "override fun dropAllTables", 1)[0]
schema_sql = re.findall(r'connection.execSQL\("([^"\n]+)"\)', room)
migration = source.split("internal val MIGRATION_11_12", 1)[1].split("internal val MIGRATION_10_11", 1)[0]
migration_sql = [a or b for a, b in re.findall(r'db.execSQL\(\s*(?:"""(.*?)"""|"([^"\n]+)")\s*\)',
                                            migration, flags=re.S)]
assert len(migration_sql) == 4, "Unexpected migration structure"


def database():
    db = sqlite3.connect(":memory:")
    db.execute("PRAGMA foreign_keys=ON")
    return db


expected = database()
actual = database()
for statement in schema_sql:
    expected.execute(statement)
    if "fsrs_" not in statement:
        actual.execute(statement)
actual.execute("INSERT INTO entries(id,term,translation,languageCode,createdAt,kind) "
               "VALUES('entry','cat','kot','en',123,'Word')")
actual.execute("INSERT INTO learning_progress VALUES('entry','LEARNING',1,3,7,2,123,456)")
actual.execute("INSERT INTO learning_sessions VALUES('session','DAILY',123,456,10,10,7)")
before = {name: actual.execute(f"SELECT * FROM {name}").fetchall()
          for name in ("entries", "learning_progress", "learning_sessions")}
with actual:
    for statement in migration_sql:
        actual.execute(statement)


def signature(db, table):
    columns = db.execute(f"PRAGMA table_info({table})").fetchall()
    foreign_keys = db.execute(f"PRAGMA foreign_key_list({table})").fetchall()
    indexes = []
    for _, name, unique, origin, partial in db.execute(f"PRAGMA index_list({table})"):
        indexes.append((name, unique, origin, partial, db.execute(f"PRAGMA index_info('{name}')").fetchall()))
    return columns, foreign_keys, sorted(indexes)


for table in ("fsrs_cards", "fsrs_reviews"):
    assert signature(actual, table) == signature(expected, table), table
for table, rows in before.items():
    assert actual.execute(f"SELECT * FROM {table}").fetchall() == rows, table

actual.execute("INSERT INTO fsrs_cards VALUES('entry',2.3,5,456,123,1,0,'Learning',1,0,6)")
actual.execute("INSERT INTO fsrs_reviews(entryId,rating,reviewedAt,elapsedDays,algorithmVersion) "
               "VALUES('entry',3,123,0,6)")
try:
    actual.execute("INSERT INTO fsrs_reviews(entryId,rating,reviewedAt,elapsedDays,algorithmVersion) "
                   "VALUES('missing',3,123,0,6)")
    raise AssertionError("Missing parent accepted")
except sqlite3.IntegrityError:
    pass
actual.execute("DELETE FROM entries WHERE id='entry'")
assert actual.execute("SELECT COUNT(*) FROM fsrs_cards").fetchone()[0] == 0
assert actual.execute("SELECT COUNT(*) FROM fsrs_reviews").fetchone()[0] == 0
assert actual.execute("SELECT COUNT(*) FROM learning_sessions").fetchone()[0] == 1
print("Migration 11->12 matches Room schema, preserves data and enforces foreign keys/cascades")
