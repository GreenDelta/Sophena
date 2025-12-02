#!/usr/bin/env python3
"""
Script to add a 'product line' column to all product CSV files.
The column is inserted after the 'price' column (index 6).
"""

import os
from pathlib import Path

# CSV files that contain product data
PRODUCT_CSV_FILES = [
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

# Column index after which to insert the new column (after 'price')
INSERT_AFTER_INDEX = 6

# The delimiter used in the CSV files
DELIMITER = " ; "


def add_product_line_column(file_path: Path) -> None:
    """Add a 'product line' column to a CSV file after the price column."""
    print(f"Processing: {file_path.name}")
    
    with open(file_path, "r", encoding="utf-8") as f:
        lines = f.readlines()
    
    if not lines:
        print(f"  Skipping empty file: {file_path.name}")
        return
    
    new_lines = []
    for i, line in enumerate(lines):
        # Remove trailing newline for processing
        line = line.rstrip("\n")
        
        # Split by delimiter
        columns = line.split(DELIMITER)
        
        if len(columns) <= INSERT_AFTER_INDEX:
            print(f"  Warning: Line {i + 1} has fewer columns than expected, skipping")
            new_lines.append(line + "\n")
            continue
        
        # Insert the new column after the price column
        if i == 0:
            # Header row - insert column name
            new_column_value = "product line"
        else:
            # Data rows - insert empty value
            new_column_value = ""
        
        # Insert at position INSERT_AFTER_INDEX + 1 (after price)
        columns.insert(INSERT_AFTER_INDEX + 1, new_column_value)
        
        # Reconstruct the line
        new_line = DELIMITER.join(columns) + "\n"
        new_lines.append(new_line)
    
    # Write back to file
    with open(file_path, "w", encoding="utf-8") as f:
        f.writelines(new_lines)
    
    print(f"  Done: Added 'product line' column to {file_path.name}")


def main():
    # Get the script directory and navigate to the csv folder
    script_dir = Path(__file__).parent
    csv_dir = script_dir.parent / "data" / "csv"
    
    if not csv_dir.exists():
        print(f"Error: CSV directory not found: {csv_dir}")
        return
    
    print(f"CSV directory: {csv_dir}")
    print(f"Adding 'product line' column after index {INSERT_AFTER_INDEX} (after 'price')")
    print("-" * 60)
    
    processed = 0
    for csv_file in PRODUCT_CSV_FILES:
        file_path = csv_dir / csv_file
        if file_path.exists():
            add_product_line_column(file_path)
            processed += 1
        else:
            print(f"Warning: File not found: {csv_file}")
    
    print("-" * 60)
    print(f"Processed {processed} files.")


if __name__ == "__main__":
    main()
