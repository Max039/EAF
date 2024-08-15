package test;

import compiler.ClassType;
import compiler.FieldType;
import compiler.FieldValue;
import compiler.SyntaxTree;
import org.json.JSONArray;
import org.json.JSONObject;
import test.rects.OptionsFieldRect;
import test.rects.Rect;
import test.rects.TextFieldRect;
import test.rects.multi.ArrayRect;
import test.rects.multi.ClassRect;

import java.util.ArrayList;

public class RectFactory {
    public static <T extends Rect> T getRectFromClassType(ClassType type) {

        int length = type.fields.size();
        var names = new String[length];
        var types = new FieldType[length];
        var rects = new Rect[length];
        int i = 0;
        for (var field : type.fields.entrySet()) {
            names[i] = field.getKey();
            types[i] = field.getValue().getFirst();
            rects[i] = getRectFromFieldType(field.getValue().getFirst(), field.getValue().getSecond());
           i++;
        }
        return (T) new ClassRect(RectPanel.instanceWidth, RectPanel.instanceHeight, RectPanel.instanceColor, type, names, rects, types, false);
    }

    public static <T extends Rect> T getRectFromFieldType(FieldType type, FieldValue value) {

        if (value != null) {
            if (type.arrayCount > 0) {
                int length = value.values.size();
                var clazz = new ClassType(type.typeName, null, "Array");
                var names = new String[length];
                var types = new FieldType[length];
                var rects = new Rect[length];

                int i = 0;
                for (var item : value.values) {
                    names[i] = "";
                    types[i] = item.type;
                    rects[i] = getRectFromFieldType(item.type, item);
                    i++;
                }

                FieldType ctype = type.clone();
                ctype.arrayCount -= 1;
                return (T) new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, clazz, ctype, names, rects, types, type.primitive, true);

            }
            else {
                Rect preR;
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    String content;
                    if (type.typeName.toLowerCase().contains("int") ||  type.typeName.toLowerCase().contains("real") || type.typeName.toLowerCase().contains("literal") || type.typeName.toLowerCase().contains("string") )  {
                        content = value.value;
                    } else if (type.typeName.toLowerCase().contains("bool")) {
                        var r = new ArrayList<Object>();
                        r.add(true);
                        r.add(false);
                        return (T)  new OptionsFieldRect(r, value.value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, TextFieldRect.uneditableColor);
                    }
                    else if (type.typeName.toLowerCase().contains("data")) {

                        return (T)  new OptionsFieldRect(Main.dataPanel.getDataFieldList(), value.value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, TextFieldRect.uneditableColor);
                    }
                    else {
                        content  = "Unkown primitive";
                        System.out.println("Unkown primitve: " + type.typeName);
                    }
                    preR = new TextFieldRect(content, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true);
                    ((TextFieldRect) preR).setTextColor(TextFieldRect.uneditableColor);
                    return (T)  preR;
                }
                else {
                    return getRectFromClassType(value.instance);
                }
            }
        }
        else {
            if (type.arrayCount > 0) {
                var clazz = new ClassType(type.typeName, null, "Array");
                FieldType ctype = type.clone();
                ctype.arrayCount -= 1;
                boolean fill = type.primitive;
                if (!fill) {
                    var check = SyntaxTree.classRegister.get(type.typeName).findSingleNonAbstractClass();
                    if (check != null) {
                        System.out.println("Info: Only 1 non abstract type available for " + type.typeName + " converting array to " + check.name);
                        fill = true;
                        ctype = new FieldType(check.name, false, ctype.arrayCount);
                    }
                }

                return (T) new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, clazz,  ctype, Main.arrayDefaultCount, fill);
            }
            else {
                if (type.primitive) {
                    var c = new ClassType(type.typeName, null, "Primitive");
                    String content;
                    if (type.typeName.toLowerCase().contains("string") ) {
                        content = "Enter string here!";
                    }
                    else if (type.typeName.toLowerCase().contains("int") ||  type.typeName.toLowerCase().contains("real"))  {
                        content = "0";
                    } else if (type.typeName.toLowerCase().contains("bool")) {
                        var r = new ArrayList<Object>();
                        r.add(true);
                        r.add(false);
                        return (T) new OptionsFieldRect(r, "true", RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, OptionsFieldRect.defaultTextColor);
                    }
                    else if (type.typeName.toLowerCase().contains("data")) {
                        return (T) new OptionsFieldRect(Main.dataPanel.getDataFieldList(), "", RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true, OptionsFieldRect.defaultTextColor);
                    }
                    else if (type.typeName.toLowerCase().contains("literal")) {
                        content  = "Enter literal here!";
                    }
                    else {
                        content  = "Unkown primitive";
                        System.out.println("Unkown primitve: " + type.typeName);
                    }
                    return (T) new TextFieldRect(content, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, true);
                }
                else {
                    var check = SyntaxTree.classRegister.get(type.typeName).findSingleNonAbstractClass();
                    if (check != null) {
                        System.out.println("Info: Only 1 non abstract type available for " + type.typeName + " creating and setting instance of " + check.name + " for field.");
                        var newR = (ClassRect) getRectFromClassType(check);
                        newR.setLocked(true);
                        return (T) newR ;
                    }
                    else {
                        return null;
                    }
                }
            }
        }
    }

    public static Rect rectFromJson(JSONObject arr) {

        String clazz = (String) arr.get("sub-type");


        switch ((String) arr.get("type")) {
            case "option-field" :
                String value = (String) arr.get("value");

                var c = new ClassType(clazz, null, "Primitive");
                boolean b1 = (Boolean) arr.get("editable");
                if (clazz.contains("bool")) {
                    var r = new ArrayList<Object>();
                    r.add(true);
                    r.add(false);
                    return new OptionsFieldRect(r, value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, b1, TextFieldRect.defaultTextColor);
                }
                else {
                    return new OptionsFieldRect(Main.dataPanel.getDataFieldList(), value, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c, b1, OptionsFieldRect.defaultTextColor);
                }
            case "text-field" :
                String value2 = (String) arr.get("value");
                boolean b2 = (Boolean) arr.get("editable");
                var c2 = new ClassType(clazz, null, "Primitive");
                return new TextFieldRect(value2, RectPanel.textBoxWidth, RectPanel.textBoxHeight, RectPanel.primitiveColor, c2, b2);
            case "instance" :
                var reg = SyntaxTree.classRegister.get(clazz);
                String pack = (String) arr.get("package");
                if (!reg.pack.equals(pack)) {
                    System.out.println("WARNING: Unequal pack \"" + pack + "\" != \"" + reg.pack + "\" for class \"" + clazz + "\" you can ignore this if you changed the class domain.");
                }

                var v = new FieldValue(reg);
                var instance =  (ClassRect) getRectFromClassType(v.instance);
                JSONArray value3 = (JSONArray) arr.get("value");
                int i = 0;
                for (var field : reg.fields.entrySet()) {
                    boolean found = false;
                    for (var jsonField : value3) {
                        if (((String)((JSONObject)jsonField).get("field-name")).equals(field.getKey())) {
                            var fr = rectFromJson((JSONObject)jsonField);
                            if (field.getValue().getSecond() != null) {
                                if (fr instanceof OptionsFieldRect) {
                                    ((OptionsFieldRect)fr).setTextColor(TextFieldRect.uneditableColor);
                                }
                                if (fr instanceof TextFieldRect) {
                                    ((TextFieldRect)fr).setTextColor(TextFieldRect.uneditableColor);
                                }
                            }
                            if (fr instanceof ClassRect && SyntaxTree.classRegister.get(field.getValue().getFirst().typeName).findSingleNonAbstractClass() != null) {
                                ((ClassRect)fr).setLocked(true);
                            }

                            instance.setIndex(i, fr);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("WARNING: For field \"" + field.getKey() + "\" in class \"" + reg.name + "\" no value was found in json!");
                        if (field.getValue().getSecond() == null) {
                            if (field.getValue().getFirst().primitive) {
                                instance.setIndex(i, getRectFromFieldType(field.getValue().getFirst(), null));
                            }
                            else {
                                if (field.getValue().getFirst().primitive) {
                                    instance.setIndex(i, getRectFromFieldType(field.getValue().getFirst(), field.getValue().getSecond()));
                                }
                            }
                        }
                    }
                    i++;
                }
                return instance;

            case "array" :


                JSONArray arrarr = (JSONArray) arr.get("value");

                var arrnames = new String[arrarr.length()];
                var arrtypes = new FieldType[arrarr.length()];
                var arrrects = new Rect[arrarr.length()];

                var ft = new FieldType(clazz, (Boolean) arr.get("primitive"), (Integer) arr.get("count"));

                boolean fill = ft.primitive;
                if (!fill) {
                    var check = SyntaxTree.classRegister.get(ft.typeName).findSingleNonAbstractClass();
                    if (check != null) {
                        System.out.println("Info: Only 1 non abstract type available for " + ft.typeName + " converting array to " + check.name);
                        fill = true;
                        ft = new FieldType(check.name, false, ft.arrayCount);
                    }
                }


                int i3 = 0;
                for (int i4 = 0; i4 < arrarr.length(); i4++) {
                    arrnames[i3] = "";
                    arrtypes[i3] = ft;
                    arrrects[i3] = null;
                    i3++;
                }

                var ct = new ClassType(clazz, null, "Array");



                var arrRect = new ArrayRect<>(RectPanel.arrayWidth, RectPanel.arrayHeight, RectPanel.arrayColor, ct, ft, arrnames, arrrects, arrtypes, fill, true);

                int i2 = 0;
                for (var jsonField : arrarr) {
                    var resRect = rectFromJson((JSONObject)jsonField);
                    arrRect.setIndex(i2, resRect);
                    if (resRect instanceof ClassRect && SyntaxTree.classRegister.get(ft.typeName).findSingleNonAbstractClass() != null) {
                        ((ClassRect)resRect).setLocked(true);
                    }
                    i2++;
                }
                return arrRect;


            default:
                throw new RuntimeException("Invalid rect type \"" + arr.get("type") + "\"!");


        }
    }
}
