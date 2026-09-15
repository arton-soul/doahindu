#!/usr/bin/env node

import { constants, copyFileSync, existsSync, mkdirSync, readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { DatabaseSync } from 'node:sqlite';

const DEFAULT_DATABASE = 'app/src/main/assets/doahindu1.sqlite';
const DEFAULT_PREVIEW = 'content/reports/content-sections-preview.json';
const DEFAULT_BACKUP = '.backups/doahindu1-before-content-sections.sqlite';

function parseArguments(args) {
  const options = {
    apply: false,
    database: DEFAULT_DATABASE,
    preview: DEFAULT_PREVIEW,
    backup: DEFAULT_BACKUP,
  };
  for (let index = 0; index < args.length; index += 1) {
    const argument = args[index];
    if (argument === '--apply') {
      options.apply = true;
    } else if (['--database', '--preview', '--backup'].includes(argument)) {
      const value = args[index + 1];
      if (!value) throw new Error(`Nilai untuk ${argument} belum diberikan.`);
      options[argument.slice(2)] = value;
      index += 1;
    } else if (argument === '--help') {
      options.help = true;
    } else {
      throw new Error(`Argumen tidak dikenal: ${argument}`);
    }
  }
  return options;
}

function loadAndValidatePreview(previewPath, database) {
  const preview = JSON.parse(readFileSync(resolve(previewPath), 'utf8'));
  if (preview.table !== 'tbl_content_sections' || !Array.isArray(preview.rows)) {
    throw new Error('Format pratinjau tidak valid.');
  }
  if (preview.rows.length === 0) throw new Error('Pratinjau tidak berisi ayat.');

  const topicIds = new Set(database.prepare('SELECT topic_id FROM tbl_topics').all()
    .map((row) => row.topic_id));
  const keys = new Set();
  const sortOrders = new Set();
  for (const row of preview.rows) {
    if (!Number.isInteger(row.topic_id) || !topicIds.has(row.topic_id)) {
      throw new Error(`topic_id tidak valid: ${row.topic_id}`);
    }
    if (!Number.isInteger(row.section_number) || row.section_number < 1) {
      throw new Error(`section_number tidak valid pada topic_id ${row.topic_id}.`);
    }
    if (!Number.isInteger(row.sort_order) || row.sort_order < 1) {
      throw new Error(`sort_order tidak valid pada topic_id ${row.topic_id}.`);
    }
    if (typeof row.section_text !== 'string' || !row.section_text.trim()) {
      throw new Error(`section_text kosong pada ${row.topic_id}:${row.section_number}.`);
    }
    const key = `${row.topic_id}:${row.section_number}`;
    const sortKey = `${row.topic_id}:${row.sort_order}`;
    if (keys.has(key)) throw new Error(`Ayat duplikat: ${key}`);
    if (sortOrders.has(sortKey)) throw new Error(`sort_order duplikat: ${sortKey}`);
    keys.add(key);
    sortOrders.add(sortKey);
  }
  return preview.rows;
}

function snapshotTopics(database) {
  return JSON.stringify(database.prepare(
    'SELECT topic_id, cat_id, topic_name, hex(topic_image) topic_image, '
      + 'topic_stories, topic_stories_isfav, topic_last_viewed FROM tbl_topics ORDER BY topic_id',
  ).all());
}

function tableExists(database) {
  return Boolean(database.prepare(
    "SELECT 1 FROM sqlite_master WHERE type='table' AND name='tbl_content_sections'",
  ).get());
}

function validateMigratedDatabase(database, expectedRows, topicSnapshot) {
  const integrity = database.prepare('PRAGMA integrity_check').get().integrity_check;
  if (integrity !== 'ok') throw new Error(`Integrity check gagal: ${integrity}`);
  if (snapshotTopics(database) !== topicSnapshot) {
    throw new Error('Isi tbl_topics berubah selama migrasi.');
  }
  const count = database.prepare('SELECT COUNT(*) count FROM tbl_content_sections').get().count;
  if (count !== expectedRows) {
    throw new Error(`Jumlah ayat ${count}, seharusnya ${expectedRows}.`);
  }
  const orphans = database.prepare(
    'SELECT COUNT(*) count FROM tbl_content_sections s '
      + 'LEFT JOIN tbl_topics t ON t.topic_id=s.topic_id WHERE t.topic_id IS NULL',
  ).get().count;
  if (orphans !== 0) throw new Error(`Ditemukan ${orphans} ayat tanpa topik.`);
}

function printHelp() {
  console.log(`Pemakaian:
  node --no-warnings content/tools/migrate-content-sections.mjs [opsi]

Tanpa --apply, alat hanya melakukan validasi dry-run.

Opsi:
  --apply          Terapkan migrasi setelah membuat backup
  --database PATH  SQLite tujuan (default: ${DEFAULT_DATABASE})
  --preview PATH   Pratinjau JSON (default: ${DEFAULT_PREVIEW})
  --backup PATH    Lokasi backup (default: ${DEFAULT_BACKUP})
  --help           Tampilkan bantuan`);
}

export function migrate(options) {
  const databasePath = resolve(options.database);
  const backupPath = resolve(options.backup);
  let database = new DatabaseSync(databasePath, { readOnly: true });
  const rows = loadAndValidatePreview(options.preview, database);
  const originalTopics = snapshotTopics(database);
  const alreadyMigrated = tableExists(database);
  const existingRows = alreadyMigrated
    ? database.prepare('SELECT COUNT(*) count FROM tbl_content_sections').get().count
    : 0;
  database.close();

  if (!options.apply) {
    return { applied: false, rowCount: rows.length, alreadyMigrated, existingRows };
  }
  if (alreadyMigrated) {
    throw new Error('tbl_content_sections sudah ada; migrasi dibatalkan agar data tidak tertimpa.');
  }
  if (existsSync(backupPath)) {
    throw new Error(`Backup sudah ada dan tidak akan ditimpa: ${backupPath}`);
  }

  mkdirSync(dirname(backupPath), { recursive: true });
  copyFileSync(databasePath, backupPath, constants.COPYFILE_EXCL);

  database = new DatabaseSync(databasePath);
  try {
    database.exec('PRAGMA foreign_keys=ON');
    database.exec('BEGIN IMMEDIATE');
    try {
      database.exec(`CREATE TABLE tbl_content_sections (
        topic_id INTEGER NOT NULL,
        section_number INTEGER NOT NULL,
        section_title TEXT,
        section_text TEXT NOT NULL,
        sort_order INTEGER NOT NULL,
        PRIMARY KEY (topic_id, section_number),
        UNIQUE (topic_id, sort_order),
        FOREIGN KEY (topic_id) REFERENCES tbl_topics(topic_id) ON DELETE CASCADE
      )`);
      database.exec('CREATE INDEX index_content_sections_topic_sort '
        + 'ON tbl_content_sections(topic_id, sort_order)');
      const insert = database.prepare(
        'INSERT INTO tbl_content_sections '
          + '(topic_id, section_number, section_title, section_text, sort_order) '
          + 'VALUES (?, ?, ?, ?, ?)',
      );
      for (const row of rows) {
        insert.run(row.topic_id, row.section_number, row.section_title,
          row.section_text, row.sort_order);
      }
      validateMigratedDatabase(database, rows.length, originalTopics);
      database.exec('COMMIT');
    } catch (error) {
      database.exec('ROLLBACK');
      throw error;
    }
    validateMigratedDatabase(database, rows.length, originalTopics);
  } finally {
    database.close();
  }
  return { applied: true, rowCount: rows.length, backupPath };
}

if (import.meta.url === new URL(`file://${resolve(process.argv[1]).replace(/\\/g, '/')}`).href) {
  try {
    const options = parseArguments(process.argv.slice(2));
    if (options.help) {
      printHelp();
    } else {
      const result = migrate(options);
      if (result.applied) {
        console.log(`Migrasi diterapkan: ${result.rowCount} ayat.`);
        console.log(`Backup: ${result.backupPath}`);
      } else {
        console.log(`Dry-run berhasil: ${result.rowCount} ayat tervalidasi.`);
        console.log(`Tabel sudah ada: ${result.alreadyMigrated ? 'ya' : 'tidak'}`);
      }
    }
  } catch (error) {
    console.error(error instanceof Error ? error.message : error);
    process.exitCode = 1;
  }
}
