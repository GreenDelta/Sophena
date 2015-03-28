var fs = require('fs');
var uuid = require('node-uuid');
var mkdir = require('mkdirp');

var args = process.argv;

var types = [
    'Boiler',
    'Fuel'
];


if (args.length < 4) {
    console.log('not enough args');
    process.exit(0);
}
create(args[3]);

function create(type) {
    var folder = 'data/' + getFolderName(type);
    mkdir.mkdirp(folder, function (err) {
        if (err)
            console.error(err);
        else
            copyTemplate(type, folder);
    });    
}

function getFolderName(type) {
    if (!type)
        return 'unknown';
    return type.toLowerCase() + 's';
}

function copyTemplate(type, folder) {
    var uid = uuid.v4().toString();
    var file = folder + '/' + uid + '.json';
    var data ={
        '@type': type,
        'id': uid,
        'name': 'name' 
    };
    var json = JSON.stringify(data, null, 4);
    fs.writeFile(file, json, function (err) {
        if (err) {
            console.log(err);
        } else {
            console.log('created file ', file);
        }
    });
}



