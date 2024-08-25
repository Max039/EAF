package eaf.plugin;

public class Plugin {

    public String path;

    public String name;

    public Plugin(String path) {
        this.path = path;
        var split = path.replace("\\", "/").split("/");
        this.name = split[split.length - 1];
    }

}
