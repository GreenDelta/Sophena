"""
Converts the Excel file with product data into the respective CSV files. It
also generates the UUIDs and makes some plausibility checks.
"""

import csv
import csvfmt
import logging as log
import sys
import uuid
import xlrd

log.basicConfig(level=log.INFO, format='%(levelname)s %(message)s',
                stream=sys.stdout)


def main(xls_file):
    log.info('extract data from %s', xls_file)
    wb = xlrd.open_workbook(xls_file)
    write_manufacturers(wb)
    write_boilers(wb)
    write_cogen_boilers(wb)
    write_heat_recoveries(wb)
    write_flue_gas_cleanings(wb)
    write_buffers(wb)
    write_pipes(wb)
    write_transfer_stations(wb)


def write_boilers(wb):
    log.info('extract boilers')
    header = prepare_header() + ['fuel', 'max_power', 'min_power', 'eff. rate',
                                 'description']
    write_records(wb, 'Kessel', header, 'boilers.csv',
                  key_fn=lambda x: path(*(x[1:5] + [x[8]])))


def write_cogen_boilers(wb):
    log.info('extract cogen-boilers')
    header = prepare_header() + ['fuel', 'max_power', 'min_power', 'eff. rate',
                                 'max_power_el', 'min_power_el', 'eff. rate el',
                                 'description']
    write_records(wb, 'KWK', header, 'boilers_cogen_plants.csv',
                  key_fn=lambda x: path(*(x[1:5] + [x[11]])))


def write_heat_recoveries(wb):
    log.info('extract heat recoveries')
    header = prepare_header() + ['power', 'producer type', 'fuel',
                                 'producer power', 'description']
    write_records(wb, 'Wärmerückgewinnung', header, 'heat_recoveries.csv')


def write_flue_gas_cleanings(wb):
    log.info('extract flue gas cleanings')
    header = prepare_header() + ['max. volume flow', 'fuel',
                                 'max. producer power', 'el. demand', 'kind',
                                 'type', 'separation rate', 'description']
    write_records(wb, 'Rauchgasreinigung', header, 'flue_gas_cleanings.csv',
                  key_fn=lambda x: path(*(x[1:5] + [x[9]])))


def write_buffers(wb):
    log.info('extract buffer tanks')
    header = prepare_header() + ['volume', 'diameter', 'height', 'insulation',
                                 'description']
    write_records(wb, 'Pufferspeicher', header, 'buffer_tanks.csv')


def write_pipes(wb):
    log.info('extract pipes')
    header = prepare_header() + ['material', 'type', 'u_value',
                                 'inner diameter', 'outer diameter',
                                 'total diameter', 'delivery type',
                                 'max. temperature', 'max. pressure',
                                 'description']
    write_records(wb, 'Wärmeleitungen', header, 'pipes.csv')


def write_transfer_stations(wb):
    log.info('extract transfer stations')
    header = prepare_header() + ['building_type', 'power', 'type', 'material',
                                 'hot water', 'control', 'description']
    write_records(wb, 'Hausübergabestationen', header, 'transfer_stations.csv',
                  key_fn=lambda x: path(*(x[1:5] + [x[8]])))


def write_records(workbook, sheet_name, header, csv_file, key_fn=None):
    records = []
    sheet = workbook.sheet_by_name(sheet_name)
    key = key_fn
    if key is None:
        key = lambda x: path(*(x[1:5]))
    for row in rows(sheet):
        r = ['']
        for col in range(0, len(header)-1):
            r.append(sheet.cell_value(row, col))
        r_path = key(r)
        log.debug('  >> %s', r_path)
        r[0] = uid(r_path)
        r.append(r_path)
        records.append(r)
    records.sort(key=key)
    records.insert(0, header + ['key'])
    write(records, csv_file)


def prepare_header():
    return ['id', 'product type', 'product group', 'manufacturer', 'name',
            'url', 'price']


def write_manufacturers(workbook):
    log.info('extract manufacturers')
    names = get_producer_names(workbook)
    names.sort()
    records = [['id', 'name', 'address', 'url', 'description']]
    for name in names:
        r = [uid(name), name, '', '', '']
        records.append(r)
    write(records, 'manufacturers.csv')


def get_producer_names(workbook) -> list:
    producers = []
    for sheet_name in workbook.sheet_names():
        sheet = workbook.sheet_by_name(sheet_name)
        for row in rows(sheet):
            producer = sheet.cell_value(row, 2)
            if not isinstance(producer, str):
                continue
            producer = producer.strip()
            if producer == '' or producer in producers:
                continue
            producers.append(producer)
    return producers


def rows(sheet):
    row = 0
    while True:
        row += 1
        if row >= sheet.nrows:
            break
        name = sheet.cell_value(row, 3)
        if not isinstance(name, str):
            break
        name = name.strip()
        if name == '':
            break
        yield row


def path(*args):
    parts = []
    for arg in args:
        a = arg
        if isinstance(a, float):
            a = int(a)
        a = str(a).lower()
        parts.append(a)
    return '/'.join(parts)


def uid(*args):
    parts = [a.strip().lower() for a in args]
    path = '/'.join(parts)
    return str(uuid.uuid3(uuid.NAMESPACE_OID, path))


def write(records, file_name):
    path = './data/csv/' + file_name
    log.info('write records to %s', path)
    with open(path, 'w', encoding='utf8', newline='\n') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerows(records)
    csvfmt.format(path)


if __name__ == '__main__':
    main('./data/Produktdatenbank_2016_07_08.xlsx')
