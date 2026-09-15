#!/usr/bin/env node

import { copyFileSync, mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { DatabaseSync } from 'node:sqlite';
import { readCsv } from './csv.mjs';

function options(args) {
  const value = { base: 'app/src/main/assets/doahindu1.sqlite', source: 'content/source',
    report: 'content/reports/content-conversion-report.json', output: null, checkBase: false };
  for (let index = 0; index < args.length; index += 1) {
    const key = args[index];
    if (key === '--check-base') {
      value.checkBase = true;
      continue;
    }
    if (!['--base', '--source', '--report', '--output'].includes(key) || !args[index + 1])
      throw new Error(`Argumen tidak valid: ${key}`);
    value[key.slice(2)] = args[index + 1];
    index += 1;
  }
  return value;
}

function sourceMatchesDatabase(content, database) {
  const databaseTopics = database.prepare(`SELECT topic_id,cat_id,topic_name,topic_stories
    FROM tbl_topics ORDER BY topic_id`).all();
  const databaseSections = database.prepare(`SELECT topic_id,section_number,section_title,
    section_text,sort_order FROM tbl_content_sections ORDER BY topic_id,sort_order`).all();
  const databaseQuotes = database.prepare(`SELECT quote_id,topic_id,section_number,quote_text,
    attribution,active,sort_order FROM tbl_daily_quotes ORDER BY sort_order`).all();
  const sourceTopics = content.topics.map(({ topic_id, cat_id, topic_name, topic_stories }) =>
    ({ topic_id, cat_id, topic_name, topic_stories }));
  const sourceSections = content.sections.map(({ topic_id, section_number, section_title,
    section_text, sort_order }) => ({ topic_id, section_number,
      section_title: section_title || null, section_text, sort_order }));
  const sourceQuotes = content.quotes.map(({ quote_id, topic_id, section_number, quote_text,
    attribution, active, sort_order }) => ({ quote_id, topic_id, section_number, quote_text,
      attribution, active, sort_order }));
  return JSON.stringify(sourceTopics) === JSON.stringify(databaseTopics)
    && JSON.stringify(sourceSections) === JSON.stringify(databaseSections)
    && JSON.stringify(sourceQuotes) === JSON.stringify(databaseQuotes);
}

function integer(value, label, errors, minimum = 0) {
  if (!/^-?\d+$/.test(value)) {
    errors.push(`${label} harus berupa bilangan bulat.`);
    return null;
  }
  const result = Number(value);
  if (!Number.isSafeInteger(result) || result < minimum) {
    errors.push(`${label} harus bernilai minimal ${minimum}.`);
    return null;
  }
  return result;
}

function duplicate(values, label, errors) {
  const seen = new Set();
  for (const value of values) {
    if (seen.has(value)) errors.push(`${label} duplikat: ${value}`);
    seen.add(value);
  }
}

function loadSource(source, database) {
  const errors = [];
  const topics = readCsv(resolve(source, 'topics.csv'),
    ['topic_id', 'cat_id', 'topic_name', 'topic_stories']);
  const sections = readCsv(resolve(source, 'content_sections.csv'),
    ['topic_id', 'section_number', 'section_title', 'section_text', 'sort_order']);
  const quotes = readCsv(resolve(source, 'daily_quotes.csv'),
    ['quote_id', 'topic_id', 'section_number', 'quote_text', 'attribution', 'active', 'sort_order']);

  for (const row of topics) {
    row.topic_id = integer(row.topic_id, `topics.csv baris ${row._row} topic_id`, errors, 1);
    row.cat_id = integer(row.cat_id, `topics.csv baris ${row._row} cat_id`, errors, 1);
    if (!row.topic_name.trim()) errors.push(`topics.csv baris ${row._row}: topic_name kosong.`);
    if (!row.topic_stories.trim()) errors.push(`topics.csv baris ${row._row}: topic_stories kosong.`);
  }
  for (const row of sections) {
    row.topic_id = integer(row.topic_id, `content_sections.csv baris ${row._row} topic_id`, errors, 1);
    row.section_number = integer(row.section_number,
      `content_sections.csv baris ${row._row} section_number`, errors, 1);
    row.sort_order = integer(row.sort_order,
      `content_sections.csv baris ${row._row} sort_order`, errors, 1);
    if (!row.section_text.trim()) errors.push(`content_sections.csv baris ${row._row}: section_text kosong.`);
  }
  for (const row of quotes) {
    row.quote_id = integer(row.quote_id, `daily_quotes.csv baris ${row._row} quote_id`, errors, 1);
    row.topic_id = integer(row.topic_id, `daily_quotes.csv baris ${row._row} topic_id`, errors, 1);
    row.section_number = integer(row.section_number,
      `daily_quotes.csv baris ${row._row} section_number`, errors, 1);
    row.active = integer(row.active, `daily_quotes.csv baris ${row._row} active`, errors, 0);
    row.sort_order = integer(row.sort_order,
      `daily_quotes.csv baris ${row._row} sort_order`, errors, 1);
    if (![0, 1].includes(row.active)) errors.push(`daily_quotes.csv baris ${row._row}: active harus 0 atau 1.`);
    if (!row.quote_text.trim()) errors.push(`daily_quotes.csv baris ${row._row}: quote_text kosong.`);
    if (!row.attribution.trim()) errors.push(`daily_quotes.csv baris ${row._row}: attribution kosong.`);
  }

  duplicate(topics.map((row) => row.topic_id), 'topic_id', errors);
  duplicate(sections.map((row) => `${row.topic_id}:${row.section_number}`), 'ayat', errors);
  duplicate(sections.map((row) => `${row.topic_id}:${row.sort_order}`), 'sort_order ayat', errors);
  duplicate(quotes.map((row) => row.quote_id), 'quote_id', errors);
  duplicate(quotes.map((row) => row.sort_order), 'sort_order quote', errors);
  duplicate(quotes.map((row) => `${row.topic_id}:${row.section_number}`), 'sumber quote', errors);

  const categories = new Set(database.prepare('SELECT cat_id FROM tbl_category').all()
    .map((row) => row.cat_id));
  const oldTopicIds = database.prepare('SELECT topic_id FROM tbl_topics').all()
    .map((row) => row.topic_id);
  const topicIds = new Set(topics.map((row) => row.topic_id));
  for (const id of oldTopicIds) if (!topicIds.has(id)) errors.push(`topic_id lama hilang: ${id}`);
  for (const row of topics) if (!categories.has(row.cat_id))
    errors.push(`topic_id ${row.topic_id} merujuk cat_id yang tidak ada: ${row.cat_id}`);
  const sectionKeys = new Set(sections.map((row) => `${row.topic_id}:${row.section_number}`));
  for (const row of sections) if (!topicIds.has(row.topic_id))
    errors.push(`Ayat merujuk topic_id yang tidak ada: ${row.topic_id}`);
  for (const row of quotes) if (!sectionKeys.has(`${row.topic_id}:${row.section_number}`))
    errors.push(`Quote ${row.quote_id} merujuk ayat yang tidak ada.`);

  const grouped = new Map();
  for (const row of sections) {
    if (!grouped.has(row.topic_id)) grouped.set(row.topic_id, []);
    grouped.get(row.topic_id).push(row.section_number);
  }
  for (const [topicId, numbers] of grouped) {
    numbers.sort((a, b) => a - b);
    numbers.forEach((number, index) => {
      if (number !== index + 1) errors.push(`topic_id ${topicId}: nomor ayat tidak berurutan pada ${number}.`);
    });
  }
  return { topics, sections, quotes, errors };
}

function applyContent(path, content) {
  const database = new DatabaseSync(path);
  database.exec('PRAGMA foreign_keys=ON; BEGIN IMMEDIATE');
  try {
    const updateTopic = database.prepare(`UPDATE tbl_topics SET cat_id=?,topic_name=?,
      topic_stories=? WHERE topic_id=?`);
    const insertTopic = database.prepare(`INSERT INTO tbl_topics
      (topic_id,cat_id,topic_name,topic_image,topic_stories,topic_stories_isfav,topic_last_viewed)
      VALUES (?,?,?,NULL,?,0,NULL)`);
    for (const row of content.topics) {
      if (updateTopic.run(row.cat_id, row.topic_name, row.topic_stories, row.topic_id).changes === 0)
        insertTopic.run(row.topic_id, row.cat_id, row.topic_name, row.topic_stories);
    }
    database.exec('DELETE FROM tbl_daily_quotes; DELETE FROM tbl_content_sections');
    const insertSection = database.prepare(`INSERT INTO tbl_content_sections
      (topic_id,section_number,section_title,section_text,sort_order) VALUES (?,?,?,?,?)`);
    for (const row of content.sections) insertSection.run(row.topic_id, row.section_number,
      row.section_title || null, row.section_text, row.sort_order);
    const insertQuote = database.prepare(`INSERT INTO tbl_daily_quotes
      (quote_id,topic_id,section_number,quote_text,attribution,active,sort_order)
      VALUES (?,?,?,?,?,?,?)`);
    for (const row of content.quotes) insertQuote.run(row.quote_id, row.topic_id,
      row.section_number, row.quote_text, row.attribution, row.active, row.sort_order);
    if (database.prepare('PRAGMA foreign_key_check').all().length)
      throw new Error('foreign_key_check gagal setelah konversi.');
    database.exec('COMMIT');
  } catch (error) {
    database.exec('ROLLBACK');
    throw error;
  } finally {
    database.close();
  }
}

const config = options(process.argv.slice(2));
const basePath = resolve(config.base);
const database = new DatabaseSync(basePath, { readOnly: true });
let content;
let matchesBase;
try {
  content = loadSource(config.source, database);
  matchesBase = content.errors.length === 0 && sourceMatchesDatabase(content, database);
} finally { database.close(); }
if (config.checkBase && !matchesBase)
  content.errors.push('CSV tidak sama dengan database aset. Hasilkan SQLite terbaru sebelum publikasi.');
const report = { valid: content.errors.length === 0, counts: { topics: content.topics.length,
  sections: content.sections.length, quotes: content.quotes.length }, matchesBase,
  errors: content.errors };
mkdirSync(dirname(resolve(config.report)), { recursive: true });
writeFileSync(resolve(config.report), `${JSON.stringify(report, null, 2)}\n`, 'utf8');
if (!report.valid) {
  console.error(`Validasi gagal dengan ${report.errors.length} kesalahan. Lihat ${resolve(config.report)}`);
  process.exit(1);
}
if (config.output) {
  const outputPath = resolve(config.output);
  if (outputPath === basePath) throw new Error('Output tidak boleh menimpa database dasar.');
  mkdirSync(dirname(outputPath), { recursive: true });
  copyFileSync(basePath, outputPath);
  applyContent(outputPath, content);
  console.log(`SQLite dihasilkan: ${outputPath}`);
}
console.log(`Valid: ${report.counts.topics} topik, ${report.counts.sections} ayat, ${report.counts.quotes} quote.`);
console.log(`Laporan: ${resolve(config.report)}`);
