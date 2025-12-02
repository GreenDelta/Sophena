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


def add_col(path: Path) -> None:
    print(f"  . add column to: {path.name}")
    rows = []
    with open(path, "r", encoding="utf-8") as f:
        r = csv.reader(f, delimiter=";")
        i = 0
        for row in r:
            val = "product line" if i == 0 else ""
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
