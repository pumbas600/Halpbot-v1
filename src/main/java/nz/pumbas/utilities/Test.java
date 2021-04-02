package nz.pumbas.utilities;

import nz.pumbas.objects.keys.KeyHolder;
import nz.pumbas.utilities.maps.FieldMap;

public class Test implements KeyHolder<Test>
{
    @FieldMap(value = {"displayName=Field", "quantity=5F"},
              types = {String.class, float.class})
    public double field;

    @FieldMap(value = {"displayName=Another Field", "pi=3.14159"},
              types = {String.class, double.class})
    public double anotherField;
}
