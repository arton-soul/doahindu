import assert from 'node:assert/strict';
import test from 'node:test';
import { parseCsv } from './csv.mjs';

test('membaca koma, kutip, dan baris baru dalam sel', () => {
  assert.deepEqual(parseCsv('id,text\n1,"baris satu, ya\nbaris ""dua"""\n'), [
    ['id', 'text'], ['1', 'baris satu, ya\nbaris "dua"'],
  ]);
});

test('menolak tanda kutip yang tidak ditutup', () => {
  assert.throws(() => parseCsv('id,text\n1,"belum selesai'), /tidak ditutup/);
});
