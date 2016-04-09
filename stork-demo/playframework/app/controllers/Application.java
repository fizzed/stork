package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static String[] sysprops = { "user.dir","user.name","user.home","launcher.name","launcher.type","launcher.action","launcher.app.dir","java.class.path","java.home","java.version","java.vendor","os.arch","os.name","os.version" };
    
}
