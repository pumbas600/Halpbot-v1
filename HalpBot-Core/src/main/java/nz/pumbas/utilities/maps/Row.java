package nz.pumbas.utilities.maps;

import java.util.Map;

public final class Row
{
    private final Map<String, Object> fieldMap;

    private Row(Map<String, Object> fieldMap)
    {
        this.fieldMap = fieldMap;
    }

    public static Row of(Map<String, Object> fieldMap)
    {
        return new Row(fieldMap);
    }

    public Object get(String key)
    {
        if (!this.fieldMap.containsKey(key))
            throw new IllegalArgumentException(
                String.format("The key %s is not mapped to this column", key));

        return this.fieldMap.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key)
    {
        return (T) this.get(key);
    }

    public String getString(String key)
    {
        return this.getValue(key);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        this.fieldMap.forEach((k, v) ->
            builder.append(k).append(": ").append(v).append('\n'));

        //Remove the last newline character
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }
}
