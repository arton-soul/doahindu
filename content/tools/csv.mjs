import { readFileSync, writeFileSync } from 'node:fs';

export function parseCsv(text) {
  const rows = [];
  let row = [];
  let field = '';
  let quoted = false;
  for (let index = 0; index < text.length; index += 1) {
    const character = text[index];
    if (quoted) {
      if (character === '"' && text[index + 1] === '"') {
        field += '"';
        index += 1;
      } else if (character === '"') {
        quoted = false;
      } else {
        field += character;
      }
    } else if (character === '"') {
      quoted = true;
    } else if (character === ',') {
      row.push(field);
      field = '';
    } else if (character === '\n') {
      row.push(field.replace(/\r$/, ''));
      rows.push(row);
      row = [];
      field = '';
    } else {
      field += character;
    }
  }
  if (quoted) throw new Error('CSV memiliki tanda kutip yang tidak ditutup.');
  if (field.length || row.length) {
    row.push(field.replace(/\r$/, ''));
    rows.push(row);
  }
  return rows;
}

export function readCsv(path, requiredColumns) {
  const rows = parseCsv(readFileSync(path, 'utf8'));
  if (!rows.length) throw new Error(`${path} kosong.`);
  const header = rows[0].map((column) => column.replace(/^\uFEFF/, ''));
  const missing = requiredColumns.filter((column) => !header.includes(column));
  if (missing.length) throw new Error(`${path} kehilangan kolom: ${missing.join(', ')}`);
  return rows.slice(1).filter((row) => row.some((field) => field !== '')).map((row, index) => {
    const value = { _row: index + 2 };
    header.forEach((column, columnIndex) => { value[column] = row[columnIndex] ?? ''; });
    return value;
  });
}

export function writeCsv(path, columns, rows) {
  const escape = (value) => {
    const text = value == null ? '' : String(value);
    return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
  };
  const output = [columns.join(','), ...rows.map((row) =>
    columns.map((column) => escape(row[column])).join(','))].join('\n') + '\n';
  writeFileSync(path, output, 'utf8');
}
