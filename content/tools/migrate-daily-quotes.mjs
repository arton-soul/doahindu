#!/usr/bin/env node

import { constants, copyFileSync, existsSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { DatabaseSync } from 'node:sqlite';

const databasePath = resolve('app/src/main/assets/doahindu1.sqlite');
const backupPath = resolve('.backups/doahindu1-before-daily-quotes.sqlite');
const apply = process.argv.includes('--apply');
let database = new DatabaseSync(databasePath, { readOnly: true });
const sectionCount = database.prepare(
  'SELECT COUNT(*) count FROM tbl_content_sections',
).get().count;
const tableExists = Boolean(database.prepare(
  "SELECT 1 FROM sqlite_master WHERE type='table' AND name='tbl_daily_quotes'",
).get());
database.close();

if (sectionCount === 0) throw new Error('tbl_content_sections masih kosong.');
if (!apply) {
  console.log(`Dry-run berhasil: ${sectionCount} ayat siap menjadi quote.`);
  console.log(`Tabel sudah ada: ${tableExists ? 'ya' : 'tidak'}`);
  process.exit(0);
}
if (tableExists) throw new Error('tbl_daily_quotes sudah ada; migrasi dibatalkan.');
if (existsSync(backupPath)) throw new Error(`Backup sudah ada: ${backupPath}`);

mkdirSync(dirname(backupPath), { recursive: true });
copyFileSync(databasePath, backupPath, constants.COPYFILE_EXCL);
database = new DatabaseSync(databasePath);
try {
  database.exec('PRAGMA foreign_keys=ON; BEGIN IMMEDIATE');
  try {
    database.exec(`CREATE TABLE tbl_daily_quotes (
      quote_id INTEGER PRIMARY KEY,
      topic_id INTEGER NOT NULL,
      section_number INTEGER NOT NULL,
      quote_text TEXT NOT NULL,
      attribution TEXT NOT NULL,
      active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0, 1)),
      sort_order INTEGER NOT NULL UNIQUE,
      UNIQUE(topic_id, section_number),
      FOREIGN KEY(topic_id, section_number)
        REFERENCES tbl_content_sections(topic_id, section_number) ON DELETE CASCADE
    )`);
    database.exec(`INSERT INTO tbl_daily_quotes
      (quote_id, topic_id, section_number, quote_text, attribution, active, sort_order)
      SELECT ROW_NUMBER() OVER (ORDER BY s.topic_id, s.sort_order), s.topic_id,
        s.section_number, s.section_text, t.topic_name, 1,
        ROW_NUMBER() OVER (ORDER BY s.topic_id, s.sort_order)
      FROM tbl_content_sections s
      JOIN tbl_topics t ON t.topic_id=s.topic_id
      ORDER BY s.topic_id, s.sort_order`);
    const quoteCount = database.prepare('SELECT COUNT(*) count FROM tbl_daily_quotes').get().count;
    const orphans = database.prepare(`SELECT COUNT(*) count FROM tbl_daily_quotes q
      LEFT JOIN tbl_content_sections s ON s.topic_id=q.topic_id
        AND s.section_number=q.section_number WHERE s.topic_id IS NULL`).get().count;
    if (quoteCount !== sectionCount || orphans !== 0) {
      throw new Error(`Validasi quote gagal: rows=${quoteCount}, orphans=${orphans}`);
    }
    database.exec('COMMIT');
  } catch (error) {
    database.exec('ROLLBACK');
    throw error;
  }
  const integrity = database.prepare('PRAGMA integrity_check').get().integrity_check;
  if (integrity !== 'ok') throw new Error(`Integrity check gagal: ${integrity}`);
  console.log(`Migrasi diterapkan: ${sectionCount} quote.`);
  console.log(`Backup: ${backupPath}`);
} finally {
  database.close();
}
