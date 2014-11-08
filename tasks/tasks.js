
var test = function() {
    print("Hi there from Javascript");
};

var which = function() {
    var exe = "mvn";
    print("Trying out which for: " + exe);
    var exeFile = F.which(exe);
    print("Which result: " + exeFile);
}

var compile = function() {
    F.executor("mvn", "compile").execute();
};

var fun2 = function (object) {
    print("JS Class Definition: " + Object.prototype.toString.call(object));
};
