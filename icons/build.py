import os


html = """<!DOCTYPE html>
<html>
<body style="margin: 20px;">
{svg}
</body>
</html>
"""

for svg_file in os.listdir("svg"):
    if svg_file.endswith(".svg"):
        with open("svg/"+svg_file, 'r', encoding="utf-8") as f:
            html_text = html.format(svg=f.read())
            with open("html/"+svg_file + ".html", 'w', encoding="utf-8") as h:
                h.write(html_text)
