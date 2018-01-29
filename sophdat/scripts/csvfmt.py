import csv
import os


def main():
    dir_path = os.path.abspath('./data/csv')
    for f in os.listdir(dir_path):
        file_path = dir_path + os.path.sep + f
        format(file_path)


def format(file_path: str):
    print('format file: ' + file_path)
    rows = read_rows(file_path)
    print('  %s lines' % len(rows))
    schema = get_schema(rows)
    print('  apply schema = %s' % schema)
    apply_schema(rows, schema)
    with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
        writer = csv.writer(f, delimiter=';')
        for row in rows:
            writer.writerow(row)
    print('  all done\n')


def read_rows(file_path: str) -> list:
    rows = []
    with open(file_path, 'r', encoding='utf-8', newline='\n') as f:
        reader = csv.reader(f, delimiter=';')
        for row in reader:
            r = [v.strip() for v in row]
            rows.append(r)
    return rows


def get_schema(rows: list) -> dict:
    schema = {}
    for row in rows:
        for i in range(0, len(row)):
            size = csv_size(row[i])
            if i not in schema:
                schema[i] = size
                continue
            old = schema[i]
            if size > old:
                schema[i] = size
    return schema


def csv_size(term: str):
    if ';' in term:
        return len(term) + 2  # length + quotation marks
    else:
        return len(term)


def apply_schema(rows: list, schema: dict):
    for row in rows:
        for i in range(0, len(row)):
            val = row[i]
            size = schema[i] + 1
            if i > 0:
                size += 2
                val = ' ' + val
            if ';' in val:
                size -= 2
            while len(val) < size:
                val += ' '
            row[i] = val


if __name__ == '__main__':
    main()
