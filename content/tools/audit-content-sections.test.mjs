import assert from 'node:assert/strict';
import test from 'node:test';

import { parseMarker, parseSections } from './audit-content-sections.mjs';

test('mengenali marker mandiri dengan dan tanpa titik', () => {
  assert.deepEqual(parseMarker(' 19 '), { number: 19, remainder: '' });
  assert.deepEqual(parseMarker('1.'), { number: 1, remainder: '' });
});

test('mengenali marker dan isi awal pada baris yang sama', () => {
  assert.deepEqual(parseMarker('2. Sanjaya uvaca:'), {
    number: 2,
    remainder: 'Sanjaya uvaca:',
  });
  assert.deepEqual(parseMarker('25.bhisma-drona-pramukhatah'), {
    number: 25,
    remainder: 'bhisma-drona-pramukhatah',
  });
});

test('tidak menganggap angka di dalam isi biasa sebagai marker', () => {
  assert.equal(parseMarker('Ada 3 jalan utama.'), null);
  assert.equal(parseMarker('2026 adalah tahun penerbitan'), null);
  assert.equal(parseMarker('2.5 adalah nilai desimal'), null);
});

test('memecah format campuran dan mempertahankan isi', () => {
  const parsed = parseSections([
    '1',
    'baris Sanskerta',
    '',
    'terjemahan',
    '',
    '2. Sanjaya uvaca:',
    'baris berikutnya',
  ].join('\n'));

  assert.deepEqual(parsed.sections, [
    {
      section_number: 1,
      section_text: 'baris Sanskerta\n\nterjemahan',
      source_line: 1,
    },
    {
      section_number: 2,
      section_text: 'Sanjaya uvaca:\nbaris berikutnya',
      source_line: 6,
    },
  ]);
  assert.deepEqual(parsed.diagnostics.missingNumbers, []);
});

test('melaporkan nomor ganda, urutan turun, dan kandidat ambigu', () => {
  const parsed = parseSections([
    '1',
    'isi',
    '3',
    'isi',
    '3.',
    'isi',
    '2',
    'isi',
    '4) bentuk belum didukung',
  ].join('\n'));

  assert.deepEqual(parsed.diagnostics.duplicateNumbers, [3]);
  assert.deepEqual(parsed.diagnostics.missingNumbers, []);
  assert.deepEqual(parsed.diagnostics.nonIncreasing, [
    { previous: 3, current: 3 },
    { previous: 3, current: 2 },
  ]);
  assert.deepEqual(parsed.diagnostics.ambiguousLines, [
    { line: 9, text: '4) bentuk belum didukung' },
  ]);
});

test('melaporkan nomor yang hilang', () => {
  const parsed = parseSections('1\nisi pertama\n3\nisi ketiga');
  assert.deepEqual(parsed.diagnostics.missingNumbers, [2]);
});
