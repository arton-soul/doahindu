#!/usr/bin/env node

import { mkdirSync } from 'node:fs';
import { resolve } from 'node:path';
import { DatabaseSync } from 'node:sqlite';
import { writeCsv } from './csv.mjs';

const databasePath = resolve('app/src/main/assets/doahindu1.sqlite');
const outputDirectory = resolve('content/source');
mkdirSync(outputDirectory, { recursive: true });
const database = new DatabaseSync(databasePath, { readOnly: true });
try {
  const topics = database.prepare(`SELECT topic_id,cat_id,topic_name,topic_stories
    FROM tbl_topics ORDER BY topic_id`).all();
  const sections = database.prepare(`SELECT topic_id,section_number,section_title,
    section_text,sort_order FROM tbl_content_sections ORDER BY topic_id,sort_order`).all();
  const quotes = database.prepare(`SELECT quote_id,topic_id,section_number,quote_text,
    attribution,active,sort_order FROM tbl_daily_quotes ORDER BY sort_order`).all();
  writeCsv(resolve(outputDirectory, 'topics.csv'),
    ['topic_id', 'cat_id', 'topic_name', 'topic_stories'], topics);
  writeCsv(resolve(outputDirectory, 'content_sections.csv'),
    ['topic_id', 'section_number', 'section_title', 'section_text', 'sort_order'], sections);
  writeCsv(resolve(outputDirectory, 'daily_quotes.csv'),
    ['quote_id', 'topic_id', 'section_number', 'quote_text', 'attribution', 'active',
      'sort_order'], quotes);
  console.log(`Diekspor: ${topics.length} topik, ${sections.length} ayat, ${quotes.length} quote.`);
} finally {
  database.close();
}
