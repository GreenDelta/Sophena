# a Julia script that generates the methods of the SheetWriter class

function generate(
    method_name, type, doc_name; converter="val", bold=false)

    style = ""
    if bold
        style = ".setCellStyle(bold)"
    end

    templ = """

    // $method_name::$type

    /**
     * Writes the given $doc_name and then moves the cursor to the next column
     * of the current row.
     */
	SheetWriter $method_name($type val) {
        Excel.cell(sheet, row, col, $converter)$style;
        col++;
		return this;
	}

    /**
	 * Moves the cursor to the given column of the current row and writes the
	 * given $doc_name.
	 */
	SheetWriter $method_name(int col, $type val) {
		this.col = col;
        return $method_name(val);
	}

    /**
	 * Moves the cursor to the given position and writes the given $doc_name.
	 */
	SheetWriter $method_name(int row, int col, $type val) {
		this.row = row;
		this.col = col;
		return $method_name(val);
	}
    """
end

open("out.java", "w") do stream
    write(stream, generate(
        "boldStr", "String", "string in bold format", bold=true))
    write(stream, generate("str", "String", "string"))
    write(stream, generate("num", "int", "integer"))
    write(stream, generate("num", "double", "number"))
    write(stream, generate(
        "rint", "double", "number as integer", converter="Math.round(val)"))
    write(stream, generate(
        "boldRint", "double", "number as integer in bold format",
        converter="Math.round(val)", bold=true))
end
