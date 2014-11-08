
var test = function() {
    print("Hi there from Javascript");
};

var compile = function() {
    F.executor("mvn2", "compile").execute();
};

var fun2 = function (object) {
    print("JS Class Definition: " + Object.prototype.toString.call(object));
};
