
var test = function() {
    print("Hi there from Javascript");
};

var which = function() {
    var exe = "mvn";
    print("Trying out which for: " + exe);
    var exeFile = F.which(exe);
    print("Which result: " + exeFile);
}

var ls = function() {
    F.execute("ls", "-la");
};

var mvn = function() {
    F.execute("mvn", "compile");
};

var play = function() {
    F.execute("play", "--version");
};

var fun2 = function (object) {
    print("JS Class Definition: " + Object.prototype.toString.call(object));
};
