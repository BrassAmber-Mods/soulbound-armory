#!/usr/bin/env python3
from re import search


def change_version(*files):
    ver_expr = r'\d+\.\d+\.\d+'
    type_expr = r'(-release|-beta|-alpha)'

    file = files[0]
    lines = file.readlines()

    for line in lines:
        match = search(f'{ver_expr}{type_expr}', line)

        if not match:
            match = search(ver_expr, line)

        if match:
            break

    print(f'\nThe current version is {match[0]}.')
    index = search(r'[0-2]', input('Enter the version index to change.\n'))

    if not index:
        index = 2
        new = str(int(version[2]) + 1)
    else:
        index = int(index[0])

    while not new:
        new = search(r'\d+', input('Enter the new number for this index.\n'))

        if new:
            new = new[0]

    for file in files:
        lines = file.readlines()

        for i in range(len(lines)):
            line = lines[i]
            match = search(f'{ver_expr}{type_expr}', line)

            if not match:
                match = search(ver_expr, line)

            if match:
                version = search(ver_expr, line)[0]
                before = line[:line.index(version)]
                after = line[line.index(version) + len(version):]
                version = version.split('.')

                version[index] = new

                if index >= 0:
                    version[2] = "0"

                    if index == 0:
                        version[1] = "0"

                line = f'{before}{".".join(version)}{after}'
                lines[i] = line

                file.seek(0)
                file.truncate()
                file.write(''.join(lines))

                break

with open('build.gradle', 'r+') as gradle, open('src/main/java/transfarmer/soulboundarmory/Main.java', 'r+') as main:
    change_version(gradle, main)