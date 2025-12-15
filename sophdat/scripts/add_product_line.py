import csv

from pathlib import Path

PRODUCT_CSVS = [
    "boilers.csv",
    "boilers_cogen_plants.csv",
    "buffer_tanks.csv",
    "flue_gas_cleanings.csv",
    "heat_pumps.csv",
    "heat_recoveries.csv",
    "pipes.csv",
    "solar_collectors.csv",
    "transfer_stations.csv",
]

# if infer is set to true, it will try to infer the name
# of the product line from the product name
INFER = True


def strip_line(s: str) -> str:
    if not s:
        return ""
    r = s.strip()
    while len(r) > 0:
        last = r[-1]
        if last.isalpha() or last == ")":
            break
        r = r[:-1]
    return r


def infer(name: str) -> str:
    """Infer the product line from the product name"""
    # find the position of the first letter
    first_letter = -1
    for i, c in enumerate(name):
        if c.isalpha():
            first_letter = i
            break
    if first_letter == -1:
        return name

    # find the first number after the first letter
    first_num_after_letter = -1
    for i in range(first_letter + 1, len(name)):
        if name[i].isdigit():
            first_num_after_letter = i
            break

    if first_num_after_letter == -1:
        result = name
    else:
        result = name[:first_num_after_letter]

    # trim trailing non-letter characters
    while result and not result[-1].isalpha():
        result = result[:-1]

    return result


def add_col(path: Path) -> None:
    print(f"  . add column to: {path.name}")
    rows = []
    with open(path, "r", encoding="utf-8") as f:
        r = csv.reader(f, delimiter=";")
        i = 0
        for row in r:
            if len(row) == 0:
                continue
            if i == 0:
                val = "product line"
            elif INFER:
                val = infer(row[4])
            else:
                val = ""
            row.insert(4, val)
            rows.append(row)
            i += 1

    with open(path, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter=";")
        w.writerows(rows)


def main():
    script_dir = Path(__file__).parent
    csv_dir = script_dir.parent / "data" / "csv"
    for csv_file in PRODUCT_CSVS:
        add_col(csv_dir / csv_file)


if __name__ == "__main__":
    main()
