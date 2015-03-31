
function keys() {
    var months = ["01", "02", "03", "04", "05", "06",
				"07", "08", "09", "10", "11", "12"];
    var days = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    var index = [];
    var idx = 0;
    for (var month = 0; month < months.length; month++) {
        for (var day = 1; day <= days[month]; day++) {
            for (var hour = 0; hour < 24; hour++) {
                var key = months[month] + format(day) + format(hour);
                index.push(key);
                idx++;
            }
        }
    }
    return index;
}

function format(val) {
    if (val < 10)
        return "0" + val;
    else
        return val.toString();
}

function keyIndexMap() {
    var keySet = keys();
    var map = {};
    for (var i = 0; i < keySet.length; i++) {
        map[keySet[i]] = i;
    }
    return map;
} 

module.exports.keys = keys;
module.exports.keyIndexMap = keyIndexMap;