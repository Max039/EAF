package eaf.compiler;

import eaf.models.ClassType;
import eaf.models.FieldType;
import eaf.models.FieldValue;
import eaf.models.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ClassFactory {

    public static ClassType createClass(String name, String pack, String parent, boolean abst, ArrayList<Pair<String, Pair<FieldType, String>>> fields) {
        ClassType parentClass = null;
        if (!parent.isEmpty()) {
            parentClass = SyntaxTree.classRegister.get(parent);
            if (parentClass == null) {
                System.out.println("Warning parent \"" + parent + "\" for rect \"" + name + "\" not found!");
            }
        }

        var c = new ClassType(name, parentClass, pack);

        for (var f : fields) {
            var fname = f.getFirst();
            var type = f.getSecond().getFirst();
            var value = f.getSecond().getSecond();
            c.addField(fname, type);
            if (type.primitive && !value.isEmpty()) {
                c.setField(fname, new FieldValue(type.typeName, value));
            }
        }

        return c;
    }

    public static ClassType fromRectFile(JSONObject o, String pack) {
        var name = o.getString("name");
        var abst = o.getBoolean("abstract");
        var parent = o.getString("parent");
        var fields = new ArrayList<Pair<String, Pair<FieldType, String>>>();
        var arr = o.getJSONArray("fields");
        for (var f : arr) {
            var o2 = (JSONObject) f;
            var fieldName = o2.getString("field-name");
            var primitive = o2.getBoolean("primitive");
            var arrayCount = o2.getInt("array-count");
            var typeName = o2.getString("type-name");
            var value = o2.getString("value");
            fields.add(new Pair<>(fieldName, new Pair<>(new FieldType(typeName, primitive, arrayCount), value)));
        }
        return createClass(name, pack, parent, abst, fields);
    }

    public static JSONObject toRectFile(ClassType c) {
        JSONObject o = new JSONObject();
        o.put("name", c.name);
        o.put("abstract", c.isAbstract);
        if (c.parent == null) {
            o.put("parent", "");
        }
        else {
            o.put("parent", c.parent.name);
        }
        var arr = new JSONArray();
        for (var f : c.fields.entrySet()) {
            var type = f.getValue().getFirst();
            var value = f.getValue().getSecond();
            var o2 = new JSONObject();
            o2.put("field-name", f.getKey());
            o2.put("primitive", type.primitive);
            o2.put("array-count", type.arrayCount);
            o2.put("type-name", type.typeName);
            if (type.primitive && value != null) {
                o2.put("value", value);
            }
            else {
                o2.put("value", "");
            }
            arr.put(o2);
        }
        o.put("fields", arr);
        return o;
    }


}
