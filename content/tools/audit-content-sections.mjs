#!/usr/bin/env node

import { DatabaseSync } from 'node:sqlite';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const DEFAULT_DATABASE = 'app/src/main/assets/doahindu1.sqlite';
const DEFAULT_REPORT = 'content/reports/section-audit.json';
const DEFAULT_PREVIEW = 'content/reports/content-sections-preview.json';
const BHAGAWADGITA_TITLE = /^Bhagawadgita\s+BAB\s+([IVXLCDM]+)\s*$/i;

export function parseMarker(line) {
  const standalone = line.match(/^\s*(\d+)\.?\s*$/);
  if (standalone) {
    return { number: Number(standalone[1]), remainder: '' };
  }

  const inline = line.match(/^\s*(\d+)\.\s*(\D.+?)\s*$/);
  if (inline) {
    return { number: Number(inline[1]), remainder: inline[2] };
  }

  return null;
}

export function parseSections(text) {
  const lines = String(text ?? '').replace(/\r\n?/g, '\n').split('\n');
  const markers = [];
  const ambiguousLines = [];

  lines.forEach((line, index) => {
    const marker = parseMarker(line);
    if (marker) {
      markers.push({ ...marker, lineIndex: index });
    } else if (/^\s*\d/.test(line)) {
      ambiguousLines.push({ line: index + 1, text: line });
    }
  });

  const sections = markers.map((marker, index) => {
    const nextLineIndex = markers[index + 1]?.lineIndex ?? lines.length;
    const body = lines.slice(marker.lineIndex + 1, nextLineIndex);
    if (marker.remainder) body.unshift(marker.remainder);
    return {
      section_number: marker.number,
      section_text: body.join('\n').trim(),
      source_line: marker.lineIndex + 1,
    };
  });

  const numbers = sections.map((section) => section.section_number);
  const occurrences = new Map();
  for (const number of numbers) {
    occurrences.set(number, (occurrences.get(number) ?? 0) + 1);
  }
  const duplicateNumbers = [...occurrences.entries()]
    .filter(([, count]) => count > 1)
    .map(([number]) => number);
  const maximum = numbers.length ? Math.max(...numbers) : 0;
  const present = new Set(numbers);
  const missingNumbers = Array.from({ length: maximum }, (_, index) => index + 1)
    .filter((number) => !present.has(number));
  const nonIncreasing = [];
  for (let index = 1; index < numbers.length; index += 1) {
    if (numbers[index] <= numbers[index - 1]) {
      nonIncreasing.push({ previous: numbers[index - 1], current: numbers[index] });
    }
  }

  const preamble = markers.length
    ? lines.slice(0, markers[0].lineIndex).join('\n').trim()
    : String(text ?? '').trim();

  return {
    sections,
    diagnostics: {
      markerCount: markers.length,
      firstNumber: numbers[0] ?? null,
      lastNumber: numbers.at(-1) ?? null,
      duplicateNumbers,
      missingNumbers,
      nonIncreasing,
      ambiguousLines,
      preamble,
      emptySectionNumbers: sections
        .filter((section) => !section.section_text)
        .map((section) => section.section_number),
    },
  };
}

function parseArguments(args) {
  const options = {
    database: DEFAULT_DATABASE,
    report: DEFAULT_REPORT,
    preview: DEFAULT_PREVIEW,
  };
  for (let index = 0; index < args.length; index += 1) {
    const argument = args[index];
    if (argument === '--database' || argument === '--report' || argument === '--preview') {
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

function writeJson(path, value) {
  const absolutePath = resolve(path);
  mkdirSync(dirname(absolutePath), { recursive: true });
  writeFileSync(absolutePath, `${JSON.stringify(value, null, 2)}\n`, 'utf8');
  return absolutePath;
}

export function auditDatabase(databasePath) {
  const absoluteDatabasePath = resolve(databasePath);
  const database = new DatabaseSync(absoluteDatabasePath, { readOnly: true });
  try {
    const integrity = database.prepare('PRAGMA integrity_check').get().integrity_check;
    const topics = database.prepare(
      'SELECT topic_id, topic_name, topic_stories FROM tbl_topics ORDER BY topic_id',
    ).all();
    const selected = topics.filter((topic) => BHAGAWADGITA_TITLE.test(topic.topic_name));
    const reportTopics = [];
    const previewRows = [];

    for (const topic of selected) {
      const parsed = parseSections(topic.topic_stories);
      const diagnostic = parsed.diagnostics;
      const valid = diagnostic.markerCount > 0
        && diagnostic.firstNumber === 1
        && diagnostic.duplicateNumbers.length === 0
        && diagnostic.missingNumbers.length === 0
        && diagnostic.nonIncreasing.length === 0
        && diagnostic.ambiguousLines.length === 0
        && diagnostic.preamble === ''
        && diagnostic.emptySectionNumbers.length === 0;

      reportTopics.push({
        topic_id: topic.topic_id,
        topic_name: topic.topic_name,
        valid,
        ...diagnostic,
      });
      parsed.sections.forEach((section, index) => {
        previewRows.push({
          topic_id: topic.topic_id,
          section_number: section.section_number,
          section_title: null,
          section_text: section.section_text,
          sort_order: index + 1,
          source_line: section.source_line,
        });
      });
    }

    return {
      report: {
        database: databasePath,
        integrity,
        selectedTopicCount: selected.length,
        expectedTopicCount: 18,
        validTopicCount: reportTopics.filter((topic) => topic.valid).length,
        requiresManualReview: reportTopics.some((topic) => !topic.valid),
        topics: reportTopics,
      },
      preview: {
        table: 'tbl_content_sections',
        generatedFrom: databasePath,
        rowCount: previewRows.length,
        rows: previewRows,
      },
    };
  } finally {
    database.close();
  }
}

function printHelp() {
  console.log(`Pemakaian:
  node --no-warnings content/tools/audit-content-sections.mjs [opsi]

Opsi:
  --database PATH  SQLite sumber (default: ${DEFAULT_DATABASE})
  --report PATH    Laporan audit JSON (default: ${DEFAULT_REPORT})
  --preview PATH   Pratinjau baris tabel JSON (default: ${DEFAULT_PREVIEW})
  --help           Tampilkan bantuan

Database selalu dibuka dalam mode baca-saja.`);
}

const invokedDirectly = process.argv[1]
  && resolve(process.argv[1]) === resolve(fileURLToPath(import.meta.url));

if (invokedDirectly) {
  try {
    const options = parseArguments(process.argv.slice(2));
    if (options.help) {
      printHelp();
      process.exit(0);
    }
    const result = auditDatabase(options.database);
    const reportPath = writeJson(options.report, result.report);
    const previewPath = writeJson(options.preview, result.preview);
    console.log(`Laporan: ${reportPath}`);
    console.log(`Pratinjau: ${previewPath}`);
    console.log(`Topik: ${result.report.selectedTopicCount}/${result.report.expectedTopicCount}`);
    console.log(`Valid tanpa tinjauan: ${result.report.validTopicCount}`);
    console.log(`Baris pratinjau: ${result.preview.rowCount}`);
    console.log(`Perlu tinjauan manual: ${result.report.requiresManualReview ? 'ya' : 'tidak'}`);
    process.exitCode = result.report.integrity === 'ok' ? 0 : 1;
  } catch (error) {
    console.error(error instanceof Error ? error.message : error);
    process.exitCode = 1;
  }
}
