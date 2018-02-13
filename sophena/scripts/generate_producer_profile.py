# generates an example producer profile

import csv
import math

PATH = 'C:/Users/Besitzer/Desktop/example_profile.csv'


def main():
    with open(PATH, 'w', encoding='utf-8', newline='\n') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['hour', 'max', 'min'])
        heat = 0
        for day in range(1, 366):
            offset = (day - 1) * 24
            is_work_day = day % 6 != 0 and day % 7 != 0
            for hour in range(1, 25):
                dec_heat = not is_work_day or (hour < 8 or hour > 18)
                if dec_heat:
                    if heat < 100:
                        heat = 0
                    else:
                        heat -= 100
                else:
                    if heat > 3800:
                        heat = 4000
                    else:
                        heat += 200

                min_heat = 200 if heat > 200 else heat
                writer.writerow([offset + hour, heat, min_heat])


if __name__ == '__main__':
    main()
